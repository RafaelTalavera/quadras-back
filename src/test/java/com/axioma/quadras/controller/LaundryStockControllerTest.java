package com.axioma.quadras.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryStockBalance;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryUnitType;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryOpeningStockRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryStockBalanceRepository;
import com.axioma.quadras.repository.LaundryStockLotRepository;
import com.axioma.quadras.repository.LaundryStockMovementRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = { "spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate" })
class LaundryStockControllerTest {

	private static final String OPENING = "/api/v2/laundry/opening-stock";
	private static final String ADJUSTMENTS = "/api/v2/laundry/stock-adjustments";
	private static final String DASHBOARD = "/api/v2/laundry/dashboard";
	private static final String METRICS = "/api/v2/laundry/metrics";
	private static final String STOCK = "/api/v2/laundry/stock";
	private static final String RECONCILIATION = "/api/v2/laundry/reconciliation";

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private JdbcTemplate jdbcTemplate;
	@Autowired private LaundryItemRepository itemRepository;
	@Autowired private LaundryProviderRepository providerRepository;
	@Autowired private LaundryOpeningStockRepository openingStockRepository;
	@Autowired private LaundryStockBalanceRepository balanceRepository;
	@Autowired private LaundryStockLotRepository lotRepository;
	@Autowired private LaundryStockMovementRepository movementRepository;

	@BeforeEach
	void cleanStockLedger() {
		jdbcTemplate.update("DELETE FROM laundry_document_confirmations");
		jdbcTemplate.update("DELETE FROM laundry_billable_events");
		jdbcTemplate.update("DELETE FROM laundry_return_allocations");
		jdbcTemplate.update("DELETE FROM laundry_return_manifest_lines");
		jdbcTemplate.update("DELETE FROM laundry_return_manifests");
		jdbcTemplate.update("DELETE FROM laundry_rewash_dispatch_allocations");
		jdbcTemplate.update("DELETE FROM laundry_idempotency_keys");
		jdbcTemplate.update("DELETE FROM laundry_stock_adjustments");
		jdbcTemplate.update("DELETE FROM laundry_opening_stock_lines");
		jdbcTemplate.update("DELETE FROM laundry_opening_stock");
		jdbcTemplate.update("DELETE FROM laundry_obligations");
		jdbcTemplate.update("DELETE FROM laundry_document_lines");
		jdbcTemplate.update("DELETE FROM laundry_documents");
		jdbcTemplate.update("DELETE FROM laundry_stock_movements");
		jdbcTemplate.update("DELETE FROM laundry_stock_lots");
		jdbcTemplate.update("DELETE FROM laundry_stock_balances");
	}

