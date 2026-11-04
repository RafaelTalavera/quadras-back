package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryShipment;
import com.axioma.quadras.domain.model.LaundryShipmentKind;
import com.axioma.quadras.domain.model.LaundryShipmentLine;
import com.axioma.quadras.domain.model.LaundryUnitType;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryShipmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
class LaundryControllerTest {
	@Autowired private MockMvc mockMvc;
	@Autowired private LaundryProviderRepository providerRepository;
	@Autowired private LaundryItemRepository itemRepository;
	@Autowired private LaundryShipmentRepository shipmentRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldCreateAndDeactivateLaundryProvider() throws Exception {
		String token = authenticate();
		String name = unique("Proveedor administrable");
		String createdBody = mockMvc.perform(post("/api/v2/laundry/providers")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"%s","contact":"+55 11 99999-0000","active":true}
						""".formatted(name)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.active").value(true))
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(createdBody).get("id").asLong();

		mockMvc.perform(put("/api/v2/laundry/providers/{id}", id)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"%s","contact":"contato atualizado","active":false}
						""".formatted(name)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contact").value("contato atualizado"))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void shouldAcceptPastSentAtAndPreserveItsInstantAndOffset() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Pasado");
		LaundryItem item = item("Pasado");
		OffsetDateTime sentAt = OffsetDateTime.parse("2026-10-29T17:30:00+05:30");

		String createdBody = createShipment(token, provider.getId(), item.getId(), sentAt)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		JsonNode created = objectMapper.readTree(createdBody);
		OffsetDateTime createdSentAt = OffsetDateTime.parse(created.get("sentAt").asText());
		org.assertj.core.api.Assertions.assertThat(createdSentAt.toInstant()).isEqualTo(sentAt.toInstant());
		long shipmentId = created.get("id").asLong();

		String persistedBody = mockMvc.perform(get("/api/v1/laundry/shipments/{id}", shipmentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		OffsetDateTime persistedSentAt = OffsetDateTime.parse(objectMapper.readTree(persistedBody).get("sentAt").asText());
		org.assertj.core.api.Assertions.assertThat(persistedSentAt.toInstant()).isEqualTo(sentAt.toInstant());
	}

	@Test
	void shouldAcceptCurrentSentAtWithoutReplacingClientValue() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Actual");
		LaundryItem item = item("Actual");
		OffsetDateTime sentAt = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

		String createdBody = createShipment(token, provider.getId(), item.getId(), sentAt)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		OffsetDateTime createdSentAt = OffsetDateTime.parse(objectMapper.readTree(createdBody).get("sentAt").asText());
		org.assertj.core.api.Assertions.assertThat(createdSentAt.toInstant()).isEqualTo(sentAt.toInstant());
	}

	@Test
	void shouldKeepAcceptingFutureSentAt() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Futuro");
		LaundryItem item = item("Futuro");
		OffsetDateTime sentAt = OffsetDateTime.parse("2099-12-31T23:59:00-03:00");

