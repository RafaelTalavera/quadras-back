package com.axioma.quadras.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.LaundryObligationStatus;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryUnitType;
import com.axioma.quadras.repository.LaundryBillableEventRepository;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryObligationRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = { "spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate" })
class LaundryReturnControllerTest {
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired JdbcTemplate jdbcTemplate;
	@Autowired LaundryItemRepository itemRepository;
	@Autowired LaundryProviderRepository providerRepository;
	@Autowired LaundryObligationRepository obligationRepository;
	@Autowired LaundryBillableEventRepository billableRepository;
	@Autowired AppUserRepository appUserRepository;
	@Autowired PasswordEncoder passwordEncoder;

	@BeforeEach
	void clean() {
		jdbcTemplate.update("DELETE FROM laundry_document_confirmations");
		jdbcTemplate.update("DELETE FROM laundry_billable_events");
		jdbcTemplate.update("DELETE FROM laundry_return_allocations");
		jdbcTemplate.update("DELETE FROM laundry_return_manifest_lines");
		jdbcTemplate.update("DELETE FROM laundry_return_manifests");
		jdbcTemplate.update("DELETE FROM laundry_rewash_dispatch_allocations");
		jdbcTemplate.update("DELETE FROM laundry_idempotency_keys");
		jdbcTemplate.update("DELETE FROM laundry_obligations");
		jdbcTemplate.update("DELETE FROM laundry_document_lines");
		jdbcTemplate.update("DELETE FROM laundry_documents");
		jdbcTemplate.update("DELETE FROM laundry_stock_adjustments");
		jdbcTemplate.update("DELETE FROM laundry_opening_stock_lines");
		jdbcTemplate.update("DELETE FROM laundry_opening_stock");
		jdbcTemplate.update("DELETE FROM laundry_stock_movements");
		jdbcTemplate.update("DELETE FROM laundry_stock_lots");
		jdbcTemplate.update("DELETE FROM laundry_stock_balances");
	}

	@Test
	void shouldCompleteMandatoryScenarioWithoutCreatingStockOrDuplicateBilling() throws Exception {
		Session supervisor = authenticate();
		LaundryItem item = item();
		LaundryProvider provider = provider();
		open(supervisor, item, "20.000");
		dispatch(supervisor, provider, item, "10.000", false);
		assertTotals(supervisor, 10, 10, 0, 0, 20);

		long firstReturn = createReturn(supervisor, provider, item, "7.000", "3.000");
		submitReturn(supervisor, firstReturn);
		String firstConfirmation = confirmBody(item.getId(), 1, "7.000", "0.000", "3.000", "0.000", "0.000", null);
		String firstResponse = mockMvc.perform(command("/api/v2/laundry/returns/" + firstReturn + "/confirm",
				supervisor, "confirm-" + UUID.randomUUID(), firstConfirmation))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andReturn().getResponse().getContentAsString();
		assertThat(objectMapper.readTree(firstResponse).get("lines").get(0).get("acceptedClean").decimalValue())
				.isEqualByComparingTo("7.000");
		assertTotals(supervisor, 17, 0, 0, 3, 20);
		assertBillable("7.000", "31.50", 1);

		long secondReturn = createReturn(supervisor, provider, item, "2.000", "1.000");
		submitReturn(supervisor, secondReturn);
		mockMvc.perform(command("/api/v2/laundry/returns/" + secondReturn + "/confirm", supervisor,
				"confirm-" + UUID.randomUUID(), confirmBody(item.getId(), 1, "2.000", "1.000",
						"0.000", "0.000", "0.000", "Proveedor declaro retenida; hotel recibio sucia")))
				.andExpect(status().isOk());
		assertTotals(supervisor, 19, 0, 1, 0, 20);
		assertBillable("9.000", "40.50", 2);

		dispatch(supervisor, provider, item, "1.000", true);
		assertTotals(supervisor, 19, 0, 0, 1, 20);
		long repeatedRewash = createReturn(supervisor, provider, item, "0.000", "1.000");
		submitReturn(supervisor, repeatedRewash);
		mockMvc.perform(command("/api/v2/laundry/returns/" + repeatedRewash + "/confirm", supervisor,
				"confirm-" + UUID.randomUUID(), confirmBody(item.getId(), 1, "0.000", "1.000",
						"0.000", "0.000", "0.000", "Hotel devolvio la prenda sucia nuevamente")))
				.andExpect(status().isOk());
		assertTotals(supervisor, 19, 0, 1, 0, 20);
		assertBillable("9.000", "40.50", 2);
		dispatch(supervisor, provider, item, "1.000", true);
		assertTotals(supervisor, 19, 0, 0, 1, 20);

		long finalReturn = createReturn(supervisor, provider, item, "1.000", "0.000");
		submitReturn(supervisor, finalReturn);
		String finalKey = "confirm-" + UUID.randomUUID();
		String finalBody = confirmBody(item.getId(), 1, "1.000", "0.000", "0.000", "0.000", "0.000", null);
		mockMvc.perform(command("/api/v2/laundry/returns/" + finalReturn + "/confirm", supervisor,
				finalKey, finalBody)).andExpect(status().isOk());
		mockMvc.perform(command("/api/v2/laundry/returns/" + finalReturn + "/confirm", supervisor,
				finalKey, finalBody)).andExpect(status().isOk())
				.andExpect(header().string("Idempotent-Replay", "true"));

		assertTotals(supervisor, 20, 0, 0, 0, 20);
		assertBillable("10.000", "45.00", 3);
		mockMvc.perform(get("/api/v2/laundry/billable-events")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token())
				.param("exportStatus", "PENDING_EXPORT").param("page", "0").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.hasNext").value(true));
		var obligation = obligationRepository.findAll().get(0);
		assertThat(obligation.getOpenQuantity()).isEqualByComparingTo("0.000");
		assertThat(obligation.getInRewashQuantity()).isEqualByComparingTo("0.000");
		assertThat(obligation.getStatus()).isEqualTo(LaundryObligationStatus.CLOSED);
	}

