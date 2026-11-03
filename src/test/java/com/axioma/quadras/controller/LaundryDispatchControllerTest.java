package com.axioma.quadras.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryUnitType;
import com.axioma.quadras.repository.LaundryDocumentRepository;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryObligationRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryStockMovementRepository;
import com.axioma.quadras.repository.LaundryRewashDispatchAllocationRepository;
import com.axioma.quadras.repository.AppUserRepository;
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
class LaundryDispatchControllerTest {
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired JdbcTemplate jdbcTemplate;
	@Autowired LaundryItemRepository itemRepository;
	@Autowired LaundryProviderRepository providerRepository;
	@Autowired LaundryDocumentRepository documentRepository;
	@Autowired LaundryObligationRepository obligationRepository;
	@Autowired LaundryStockMovementRepository movementRepository;
	@Autowired LaundryRewashDispatchAllocationRepository rewashAllocationRepository;
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
	void shouldCreateDraftThenSubmitAtomicallyAndReplay() throws Exception {
		Session supervisor = authenticate("supervisor.demo", "654321");
		LaundryItem item = item("Toalla");
		LaundryProvider provider = provider("Lavanderia");
		open(supervisor, item, "20.000");
		long openingMovements = movementRepository.count();

		String createKey = "create-" + UUID.randomUUID();
		String createBody = dispatchJson(provider.getId(), item.getId(), "10.000");
		String created = mockMvc.perform(command("/api/v2/laundry/dispatches", supervisor,
				createKey, createBody)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.documentType").value("STANDARD_DISPATCH"))
				.andExpect(jsonPath("$.lines[0].unitPrice").value(4.5))
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(created).get("id").asLong();
		assertThat(movementRepository.count()).isEqualTo(openingMovements);
		assertThat(obligationRepository.count()).isZero();

		String submitKey = "submit-" + UUID.randomUUID();
		String submitted = mockMvc.perform(command("/api/v2/laundry/documents/" + id + "/submit",
				supervisor, submitKey, "{\"version\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUBMITTED"))
				.andExpect(jsonPath("$.version").value(1))
				.andReturn().getResponse().getContentAsString();

		assertThat(obligationRepository.count()).isEqualTo(1);
		var obligation = obligationRepository.findAll().get(0);
		assertThat(obligation.getOpenQuantity()).isEqualByComparingTo("10.000");
		assertThat(obligation.getUnitPrice()).isEqualByComparingTo("4.50");
		assertThat(movementRepository.count()).isEqualTo(openingMovements + 1);
		assertStock(supervisor, item.getId(), "HOTEL_AVAILABLE", null, 10.0);
		assertStock(supervisor, item.getId(), "PROVIDER_PROCESSING", provider.getId(), 10.0);

		mockMvc.perform(command("/api/v2/laundry/documents/" + id + "/submit", supervisor,
				submitKey, "{\"version\":0}"))
				.andExpect(status().isOk())
				.andExpect(header().string("Idempotent-Replay", "true"))
				.andExpect(jsonPath("$.id").value(objectMapper.readTree(submitted).get("id").asLong()));
		assertThat(obligationRepository.count()).isEqualTo(1);
		assertThat(movementRepository.count()).isEqualTo(openingMovements + 1);
		mockMvc.perform(command("/api/v2/laundry/documents/" + id + "/submit", supervisor,
				"stale-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith(
						"Laundry document version mismatch")));
	}