	@Test
	void shouldProtectStockCommandsAndRequireIdempotencyKey() throws Exception {
		final UserSession operator = authenticate("operador.demo", "123456");
		final UserSession supervisor = authenticate("supervisor.demo", "654321");
		final LaundryItem item = item("Proteccion");
		final String effectiveAt = OffsetDateTime.now().withNano(0).toString();

		mockMvc.perform(command(OPENING, operator, "op-1", openingJson(item.getId(), "20.000", effectiveAt)))
				.andExpect(status().isForbidden());

		mockMvc.perform(command(OPENING, supervisor, null, openingJson(item.getId(), "20.000", effectiveAt)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Idempotency-Key header is required"));

		mockMvc.perform(command(OPENING, supervisor, "bad key!", openingJson(item.getId(), "20.000", effectiveAt)))
				.andExpect(status().isBadRequest());

		assertThat(openingStockRepository.count()).isZero();
	}

	@Test
	void shouldApproveOpeningStockAndReplayTheStoredResponse() throws Exception {
		final UserSession supervisor = authenticate("supervisor.demo", "654321");
		final LaundryItem item = item("Apertura");
		final String effectiveAt = OffsetDateTime.now().minusDays(3).withNano(0).toString();
		final String key = "open-" + UUID.randomUUID();
		final String body = openingJson(item.getId(), "20.000", effectiveAt);

		final String firstBody = mockMvc.perform(command(OPENING, supervisor, key, body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.startsWith("OPEN-")))
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.approvedBy").value("supervisor.demo"))
				.andExpect(jsonPath("$.lines[0].itemId").value(item.getId()))
				.andExpect(jsonPath("$.lines[0].quantity").value(20.0))
				.andReturn().getResponse().getContentAsString();

		mockMvc.perform(command(OPENING, supervisor, key, body))
				.andExpect(status().isCreated())
				.andExpect(header().string("Idempotent-Replay", "true"))
				.andExpect(jsonPath("$.id").value(objectMapper.readTree(firstBody).get("id").asLong()));

		assertThat(openingStockRepository.count()).isEqualTo(1);
		assertThat(movementRepository.count()).isEqualTo(1);
		assertThat(balanceRepository.count()).isEqualTo(1);

		mockMvc.perform(command(OPENING, supervisor, key, openingJson(item.getId(), "21.000", effectiveAt)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Idempotency-Key was already used with a different request"
				));

		mockMvc.perform(command(OPENING, supervisor, "another-" + UUID.randomUUID(), body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Opening stock is already approved"));
	}

	@Test
	void shouldExposeDashboardAndStockAging() throws Exception {
		final UserSession supervisor = authenticate("supervisor.demo", "654321");
		final LaundryItem item = item("Tablero");
		final OffsetDateTime effectiveAt = OffsetDateTime.now().minusDays(3).withNano(0);

		mockMvc.perform(command(
						OPENING,
						supervisor,
						"dashboard-" + UUID.randomUUID(),
						openingJson(item.getId(), "20.000", effectiveAt.toString())
				))
				.andExpect(status().isCreated());

		mockMvc.perform(get(DASHBOARD).header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totals.hotelAvailable").value(20.0))
				.andExpect(jsonPath("$.totals.providerProcessing").value(0.0))
				.andExpect(jsonPath("$.totals.total").value(20.0))
				.andExpect(jsonPath("$.items[0].itemId").value(item.getId()))
				.andExpect(jsonPath("$.items[0].hotelAvailable").value(20.0))
				.andExpect(jsonPath("$.items[0].total").value(20.0));

		mockMvc.perform(get(STOCK)
						.param("itemId", item.getId().toString())
						.param("bucket", "HOTEL_AVAILABLE")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(20.0))
				.andExpect(jsonPath("$.items[0].providerId").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.items[0].ageDays").value(greaterThanOrEqualTo(3)))
				.andExpect(jsonPath("$.items[0].oldestEffectiveAt").exists())
				.andExpect(jsonPath("$.items[0].version").isNumber());

		mockMvc.perform(get(STOCK)
						.param("bucket", "HOTEL_AVAILABLE")
						.param("providerId", "1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get(DASHBOARD)
						.param("providerId", "999999")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(METRICS).header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.generatedAt").exists())
				.andExpect(jsonPath("$.providerId").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.stock.hotelAvailable").value(20.0))
				.andExpect(jsonPath("$.stock.total").value(20.0))
				.andExpect(jsonPath("$.oldestPendingAt").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.observedDocuments").value(0))
				.andExpect(jsonPath("$.returnsAwaitingHotel").value(0))
				.andExpect(jsonPath("$.cleanAcceptedQuantity").value(0.0))
				.andExpect(jsonPath("$.rewashQuantity").value(0.0))
				.andExpect(jsonPath("$.classifiedQuantity").value(0.0))
				.andExpect(jsonPath("$.rewashRatePercent").value(0.0));

		mockMvc.perform(get(METRICS)
						.param("providerId", "999999")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReconcileBalancesLotsAndLedgerForSupervisor() throws Exception {
		final UserSession supervisor = authenticate("supervisor.demo", "654321");
		final UserSession operator = authenticate("operador.demo", "123456");
		final LaundryItem item = item("Conciliacion");
		final String effectiveAt = OffsetDateTime.now().minusDays(1).withNano(0).toString();

		mockMvc.perform(command(OPENING, supervisor, "reconcile-" + UUID.randomUUID(),
				openingJson(item.getId(), "8.000", effectiveAt)))
				.andExpect(status().isCreated());

		mockMvc.perform(get(RECONCILIATION)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operator.token()))
				.andExpect(status().isForbidden());

		mockMvc.perform(get(RECONCILIATION)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consistent").value(true))
				.andExpect(jsonPath("$.balanceTotal").value(8.0))
				.andExpect(jsonPath("$.lotTotal").value(8.0))
				.andExpect(jsonPath("$.ledgerTotal").value(8.0))
				.andExpect(jsonPath("$.mismatches").isEmpty());

		jdbcTemplate.update("UPDATE laundry_stock_lots SET remaining_quantity = 7.000 WHERE item_id = ?",
				item.getId());

		mockMvc.perform(get(RECONCILIATION)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consistent").value(false))
				.andExpect(jsonPath("$.mismatches[0].itemId").value(item.getId()))
				.andExpect(jsonPath("$.mismatches[0].difference").value(1.0));
	}

	@Test
	void shouldApplyAdjustmentsAndKeepLedgerInvariants() throws Exception {
		final UserSession supervisor = authenticate("supervisor.demo", "654321");
		final LaundryItem item = item("Ajustes");
		final LaundryProvider provider = provider("Proveedor ajustes");
		final OffsetDateTime effectiveAt = OffsetDateTime.now().minusDays(1).withNano(0);

		mockMvc.perform(command(
						OPENING,
						supervisor,
						"opening-" + UUID.randomUUID(),
						openingJson(item.getId(), "20.000", effectiveAt.toString())
				))
				.andExpect(status().isCreated());

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"increase-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "INCREASE", null, "MISSING", provider.getId(),
								"2.000", "Prenda extraviada", effectiveAt.toString(), 0
						)
				))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.operation").value("INCREASE"))
				.andExpect(jsonPath("$.toBucket").value("MISSING"));

		final JsonNode missingStock = read(get(STOCK)
				.param("itemId", item.getId().toString())
				.param("bucket", "MISSING")
				.param("providerId", provider.getId().toString())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()));
		final long missingVersion = missingStock.get("items").get(0).get("version").asLong();

		final String transferKey = "transfer-" + UUID.randomUUID();
		final String transferBody = adjustmentJson(
				item.getId(), "TRANSFER", "MISSING", "HOTEL_AVAILABLE", provider.getId(),
				"2.000", "Prenda encontrada", effectiveAt.toString(), missingVersion
		);
		mockMvc.perform(command(ADJUSTMENTS, supervisor, transferKey, transferBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.fromBucket").value("MISSING"))
				.andExpect(jsonPath("$.toBucket").value("HOTEL_AVAILABLE"));

		final long movementsAfterTransfer = movementRepository.count();
		mockMvc.perform(command(ADJUSTMENTS, supervisor, transferKey, transferBody))
				.andExpect(status().isCreated())
				.andExpect(header().string("Idempotent-Replay", "true"))
				.andExpect(jsonPath("$.id").isNumber());
		assertThat(movementRepository.count()).isEqualTo(movementsAfterTransfer);

		final JsonNode availableStock = read(get(STOCK)
				.param("itemId", item.getId().toString())
				.param("bucket", "HOTEL_AVAILABLE")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()));
		final long availableVersion = availableStock.get("items").get(0).get("version").asLong();

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"decrease-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "DECREASE", "HOTEL_AVAILABLE", null, null,
								"1.000", "Prenda retirada", effectiveAt.toString(), availableVersion
						)
				))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.operation").value("DECREASE"));

		mockMvc.perform(get(DASHBOARD).header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totals.hotelAvailable").value(21.0))
				.andExpect(jsonPath("$.totals.missing").value(0.0))
				.andExpect(jsonPath("$.totals.total").value(21.0));

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"stale-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "DECREASE", "HOTEL_AVAILABLE", null, null,
								"1.000", "Version vieja", effectiveAt.toString(), 0
						)
				))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith(
						"Stock balance version mismatch"
				)));

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"insufficient-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "DECREASE", "HOTEL_AVAILABLE", null, null,
								"999.000", "Sin saldo", effectiveAt.toString(), availableVersion + 1
						)
				))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith(
						"Insufficient HOTEL_AVAILABLE balance"
				)));

		assertLedgerConsistency("21.000");
	}

	@Test
	void shouldRejectInvalidAdjustmentProviderAndBucketCombinations() throws Exception {
		final UserSession supervisor = authenticate("supervisor.demo", "654321");
		final LaundryItem item = item("Combinaciones");
		final LaundryProvider provider = provider("Proveedor combinaciones");

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"hotel-provider-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "INCREASE", null, "HOTEL_AVAILABLE", provider.getId(),
								"1.000", "Combinacion invalida", OffsetDateTime.now().withNano(0).toString(), 0
						)
				))
				.andExpect(status().isBadRequest());

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"provider-missing-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "INCREASE", null, "PROVIDER_PROCESSING", null,
								"1.000", "Combinacion invalida", OffsetDateTime.now().withNano(0).toString(), 0
						)
				))
				.andExpect(status().isBadRequest());

		mockMvc.perform(command(
						ADJUSTMENTS,
						supervisor,
						"provider-unknown-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "INCREASE", null, "PROVIDER_PROCESSING", 999999L,
								"1.000", "Proveedor inexistente", OffsetDateTime.now().withNano(0).toString(), 0
						)
				))
				.andExpect(status().isNotFound());

		final UserSession operator = authenticate("operador.demo", "123456");
		mockMvc.perform(command(
						ADJUSTMENTS,
						operator,
						"operator-" + UUID.randomUUID(),
						adjustmentJson(
								item.getId(), "INCREASE", null, "HOTEL_AVAILABLE", null,
								"1.000", "Sin permiso", OffsetDateTime.now().withNano(0).toString(), 0
						)
				))
				.andExpect(status().isForbidden());
	}

	private void assertLedgerConsistency(String expectedTotal) {
		final List<LaundryStockBalance> balances = balanceRepository.findAll();
		final BigDecimal balanceTotal = balances.stream()
				.map(LaundryStockBalance::getQuantity)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		assertThat(balanceTotal).isEqualByComparingTo(expectedTotal);
		for (LaundryStockBalance balance : balances) {
			assertThat(lotRepository.sumRemaining(
					balance.getItem().getId(),
					balance.getBucket(),
					balance.getProviderScope()
			)).isEqualByComparingTo(balance.getQuantity());
		}
		assertThat(movementRepository.sumAllIncoming().subtract(movementRepository.sumAllOutgoing()))
				.isEqualByComparingTo(expectedTotal);
	}

	private UserSession authenticate(String username, String password) throws Exception {
		final String response = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return new UserSession(objectMapper.readTree(response).get("accessToken").asText());
	}

	private MockHttpServletRequestBuilder command(
			String path,
			UserSession session,
			String idempotencyKey,
			String body
	) {
		final MockHttpServletRequestBuilder request = post(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body);
		if (idempotencyKey != null) {
			request.header("Idempotency-Key", idempotencyKey);
		}
		return request;
	}

	private String openingJson(Long itemId, String quantity, String effectiveAt) {
		return """
				{"version":0,"effectiveAt":"%s","notes":"Carga inicial","lines":[{"itemId":%d,"quantity":%s}]}
				""".formatted(effectiveAt, itemId, quantity);
	}

	private String adjustmentJson(
			Long itemId,
			String operation,
			String fromBucket,
			String toBucket,
			Long providerId,
			String quantity,
			String reason,
			String effectiveAt,
			long version
	) {
		return """
				{"version":%d,"itemId":%d,"operation":"%s","fromBucket":%s,"toBucket":%s,"providerId":%s,"quantity":%s,"reason":"%s","effectiveAt":"%s"}
				""".formatted(
				version,
				itemId,
				operation,
				fromBucket == null ? "null" : "\"" + fromBucket + "\"",
				toBucket == null ? "null" : "\"" + toBucket + "\"",
				providerId == null ? "null" : providerId.toString(),
				quantity,
				reason,
				effectiveAt
		);
	}

	private JsonNode read(MockHttpServletRequestBuilder request) throws Exception {
		final String body = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body);
	}

	private LaundryItem item(String prefix) {
		return itemRepository.save(LaundryItem.create(
				prefix + " " + UUID.randomUUID().toString().substring(0, 8),
				LaundryUnitType.PIECE,
				new BigDecimal("4.50"),
				true
		));
	}

	private LaundryProvider provider(String prefix) {
		return providerRepository.save(LaundryProvider.create(
				prefix + " " + UUID.randomUUID().toString().substring(0, 8),
				"contacto",
				true
		));
	}

	private record UserSession(String token) {
	}
}