	@Test
	void shouldRejectCorrectionWithoutNotesAndRollbackAllChanges() throws Exception {
		Session supervisor = authenticate();
		LaundryItem item = item();
		LaundryProvider provider = provider();
		open(supervisor, item, "5.000");
		dispatch(supervisor, provider, item, "5.000", false);
		long manifest = createReturn(supervisor, provider, item, "5.000", "0.000");
		submitReturn(supervisor, manifest);
		mockMvc.perform(command("/api/v2/laundry/returns/" + manifest + "/confirm", supervisor,
				"confirm-" + UUID.randomUUID(), confirmBody(item.getId(), 1, "4.000", "1.000",
						"0.000", "0.000", "0.000", null))).andExpect(status().isBadRequest());
		assertThat(billableRepository.count()).isZero();
		assertThat(obligationRepository.findAll().get(0).getOpenQuantity()).isEqualByComparingTo("5.000");
		assertTotals(supervisor, 0, 5, 0, 0, 5);
	}

	@Test
	void shouldAllowHotelToCreateAndConfirmManualReturnAtomically() throws Exception {
		Session supervisor = authenticate();
		LaundryItem item = item();
		LaundryProvider provider = provider();
		open(supervisor, item, "3.000");
		dispatch(supervisor, provider, item, "3.000", false);
		String body = """
				{"version":0,"providerId":%d,"effectiveAt":"%s","notes":"Registro hotel","correctionNotes":null,"lines":[{"itemId":%d,"declaredClean":3.000,"declaredRetained":0.000,"acceptedClean":3.000,"returnedDirty":0.000,"retained":0.000,"missing":0.000,"damaged":0.000}]}
				""".formatted(provider.getId(), now(), item.getId());
		String key = "manual-" + UUID.randomUUID();
		String created = mockMvc.perform(command("/api/v2/laundry/returns/manual", supervisor, key, body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CONFIRMED_BY_HOTEL"))
				.andExpect(jsonPath("$.confirmedBy").value("supervisor.demo"))
				.andReturn().getResponse().getContentAsString();
		long returnId = objectMapper.readTree(created).get("id").asLong();
		mockMvc.perform(get("/api/v2/laundry/returns")
						.param("providerId", provider.getId().toString())
						.param("status", "CONFIRMED_BY_HOTEL")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(returnId))
				.andExpect(jsonPath("$.items[0].providerId").value(provider.getId()))
				.andExpect(jsonPath("$.items[0].providerName").value(provider.getName()));
		mockMvc.perform(command("/api/v2/laundry/returns/manual", supervisor, key, body))
				.andExpect(status().isCreated())
				.andExpect(header().string("Idempotent-Replay", "true"));
		assertBillable("3.000", "13.50", 1);
		assertTotals(supervisor, 3, 0, 0, 0, 3);
	}

	@Test
	void shouldReconcileMissingAndDamagedWithoutBillingAndExposeReports() throws Exception {
		Session supervisor = authenticate();
		LaundryItem item = item();
		LaundryProvider provider = provider();
		open(supervisor, item, "2.000");
		dispatch(supervisor, provider, item, "2.000", false);
		long manifest = createReturn(supervisor, provider, item, "2.000", "0.000");
		submitReturn(supervisor, manifest);
		mockMvc.perform(command("/api/v2/laundry/returns/" + manifest + "/confirm", supervisor,
				"confirm-" + UUID.randomUUID(), confirmBody(item.getId(), 1, "0.000", "0.000",
						"0.000", "1.000", "1.000", "Hotel registro faltante y dano")))
				.andExpect(status().isOk());

		assertThat(billableRepository.count()).isZero();
		mockMvc.perform(get("/api/v2/laundry/reports/missing")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token())
				.param("providerId", provider.getId().toString()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items[0].quantity").value(1.0));
		mockMvc.perform(get("/api/v2/laundry/reports/damaged")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisor.token())
				.param("providerId", provider.getId().toString()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items[0].quantity").value(1.0));
	}

	@Test
	void providerShouldCreateUpdateSubmitAndListOwnReturnWithoutInternalMetadata() throws Exception {
		Session supervisor = authenticate();
		LaundryItem item = item();
		LaundryProvider provider = provider();
		Session providerSession = providerSession(provider);
		open(supervisor, item, "4.000");
		dispatch(supervisor, provider, item, "4.000", false);
		mockMvc.perform(get("/api/v2/laundry/provider/stock")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerSession.token())
						.param("bucket", "PROVIDER_PROCESSING"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(4.0));
		mockMvc.perform(get("/api/v2/laundry/provider/stock")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerSession.token())
						.param("bucket", "HOTEL_AVAILABLE"))
				.andExpect(status().isBadRequest());
		String createBody = providerReturnBody(item.getId(), 0, "3.000", "1.000", "Declaracao");
		String created = mockMvc.perform(command("/api/v2/laundry/returns", providerSession,
				"provider-return-" + UUID.randomUUID(), createBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.providerId").value(provider.getId()))
				.andExpect(jsonPath("$.createdBy").doesNotExist())
				.andExpect(jsonPath("$.correlationId").doesNotExist())
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(created).get("id").asLong();

		mockMvc.perform(put("/api/v2/laundry/returns/" + id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerSession.token())
						.header("Idempotency-Key", "update-" + UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(providerReturnBody(item.getId(), 0, "2.000", "2.000", "Atualizada")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines[0].declaredClean").value(2.0))
				.andExpect(jsonPath("$.version").value(1));
		mockMvc.perform(get("/api/v2/laundry/provider/returns")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerSession.token())
						.param("status", "DRAFT").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(id));
		mockMvc.perform(command("/api/v2/laundry/returns/" + id + "/submit", providerSession,
				"provider-submit-" + UUID.randomUUID(), "{\"version\":1}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUBMITTED"));
		mockMvc.perform(command("/api/v2/laundry/returns/" + id + "/confirm", supervisor,
				"hotel-confirm-" + UUID.randomUUID(), confirmBody(item.getId(), 2, "2.000",
						"0.000", "2.000", "0.000", "0.000", null)))
				.andExpect(status().isOk());
		assertBillable("2.000", "9.00", 1);
	}

	@Test
	void providerReturnCommandsShouldRejectExplicitOrForeignProviderOwnership() throws Exception {
		LaundryItem item = item();
		LaundryProvider provider = provider();
		LaundryProvider other = provider();
		Session providerSession = providerSession(provider);
		Session otherSession = providerSession(other);

		String explicitProvider = """
				{"version":0,"providerId":%d,"effectiveAt":"%s","lines":[{"itemId":%d,"declaredClean":1.000,"declaredRetained":0.000}]}
				""".formatted(other.getId(), now(), item.getId());
		mockMvc.perform(command("/api/v2/laundry/returns", providerSession,
				"foreign-create-" + UUID.randomUUID(), explicitProvider))
				.andExpect(status().isBadRequest());
		String created = mockMvc.perform(command("/api/v2/laundry/returns", providerSession,
				"own-create-" + UUID.randomUUID(),
				providerReturnBody(item.getId(), 0, "1.000", "0.000", null)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(created).get("id").asLong();
		mockMvc.perform(get("/api/v2/laundry/returns/" + id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherSession.token()))
				.andExpect(status().isNotFound());
		mockMvc.perform(command("/api/v2/laundry/returns/" + id + "/submit", otherSession,
				"foreign-submit-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isNotFound());
	}

	private void assertBillable(String quantity, String amount, long count) {
		assertThat(billableRepository.count()).isEqualTo(count);
		BigDecimal totalQuantity = billableRepository.findAll().stream().map(e -> e.getQuantity())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalAmount = billableRepository.findAll().stream().map(e -> e.getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		assertThat(totalQuantity).isEqualByComparingTo(quantity);
		assertThat(totalAmount).isEqualByComparingTo(amount);
	}

	private void assertTotals(Session session, double available, double processing,
			double hotelRewash, double providerRewash, double total) throws Exception {
		mockMvc.perform(get("/api/v2/laundry/dashboard")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totals.hotelAvailable").value(available))
				.andExpect(jsonPath("$.totals.providerProcessing").value(processing))
				.andExpect(jsonPath("$.totals.hotelRewashQueue").value(hotelRewash))
				.andExpect(jsonPath("$.totals.providerRewash").value(providerRewash))
				.andExpect(jsonPath("$.totals.total").value(total));
	}

	private long createReturn(Session session, LaundryProvider provider, LaundryItem item,
			String clean, String retained) throws Exception {
		String body = """
				{"version":0,"providerId":%d,"effectiveAt":"%s","lines":[{"itemId":%d,"declaredClean":%s,"declaredRetained":%s}]}
				""".formatted(provider.getId(), now(), item.getId(), clean, retained);
		String response = mockMvc.perform(command("/api/v2/laundry/returns", session,
				"return-" + UUID.randomUUID(), body)).andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).get("id").asLong();
	}

	private void submitReturn(Session session, long id) throws Exception {
		mockMvc.perform(command("/api/v2/laundry/returns/" + id + "/submit", session,
				"submit-return-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
	}

	private String confirmBody(Long itemId, long version, String clean, String dirty,
			String retained, String missing, String damaged, String notes) {
		return """
				{"version":%d,"correctionNotes":%s,"lines":[{"itemId":%d,"acceptedClean":%s,"returnedDirty":%s,"retained":%s,"missing":%s,"damaged":%s}]}
				""".formatted(version, notes == null ? "null" : "\"" + notes + "\"", itemId,
				clean, dirty, retained, missing, damaged);
	}

	private void dispatch(Session session, LaundryProvider provider, LaundryItem item,
			String quantity, boolean rewash) throws Exception {
		String path = rewash ? "/api/v2/laundry/rewash-dispatches" : "/api/v2/laundry/dispatches";
		String body = """
				{"version":0,"providerId":%d,"effectiveAt":"%s","lines":[{"itemId":%d,"quantity":%s}]}
				""".formatted(provider.getId(), now(), item.getId(), quantity);
		String response = mockMvc.perform(command(path, session, "dispatch-" + UUID.randomUUID(), body))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(response).get("id").asLong();
		mockMvc.perform(command("/api/v2/laundry/documents/" + id + "/submit", session,
				"submit-dispatch-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isOk());
	}

	private void open(Session session, LaundryItem item, String quantity) throws Exception {
		String body = """
				{"version":0,"effectiveAt":"%s","lines":[{"itemId":%d,"quantity":%s}]}
				""".formatted(now(), item.getId(), quantity);
		mockMvc.perform(command("/api/v2/laundry/opening-stock", session,
				"opening-" + UUID.randomUUID(), body)).andExpect(status().isCreated());
	}

	private Session authenticate() throws Exception {
		String response = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"supervisor.demo\",\"password\":\"654321\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new Session(objectMapper.readTree(response).get("accessToken").asText());
	}
	private Session providerSession(LaundryProvider provider) throws Exception {
		String username = "return.provider." + UUID.randomUUID().toString().substring(0, 8);
		appUserRepository.save(AppUser.create(username, passwordEncoder.encode("123456"),
				AppUserRole.PROVIDER, provider, true));
		String response = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"123456\"}".formatted(username)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new Session(objectMapper.readTree(response).get("accessToken").asText());
	}
	private String providerReturnBody(Long itemId, long version, String clean,
			String retained, String notes) {
		return """
				{"version":%d,"effectiveAt":"%s","notes":%s,"lines":[{"itemId":%d,"declaredClean":%s,"declaredRetained":%s}]}
				""".formatted(version, now(), notes == null ? "null" : "\"" + notes + "\"",
				itemId, clean, retained);
	}

	private MockHttpServletRequestBuilder command(String path, Session session, String key, String body) {
		return post(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token())
				.header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body);
	}
	private String now() { return OffsetDateTime.now().minusMinutes(1).withNano(0).toString(); }
	private LaundryItem item() { return itemRepository.save(LaundryItem.create("Toalla " + UUID.randomUUID().toString().substring(0, 6), LaundryUnitType.PIECE, new BigDecimal("4.50"), true)); }
	private LaundryProvider provider() { return providerRepository.save(LaundryProvider.create("Lavanderia " + UUID.randomUUID().toString().substring(0, 6), null, true)); }
	private record Session(String token) { }
}