	@Test
	void shouldRollbackSubmitWhenAnyLineHasInsufficientStock() throws Exception {
		Session supervisor = authenticate("supervisor.demo", "654321");
		LaundryItem item = item("Sabana");
		LaundryProvider provider = provider("Proveedor");
		open(supervisor, item, "5.000");
		String created = mockMvc.perform(command("/api/v2/laundry/dispatches", supervisor,
				"create-" + UUID.randomUUID(), dispatchJson(provider.getId(), item.getId(), "6.000")))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(created).get("id").asLong();
		long movements = movementRepository.count();

		mockMvc.perform(command("/api/v2/laundry/documents/" + id + "/submit", supervisor,
				"submit-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isConflict());

		assertThat(documentRepository.findById(id).orElseThrow().getStatus().name()).isEqualTo("DRAFT");
		assertThat(obligationRepository.count()).isZero();
		assertThat(movementRepository.count()).isEqualTo(movements);
		assertStock(supervisor, item.getId(), "HOTEL_AVAILABLE", null, 5.0);
	}

	@Test
	void shouldDispatchRewashAgainstOldestEligibleObligationAtZeroPrice() throws Exception {
		Session supervisor = authenticate("supervisor.demo", "654321");
		LaundryItem item = item("Funda");
		LaundryProvider provider = provider("Lavado");
		open(supervisor, item, "10.000");
		String standard = mockMvc.perform(command("/api/v2/laundry/dispatches", supervisor,
				"standard-" + UUID.randomUUID(), dispatchJson(provider.getId(), item.getId(), "2.000")))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long standardId = objectMapper.readTree(standard).get("id").asLong();
		mockMvc.perform(command("/api/v2/laundry/documents/" + standardId + "/submit", supervisor,
				"standard-submit-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isOk());
		String second = mockMvc.perform(command("/api/v2/laundry/dispatches", supervisor,
				"standard-" + UUID.randomUUID(), dispatchJson(provider.getId(), item.getId(), "4.000")))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long secondId = objectMapper.readTree(second).get("id").asLong();
		mockMvc.perform(command("/api/v2/laundry/documents/" + secondId + "/submit", supervisor,
				"standard-submit-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isOk());

		JsonNode processing = stock(supervisor, item.getId(), "PROVIDER_PROCESSING", provider.getId());
		long version = processing.get("items").get(0).get("version").asLong();
		String adjustment = """
				{"version":%d,"itemId":%d,"operation":"TRANSFER","fromBucket":"PROVIDER_PROCESSING","toBucket":"HOTEL_REWASH_QUEUE","providerId":%d,"quantity":4.000,"reason":"Rechazo operativo","effectiveAt":"%s"}
				""".formatted(version, item.getId(), provider.getId(), OffsetDateTime.now().withNano(0));
		mockMvc.perform(command("/api/v2/laundry/stock-adjustments", supervisor,
				"queue-" + UUID.randomUUID(), adjustment)).andExpect(status().isCreated());
		var obligations = obligationRepository.findAll().stream()
				.sorted(java.util.Comparator.comparing(com.axioma.quadras.domain.model.LaundryObligation::getId))
				.toList();
		obligations.get(0).markInRewash(new BigDecimal("2.000"));
		obligations.get(1).markInRewash(new BigDecimal("2.000"));
		obligationRepository.saveAllAndFlush(obligations);

		String rewash = mockMvc.perform(command("/api/v2/laundry/rewash-dispatches", supervisor,
				"rewash-" + UUID.randomUUID(), dispatchJson(provider.getId(), item.getId(), "3.000")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.documentType").value("REWASH_DISPATCH"))
				.andExpect(jsonPath("$.lines[0].unitPrice").value(0.0))
				.andReturn().getResponse().getContentAsString();
		long rewashId = objectMapper.readTree(rewash).get("id").asLong();
		mockMvc.perform(command("/api/v2/laundry/documents/" + rewashId + "/submit", supervisor,
				"rewash-submit-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUBMITTED"));

		assertThat(rewashAllocationRepository.count()).isEqualTo(2);
		var allocations = rewashAllocationRepository.findAll().stream()
				.sorted(java.util.Comparator.comparing(com.axioma.quadras.domain.model.LaundryRewashDispatchAllocation::getId))
				.toList();
		assertThat(allocations.get(0).getObligation().getId()).isEqualTo(obligations.get(0).getId());
		assertThat(allocations.get(0).getQuantity()).isEqualByComparingTo("2.000");
		assertThat(allocations.get(1).getObligation().getId()).isEqualTo(obligations.get(1).getId());
		assertThat(allocations.get(1).getQuantity()).isEqualByComparingTo("1.000");
		assertStock(supervisor, item.getId(), "HOTEL_REWASH_QUEUE", provider.getId(), 1.0);
		assertStock(supervisor, item.getId(), "PROVIDER_REWASH", provider.getId(), 3.0);
		assertThat(obligationRepository.count()).isEqualTo(2);
	}

	@Test
	void providerShouldConfirmOwnedDispatchWithoutSeeingPricesAndReplaySafely() throws Exception {
		Session supervisor = authenticate("supervisor.demo", "654321");
		LaundryItem item = item("Roupao");
		LaundryProvider provider = provider("Portal");
		LaundryProvider otherProvider = provider("Outro");
		Session providerSession = providerSession(provider);
		Session otherSession = providerSession(otherProvider);
		open(supervisor, item, "3.000");
		long documentId = createAndSubmit(supervisor, provider, item, "3.000");
		long movements = movementRepository.count();

		mockMvc.perform(get("/api/v2/laundry/documents/" + documentId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerSession.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.providerId").value(provider.getId()))
				.andExpect(jsonPath("$.lines[0].quantity").value(3.0))
				.andExpect(jsonPath("$.lines[0].unitPrice").doesNotExist())
				.andExpect(jsonPath("$.correlationId").doesNotExist())
				.andExpect(jsonPath("$.createdBy").doesNotExist());
		mockMvc.perform(get("/api/v2/laundry/documents/" + documentId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherSession.token()))
				.andExpect(status().isNotFound());

		String key = "ack-" + UUID.randomUUID();
		String body = "{\"version\":1,\"notes\":\"Recebido\"}";
		mockMvc.perform(command("/api/v2/laundry/dispatches/" + documentId + "/acknowledgements",
				providerSession, key, body)).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andExpect(jsonPath("$.confirmations[0].action").value("PROVIDER_CONFIRMED"));
		mockMvc.perform(command("/api/v2/laundry/dispatches/" + documentId + "/acknowledgements",
				providerSession, key, body)).andExpect(status().isOk())
				.andExpect(header().string("Idempotent-Replay", "true"));
		assertThat(movementRepository.count()).isEqualTo(movements);
	}

	@Test
	void providerObservationShouldRequireOwnershipAndHotelResolutionShouldNotMoveStock() throws Exception {
		Session supervisor = authenticate("supervisor.demo", "654321");
		LaundryItem item = item("Lencol");
		LaundryProvider provider = provider("Observacao");
		LaundryProvider otherProvider = provider("Sem acesso");
		Session providerSession = providerSession(provider);
		Session otherSession = providerSession(otherProvider);
		open(supervisor, item, "2.000");
		long documentId = createAndSubmit(supervisor, provider, item, "2.000");
		long movements = movementRepository.count();

		mockMvc.perform(command("/api/v2/laundry/documents/" + documentId + "/observations",
				otherSession, "obs-" + UUID.randomUUID(),
				"{\"version\":1,\"notes\":\"Documento alheio\"}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(command("/api/v2/laundry/documents/" + documentId + "/observations",
				providerSession, "obs-" + UUID.randomUUID(),
				"{\"version\":1,\"notes\":\"Quantidade divergente\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("OBSERVED"));
		mockMvc.perform(command("/api/v2/laundry/discrepancies/" + documentId + "/resolve",
				supervisor, "resolve-" + UUID.randomUUID(),
				"{\"version\":2,\"notes\":\"Conferido fisicamente pelo hotel\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RESOLVED"));
		assertThat(movementRepository.count()).isEqualTo(movements);
	}

	private void assertStock(Session session, Long itemId, String bucket, Long providerId,
			double quantity) throws Exception {
		MockHttpServletRequestBuilder request = get("/api/v2/laundry/stock")
				.param("itemId", itemId.toString()).param("bucket", bucket)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token());
		if (providerId != null) request.param("providerId", providerId.toString());
		mockMvc.perform(request).andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(quantity));
	}

	private JsonNode stock(Session session, Long itemId, String bucket, Long providerId) throws Exception {
		MockHttpServletRequestBuilder request = get("/api/v2/laundry/stock")
				.param("itemId", itemId.toString()).param("bucket", bucket)
				.param("providerId", providerId.toString())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token());
		return objectMapper.readTree(mockMvc.perform(request).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString());
	}

	private void open(Session session, LaundryItem item, String quantity) throws Exception {
		String body = """
				{"version":0,"effectiveAt":"%s","lines":[{"itemId":%d,"quantity":%s}]}
				""".formatted(OffsetDateTime.now().minusDays(1).withNano(0), item.getId(), quantity);
		mockMvc.perform(command("/api/v2/laundry/opening-stock", session,
				"open-" + UUID.randomUUID(), body)).andExpect(status().isCreated());
	}

	private long createAndSubmit(Session supervisor, LaundryProvider provider,
			LaundryItem item, String quantity) throws Exception {
		String created = mockMvc.perform(command("/api/v2/laundry/dispatches", supervisor,
				"create-" + UUID.randomUUID(), dispatchJson(provider.getId(), item.getId(), quantity)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(created).get("id").asLong();
		mockMvc.perform(command("/api/v2/laundry/documents/" + id + "/submit", supervisor,
				"submit-" + UUID.randomUUID(), "{\"version\":0}"))
				.andExpect(status().isOk());
		return id;
	}

	private Session providerSession(LaundryProvider provider) throws Exception {
		String username = "provider." + UUID.randomUUID().toString().substring(0, 8);
		appUserRepository.save(AppUser.create(username, passwordEncoder.encode("123456"),
				AppUserRole.PROVIDER, provider, true));
		return authenticate(username, "123456");
	}

	private String dispatchJson(Long providerId, Long itemId, String quantity) {
		return """
				{"version":0,"providerId":%d,"effectiveAt":"%s","notes":"Envio","lines":[{"itemId":%d,"quantity":%s}]}
				""".formatted(providerId, OffsetDateTime.now().withNano(0), itemId, quantity);
	}

	private MockHttpServletRequestBuilder command(String path, Session session, String key, String body) {
		return post(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token())
				.header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body);
	}

	private Session authenticate(String username, String password) throws Exception {
		String response = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new Session(objectMapper.readTree(response).get("accessToken").asText());
	}

	private LaundryItem item(String prefix) {
		return itemRepository.save(LaundryItem.create(prefix + " " + UUID.randomUUID().toString().substring(0, 6),
				LaundryUnitType.PIECE, new BigDecimal("4.50"), true));
	}

	private LaundryProvider provider(String prefix) {
		return providerRepository.save(LaundryProvider.create(prefix + " "
				+ UUID.randomUUID().toString().substring(0, 6), null, true));
	}

	private record Session(String token) { }
}
