package com.axioma.quadras.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryShipment;
import com.axioma.quadras.domain.model.LaundryShipmentLine;
import com.axioma.quadras.domain.model.LaundryUnitType;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryShipmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
class LaundryV1CompatibilitySnapshotTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private LaundryProviderRepository providerRepository;
	@Autowired private LaundryItemRepository itemRepository;
	@Autowired private LaundryShipmentRepository shipmentRepository;

	private JsonNode contract;

	@BeforeEach
	void loadContract() throws Exception {
		try (InputStream input = new ClassPathResource("laundry/v1-contract.json").getInputStream()) {
			contract = objectMapper.readTree(input);
		}
	}

	@Test
	void shouldKeepV1ProviderItemShipmentSummaryAuditAndErrorShapes() throws Exception {
		final String token = authenticate();
		final LaundryProvider provider = providerRepository.save(
				LaundryProvider.create(unique("Contrato proveedor"), "contacto", true)
		);
		final LaundryItem item = itemRepository.save(
				LaundryItem.create(unique("Contrato prenda"), LaundryUnitType.PIECE, new BigDecimal("4.50"), true)
		);

		final String providerBody = mockMvc.perform(post("/api/v1/laundry/providers")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"%s","contact":"contacto","active":true}
						""".formatted(unique("Contrato nuevo proveedor"))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		final JsonNode createdProvider = objectMapper.readTree(providerBody);
		assertOnlyContractFields(createdProvider, "provider");

		final String itemBody = mockMvc.perform(post("/api/v1/laundry/items")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"name":"%s","unitType":"PIECE","currentUnitPrice":4.50,"active":true}
						""".formatted(unique("Contrato nueva prenda"))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		final JsonNode createdItem = objectMapper.readTree(itemBody);
		assertOnlyContractFields(createdItem, "item");

		final String shipmentBody = mockMvc.perform(post("/api/v1/laundry/shipments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"providerId":%d,"sentAt":"2026-11-01T10:00:00Z","notes":"Snapshot V1","lines":[{"itemId":%d,"quantity":2}]}
						""".formatted(provider.getId(), item.getId())))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		final JsonNode createdShipment = objectMapper.readTree(shipmentBody);
		assertOnlyContractFields(createdShipment, "shipment");
		assertOnlyContractFields(createdShipment.get("lines").get(0), "shipmentLine");
		final long shipmentId = createdShipment.get("id").asLong();

		final JsonNode list = read(get("/api/v1/laundry/shipments")
				.param("providerId", provider.getId().toString())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
		assertOnlyContractFields(findById(list, shipmentId), "shipment");

		final JsonNode detail = read(get("/api/v1/laundry/shipments/{id}", shipmentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
		assertOnlyContractFields(detail, "shipment");
		assertOnlyContractFields(detail.get("lines").get(0), "shipmentLine");
		assertThat(detail.get("providerName").asText()).isEqualTo(provider.getName());
		assertThat(detail.get("kind").asText()).isEqualTo("STANDARD");
		assertThat(detail.get("status").asText()).isEqualTo("SENT");

		final JsonNode summary = read(get("/api/v1/laundry/summary")
				.param("providerId", provider.getId().toString())
				.param("dateFrom", "2026-11-01")
				.param("dateTo", "2026-11-01")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
		assertOnlyContractFields(summary, "summary");
		final JsonNode dailyRow = summary.get("rows").get(0);
		assertOnlyContractFields(dailyRow, "dailySummaryRow");
		assertThat(dailyRow.get("date").asText()).isEqualTo("2026-11-01");

		final JsonNode audit = read(get("/api/v1/laundry/shipments/{id}/audit", shipmentId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
		JsonNode createEvent = null;
		for (JsonNode event : audit) {
			if ("CREATE".equals(event.get("actionName").asText())) {
				createEvent = event;
				break;
			}
		}
		assertThat(createEvent).isNotNull();
		assertOnlyContractFields(createEvent, "auditEvent");

		final LaundryShipment receipt = LaundryShipment.createReceipt(
				unique("REC"), provider, OffsetDateTime.parse("2026-11-02T12:00:00Z"), "Snapshot receipt", "operador.demo"
		);
		receipt.addLine(LaundryShipmentLine.receipt(item, BigDecimal.ONE));
		final long receiptId = shipmentRepository.saveAndFlush(receipt).getId();
		final JsonNode receiptDetail = read(get("/api/v1/laundry/shipments/{id}", receiptId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
		assertOnlyContractFields(receiptDetail, "shipment");
		assertOnlyContractFields(receiptDetail.get("lines").get(0), "shipmentLine");
		assertThat(receiptDetail.get("kind").asText()).isEqualTo("RECEIPT");
		assertThat(receiptDetail.get("status").asText()).isEqualTo("RECEIVED");

		final JsonNode error = readBadRequest(post("/api/v1/laundry/shipments")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"providerId":%d,"lines":[{"itemId":%d,"quantity":1}]}
					""".formatted(provider.getId(), item.getId())));
		assertOnlyContractFields(error, "error");
		assertThat(error.get("status").asInt()).isEqualTo(400);
		assertThat(error.get("path").asText()).isEqualTo("/api/v1/laundry/shipments");
	}

	private String authenticate() throws Exception {
		final String response = mockMvc.perform(post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"operador.demo\",\"password\":\"123456\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).get("accessToken").asText();
	}

	private JsonNode read(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
		final String body = mockMvc.perform(request)
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body);
	}

	private JsonNode readBadRequest(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
		final String body = mockMvc.perform(request)
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body);
	}

	private JsonNode findById(JsonNode array, long id) {
		for (JsonNode node : array) {
			if (node.path("id").asLong() == id) {
				return node;
			}
		}
		throw new AssertionError("Node with id " + id + " not found");
	}

	private void assertOnlyContractFields(JsonNode node, String contractKey) {
		assertThat(node).isNotNull();
		final Set<String> actual = new TreeSet<>();
		node.fieldNames().forEachRemaining(actual::add);
		final Set<String> expected = new TreeSet<>();
		contract.get(contractKey).forEach(value -> expected.add(value.asText()));
		assertThat(actual).isEqualTo(expected);
	}

	private String unique(String prefix) {
		return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
	}
}