		String createdBody = createShipment(token, provider.getId(), item.getId(), sentAt)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		OffsetDateTime createdSentAt = OffsetDateTime.parse(objectMapper.readTree(createdBody).get("sentAt").asText());
		org.assertj.core.api.Assertions.assertThat(createdSentAt.toInstant()).isEqualTo(sentAt.toInstant());
	}

	@Test
	void shouldRejectNullSentAtWithCommonBadRequestFormat() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Fecha nula");
		LaundryItem item = item("Fecha nula");

		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(shipmentPayload(provider.getId(), item.getId(), "null")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/v1/laundry/shipments"));
	}

	@Test
	void shouldRejectMalformedSentAtWithCommonBadRequestFormat() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Fecha invalida");
		LaundryItem item = item("Fecha invalida");

		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(shipmentPayload(provider.getId(), item.getId(), "\"ayer al mediodia\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/v1/laundry/shipments"));
	}

	@Test
	void shouldRequireOperatorAuthenticationToCreateShipment() throws Exception {
		LaundryProvider provider = provider("Sin autenticar");
		LaundryItem item = item("Sin autenticar");

		mockMvc.perform(post("/api/v1/laundry/shipments")
					.contentType(MediaType.APPLICATION_JSON)
					.content(shipmentPayload(provider.getId(), item.getId(), "\"2026-10-29T12:00:00Z\"")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldCreateAndUpdateLaundryItem() throws Exception {
		String token = authenticate();
		String createdBody = mockMvc.perform(post("/api/v1/laundry/items")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"Alfombra premium","unitType":"PIECE","currentUnitPrice":7.25,"active":true}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.currentUnitPrice").value(7.25))
				.andReturn().getResponse().getContentAsString();
		long itemId = objectMapper.readTree(createdBody).get("id").asLong();

		mockMvc.perform(put("/api/v1/laundry/items/{id}", itemId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"Alfombra premium editada","unitType":"PIECE","currentUnitPrice":8.50,"active":false}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(itemId))
				.andExpect(jsonPath("$.name").value("Alfombra premium editada"))
				.andExpect(jsonPath("$.currentUnitPrice").value(8.5))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void shouldRejectDuplicateLaundryItemNameWhenUpdating() throws Exception {
		String token = authenticate();
		itemRepository.save(LaundryItem.create("Alfombra duplicada A", LaundryUnitType.PIECE, new BigDecimal("4.00"), true));
		LaundryItem item = itemRepository.save(LaundryItem.create("Alfombra duplicada B", LaundryUnitType.PIECE, new BigDecimal("5.00"), true));

		mockMvc.perform(put("/api/v1/laundry/items/{id}", item.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"alfombra duplicada a","unitType":"PIECE","currentUnitPrice":6.00,"active":true}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingUnknownLaundryItem() throws Exception {
		String token = authenticate();
		mockMvc.perform(put("/api/v1/laundry/items/{id}", 999999L)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"Inexistente","unitType":"PIECE","currentUnitPrice":1.00,"active":true}
						"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldCreateRewashAndReceiptAsIndependentTransactions() throws Exception {
		String token = authenticate();
		LaundryProvider provider = providerRepository.save(LaundryProvider.create("Lavanderia teste", null, true));
		LaundryItem item = itemRepository.save(LaundryItem.create("Toalla teste", LaundryUnitType.PIECE, new BigDecimal("4.50"), true));
		String rewashPayload = """
			{
			  "providerId": %d,
			  "kind": "REWASH",
			  "sentAt": "2026-10-30T12:00:00Z",
			  "lines": [{"itemId": %d, "quantity": 10}]
			}
			""".formatted(provider.getId(), item.getId());
		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON).content(rewashPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.kind").value("REWASH"))
				.andExpect(jsonPath("$.status").value("SENT"))
				.andExpect(jsonPath("$.billableAmount").value(0.0))
				.andExpect(jsonPath("$.lines[0].originLineId").doesNotExist());

		String receiptPayload = """
			{
			  "providerId": %d,
			  "kind": "RECEIPT",
			  "sentAt": "2026-10-31T09:30:00Z",
			  "lines": [{"itemId": %d, "quantity": 6}]
			}
			""".formatted(provider.getId(), item.getId());
		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON).content(receiptPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.kind").value("RECEIPT"))
				.andExpect(jsonPath("$.status").value("RECEIVED"))
				.andExpect(jsonPath("$.sentAt").doesNotExist())
				.andExpect(jsonPath("$.receivedAt").value("2026-10-31T09:30:00Z"))
				.andExpect(jsonPath("$.billableAmount").value(0.0))
				.andExpect(jsonPath("$.lines[0].acceptedQuantity").value(6.0))
				.andExpect(jsonPath("$.lines[0].pendingQuantity").value(0.0));

		String standardPayload = """
			{
			  "providerId": %d,
			  "sentAt": "2026-10-30T12:00:00Z",
			  "lines": [{"itemId": %d, "quantity": 10}]
			}
			""".formatted(provider.getId(), item.getId());
		String createdBody = mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON).content(standardPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.kind").value("STANDARD"))
				.andExpect(jsonPath("$.billableAmount").value(45.0))
				.andReturn().getResponse().getContentAsString();
		JsonNode created = objectMapper.readTree(createdBody);
		long shipmentId = created.get("id").asLong();
		long lineId = created.get("lines").get(0).get("id").asLong();
		long version = created.get("version").asLong();

		String receivePayload = """
			{
			  "receivedAt": "2026-10-31T12:00:00Z",
			  "version": %d,
			  "lines": [{
			    "lineId": %d,
			    "acceptedQuantity": 10,
			    "rewashQuantity": 0,
			    "missingQuantity": 0,
			    "damagedQuantity": 0
			  }]
			}
			""".formatted(version, lineId);
		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/receive", shipmentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON).content(receivePayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.shipment.status").value("RECEIVED"))
				.andExpect(jsonPath("$.generatedRewashShipmentId").doesNotExist());
		mockMvc.perform(get("/api/v1/laundry/shipments/{id}/audit", shipmentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.actionName == 'RECEIVE_SHIPMENT')]").exists());
	}

	@Test
	void shouldSummarizeDailyLaundryMovementsAndTotals() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Resumen");
		LaundryItem item = item("Resumen");

		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"providerId":%d,"kind":"STANDARD","sentAt":"2098-01-10T14:00:00Z",
						 "lines":[{"itemId":%d,"quantity":2}]}
						""".formatted(provider.getId(), item.getId())))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"providerId":%d,"kind":"REWASH","sentAt":"2098-01-10T18:00:00Z",
						 "lines":[{"itemId":%d,"quantity":3}]}
						""".formatted(provider.getId(), item.getId())))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"providerId":%d,"kind":"RECEIPT","sentAt":"2098-01-11T09:00:00Z",
						 "lines":[{"itemId":%d,"quantity":4}]}
						""".formatted(provider.getId(), item.getId())))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/laundry/summary")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.param("providerId", provider.getId().toString())
					.param("dateFrom", "2098-01-10")
					.param("dateTo", "2098-01-11"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rows.length()").value(2))
				.andExpect(jsonPath("$.rows[0].date").value("2098-01-11"))
				.andExpect(jsonPath("$.rows[0].receivedQuantity").value(4.0))
				.andExpect(jsonPath("$.rows[0].sentQuantity").value(0))
				.andExpect(jsonPath("$.rows[0].rewashedQuantity").value(0))
				.andExpect(jsonPath("$.rows[1].date").value("2098-01-10"))
				.andExpect(jsonPath("$.rows[1].sentQuantity").value(2.0))
				.andExpect(jsonPath("$.rows[1].rewashedQuantity").value(3.0))
				.andExpect(jsonPath("$.totalReceivedQuantity").value(4.0))
				.andExpect(jsonPath("$.totalSentQuantity").value(2.0))
				.andExpect(jsonPath("$.totalRewashedQuantity").value(3.0));
	}

	@Test
	void shouldRejectInvalidLaundrySummaryDateRange() throws Exception {
		mockMvc.perform(get("/api/v1/laundry/summary")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
					.param("dateFrom", "2026-11-02")
					.param("dateTo", "2026-11-01"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectDispatchForStandardShipmentAndInvalidPayload() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Dispatch standard");
		LaundryItem item = item("Dispatch standard");
		JsonNode created = objectMapper.readTree(createShipment(
				token, provider.getId(), item.getId(), OffsetDateTime.parse("2026-10-30T12:00:00Z"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/dispatch", created.get("id").asLong())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"sentAt\":\"2026-10-30T18:00:00Z\",\"version\":0}"))
				.andExpect(status().isConflict());
		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/dispatch", created.get("id").asLong())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"sentAt\":null,\"version\":-1}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectInvalidReceiptClassificationsAndRequireRewashReason() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Validaciones recepcion");
		LaundryItem item = item("Validaciones recepcion");
		JsonNode created = objectMapper.readTree(createShipment(
				token, provider.getId(), item.getId(), OffsetDateTime.parse("2026-10-30T12:00:00Z"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
		long shipmentId = created.get("id").asLong();
		long lineId = created.get("lines").get(0).get("id").asLong();

		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/receive", shipmentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"receivedAt":"2026-10-31T12:00:00Z","version":0,"lines":[{
					 "lineId":%d,"acceptedQuantity":3,"rewashQuantity":0,
					 "missingQuantity":0,"damagedQuantity":0
					}]}
					""".formatted(lineId)))
				.andExpect(status().isConflict());

		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/receive", shipmentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"receivedAt":"2026-10-31T12:00:00Z","version":0,"lines":[{
					 "lineId":%d,"acceptedQuantity":0,"rewashQuantity":1,
					 "missingQuantity":0,"damagedQuantity":0
					}]}
					""".formatted(lineId)))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/receive", shipmentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldRejectReceiptAndAllowCancellationWhileRewashIsPending() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Pendiente");
		LaundryItem item = item("Pendiente");
		LaundryShipment source = LaundryShipment.create(
				"LAV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), provider,
				LaundryShipmentKind.STANDARD, OffsetDateTime.parse("2026-10-30T12:00:00Z"), null, "operador.demo"
		);
		source.addLine(LaundryShipmentLine.standard(item, BigDecimal.ONE));
		source = shipmentRepository.saveAndFlush(source);
		LaundryShipment pending = LaundryShipment.createPendingRewash(
				"LAV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), provider,
				"Pendiente", "operador.demo"
		);
		pending.addLine(LaundryShipmentLine.rewash(
				source.getLines().get(0), BigDecimal.ONE,
				com.axioma.quadras.domain.model.LaundryRewashReason.INCOMPLETE_WASH
		));
		pending = shipmentRepository.saveAndFlush(pending);
		long pendingId = pending.getId();
		long pendingLineId = pending.getLines().get(0).getId();

		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/receive", pendingId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"receivedAt":"2026-11-02T12:00:00Z","version":0,"lines":[{
						  "lineId":%d,"acceptedQuantity":1,"rewashQuantity":0,
						  "missingQuantity":0,"damagedQuantity":0
						}]}
						""".formatted(pendingLineId)))
				.andExpect(status().isConflict());

		mockMvc.perform(patch("/api/v1/laundry/shipments/{id}/cancel", pendingId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"cancellationNotes\":\"Retiro cancelado\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"))
				.andExpect(jsonPath("$.cancellationNotes").value("Retiro cancelado"));
	}

	@Test
	void shouldRollbackReceiptClassificationWhenRewashCreationFlowFails() throws Exception {
		String token = authenticate();
		LaundryProvider provider = provider("Atomica");
		LaundryItem item = item("Atomica");
		JsonNode created = objectMapper.readTree(createShipment(
				token, provider.getId(), item.getId(), OffsetDateTime.parse("2026-10-30T12:00:00Z"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
		long shipmentId = created.get("id").asLong();
		long lineId = created.get("lines").get(0).get("id").asLong();
		long rewashCountBefore = shipmentRepository.findAll().stream()
				.filter(value -> value.getKind() == LaundryShipmentKind.REWASH).count();

		mockMvc.perform(post("/api/v1/laundry/shipments/{id}/receive", shipmentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"receivedAt":"2026-10-31T12:00:00Z","version":0,"lines":[
						 {"lineId":%d,"acceptedQuantity":0,"rewashQuantity":1,"missingQuantity":0,"damagedQuantity":0,"rewashReason":"INCOMPLETE_WASH"},
						 {"lineId":999999,"acceptedQuantity":1,"rewashQuantity":0,"missingQuantity":0,"damagedQuantity":0}
						]}
						""".formatted(lineId)))
				.andExpect(status().isBadRequest());

		long rewashCountAfter = shipmentRepository.findAll().stream()
				.filter(value -> value.getKind() == LaundryShipmentKind.REWASH).count();
		LaundryShipment reloaded = shipmentRepository.findDetailedById(shipmentId).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(rewashCountAfter).isEqualTo(rewashCountBefore);
		org.assertj.core.api.Assertions.assertThat(reloaded.getLines().get(0).getRewashQuantity())
				.isEqualByComparingTo("0.000");
	}

	private String authenticate() throws Exception {
		String response = mockMvc.perform(post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"operador.demo\",\"password\":\"123456\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).get("accessToken").asText();
	}

	private LaundryProvider provider(String prefix) {
		return providerRepository.save(LaundryProvider.create(unique(prefix), null, true));
	}

	private LaundryItem item(String prefix) {
		return itemRepository.save(LaundryItem.create(unique(prefix), LaundryUnitType.PIECE, new BigDecimal("4.50"), true));
	}

	private org.springframework.test.web.servlet.ResultActions createShipment(
			String token, Long providerId, Long itemId, OffsetDateTime sentAt
	) throws Exception {
		return mockMvc.perform(post("/api/v1/laundry/shipments")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(shipmentPayload(providerId, itemId, "\"" + sentAt + "\"")));
	}

	private String shipmentPayload(Long providerId, Long itemId, String sentAtJson) {
		return """
			{
			  "providerId": %d,
			  "sentAt": %s,
			  "notes": "Registro de contrato temporal",
			  "lines": [{"itemId": %d, "quantity": 2}]
			}
			""".formatted(providerId, sentAtJson, itemId);
	}

	private String unique(String prefix) {
		return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
	}
}
