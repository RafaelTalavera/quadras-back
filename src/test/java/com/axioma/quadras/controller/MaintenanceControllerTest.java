package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.repository.MaintenanceLocationRepository;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentRepository;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
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
@TestPropertySource(properties = {
		"spring.flyway.enabled=true",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class MaintenanceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository;

	@Autowired
	private MaintenanceOrderRepository maintenanceOrderRepository;

	@Autowired
	private MaintenanceProviderRepository maintenanceProviderRepository;

	@Autowired
	private MaintenanceLocationRepository maintenanceLocationRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		maintenanceOrderAttachmentRepository.deleteAll();
		maintenanceOrderRepository.deleteAll();
		maintenanceProviderRepository.deleteAll();
		maintenanceLocationRepository.deleteAll();
		accessToken = authenticate();
	}

	@Test
	void shouldCreateOverlappingOrdersAndExposeConflictsWithoutBlocking() throws Exception {
		final long locationId = createLocation(
				"ROOM",
				"204",
				"Habitacion 204",
				"2",
				"Cuarto premium"
		);
		final long providerId = createProvider(
				"EXTERNAL",
				"Clima Sul",
				"Servicos de aires",
				"Mantenimiento preventivo y correctivo de aires"
		);
		final long firstOrderId = createOrder(
				locationId,
				providerId,
				"Revision del aire",
				"Chequeo de temperatura y filtros",
				"HIGH",
				"2026-04-10T10:00:00",
				"2026-04-10T11:30:00"
		);

		mockMvc.perform(get("/api/v1/maintenance/orders/conflicts")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("locationId", String.valueOf(locationId))
						.param("scheduledStartAt", "2026-04-10T10:45:00")
						.param("scheduledEndAt", "2026-04-10T11:15:00"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(firstOrderId))
				.andExpect(jsonPath("$[0].title").value("Revision del aire"))
				.andExpect(jsonPath("$[0].status").value("SCHEDULED"));

		createOrder(
				locationId,
				providerId,
				"Chequeo de internet en paralelo",
				"El operador decide seguir aunque exista conflicto",
				"MEDIUM",
				"2026-04-10T11:00:00",
				"2026-04-10T12:00:00"
		);

		mockMvc.perform(get("/api/v1/maintenance/orders")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-10")
						.param("dateTo", "2026-04-10")
						.param("locationId", String.valueOf(locationId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));

		mockMvc.perform(get("/api/v1/maintenance/locations/{locationId}/history", locationId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("Chequeo de internet en paralelo"))
				.andExpect(jsonPath("$[1].title").value("Revision del aire"));
	}

	@Test
	void shouldManageOrderAttachmentsAndLifecycle() throws Exception {
		final long locationId = createLocation(
				"COMMON_AREA",
				"LOBBY",
				"Lobby principal",
				"PB",
				"Area de recepcion"
		);
		final long providerId = createProvider(
				"INTERNAL",
				"Equipo interno",
				"Manutencao geral",
				"Atiende incidencias generales del hotel"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Cambiar luminaria",
				"Se reporta una luz intermitente",
				"MEDIUM",
				"2026-04-12T08:00:00",
				"2026-04-12T09:00:00"
		);

		final String attachmentResponse = mockMvc.perform(post("/api/v1/maintenance/orders/{orderId}/attachments", orderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "attachmentType": "PHOTO",
								  "fileName": "lobby-luz.jpg",
								  "contentType": "image/jpeg",
								  "base64Content": "%s"
								}
								""".formatted(base64("foto"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.attachmentType").value("PHOTO"))
				.andExpect(jsonPath("$.fileName").value("lobby-luz.jpg"))
				.andExpect(jsonPath("$.fileSize").value(4))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final long attachmentId = objectMapper.readTree(attachmentResponse).get("id").asLong();

		mockMvc.perform(get("/api/v1/maintenance/orders/{orderId}/attachments", orderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].fileName").value("lobby-luz.jpg"));

		mockMvc.perform(patch("/api/v1/maintenance/orders/{orderId}/start", orderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "startedAt": "2026-04-12T08:05:00Z"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		mockMvc.perform(patch("/api/v1/maintenance/orders/{orderId}/complete", orderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "completedAt": "2026-04-12T08:45:00Z",
								  "resolutionNotes": "Luminaria reemplazada y probada"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.resolutionNotes").value("Luminaria reemplazada y probada"));

		mockMvc.perform(delete("/api/v1/maintenance/orders/{orderId}/attachments/{attachmentId}", orderId, attachmentId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/maintenance/orders/{orderId}/attachments", orderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void shouldMarkMaintenanceOrderPayment() throws Exception {
		final long locationId = createLocation(
				"ROOM",
				"409",
				"Habitacion 409",
				"4",
				"Cuarto con vista interna"
		);
		final long providerId = createProvider(
				"EXTERNAL",
				"Electric Sul",
				"Servicio electrico",
				"Atiende fallas y reparaciones de energia"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Revisar tomacorriente",
				"Cliente reporta chispa intermitente",
				"HIGH",
				"2026-04-14T15:00:00",
				"2026-04-14T16:00:00"
		);

		mockMvc.perform(patch("/api/v1/maintenance/orders/{orderId}/payment", orderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "paymentMethod": "PIX",
								  "paymentDate": "2026-04-14",
								  "paymentNotes": "Pago confirmado con comprobante"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paid").value(true))
				.andExpect(jsonPath("$.paymentMethod").value("PIX"))
				.andExpect(jsonPath("$.paymentDate").value("2026-04-14"))
				.andExpect(jsonPath("$.paymentNotes").value("Pago confirmado con comprobante"));

		mockMvc.perform(get("/api/v1/maintenance/orders")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-14")
						.param("dateTo", "2026-04-14"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(orderId))
				.andExpect(jsonPath("$[0].paid").value(true))
				.andExpect(jsonPath("$[0].paymentMethod").value("PIX"));
	}

	@Test
	void shouldReturnMaintenanceSummaryAndDetails() throws Exception {
		final MaintenanceLocation room = maintenanceLocationRepository.save(
				MaintenanceLocation.create(
						MaintenanceLocationType.ROOM,
						"301",
						"Habitacion 301",
						"3",
						"Suite frente al mar",
						true,
						"system"
				)
		);
		final MaintenanceLocation commonArea = maintenanceLocationRepository.save(
				MaintenanceLocation.create(
						MaintenanceLocationType.COMMON_AREA,
						"ROOF",
						"Terraza",
						"4",
						"Area comun externa",
						true,
						"system"
				)
		);
		final MaintenanceProvider internalProvider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						MaintenanceProviderType.INTERNAL,
						"Equipe interna",
						"Manutencao geral",
						"Rutinas y correctivos livianos",
						"interno@hotel.local",
						true,
						"system"
				)
		);
		final MaintenanceProvider externalProvider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						MaintenanceProviderType.EXTERNAL,
						"Internet Sul",
						"Servico de internet",
						"Conectividad y cableado",
						"soporte@internet-sul.local",
						true,
						"system"
				)
		);

		final MaintenanceOrder completedOrder = MaintenanceOrder.report(
				room,
				internalProvider,
				"Troca de filtro",
				"Filtro del aire reemplazado",
				MaintenancePriority.URGENT,
				LocalDateTime.of(2026, 4, 2, 10, 0),
				LocalDateTime.of(2026, 4, 2, 11, 30),
				"system"
		);
		completedOrder.start(OffsetDateTime.parse("2026-04-02T10:00:00Z"), "system");
		completedOrder.complete(OffsetDateTime.parse("2026-04-02T11:30:00Z"), "Trabajo finalizado", "system");
		maintenanceOrderRepository.save(completedOrder);

		final MaintenanceOrder scheduledOrder = MaintenanceOrder.report(
				commonArea,
				externalProvider,
				"Revision de routers",
				"Inspeccion preventiva en terraza",
				MaintenancePriority.MEDIUM,
				LocalDateTime.of(2026, 4, 3, 9, 0),
				LocalDateTime.of(2026, 4, 3, 10, 0),
				"system"
		);
		maintenanceOrderRepository.save(scheduledOrder);

		final MaintenanceOrder cancelledOrder = MaintenanceOrder.report(
				room,
				externalProvider,
				"Chequeo de access point",
				"Se pospone por falta de repuesto",
				MaintenancePriority.HIGH,
				LocalDateTime.of(2026, 4, 4, 14, 0),
				LocalDateTime.of(2026, 4, 4, 15, 0),
				"system"
		);
		cancelledOrder.cancel("Proveedor reagendara la visita", "system");
		maintenanceOrderRepository.save(cancelledOrder);

		mockMvc.perform(get("/api/v1/maintenance/reports/summary")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openCount").value(0))
				.andExpect(jsonPath("$.scheduledCount").value(1))
				.andExpect(jsonPath("$.inProgressCount").value(0))
				.andExpect(jsonPath("$.completedCount").value(1))
				.andExpect(jsonPath("$.cancelledCount").value(1))
				.andExpect(jsonPath("$.internalCount").value(1))
				.andExpect(jsonPath("$.externalCount").value(2))
				.andExpect(jsonPath("$.roomsCount").value(2))
				.andExpect(jsonPath("$.commonAreasCount").value(1))
				.andExpect(jsonPath("$.urgentCount").value(1))
				.andExpect(jsonPath("$.averageResolutionHours").value(1.50))
				.andExpect(jsonPath("$.providerBreakdown.length()").value(2))
				.andExpect(jsonPath("$.providerTypeBreakdown[0].code").value("INTERNAL"))
				.andExpect(jsonPath("$.providerTypeBreakdown[0].completedCount").value(1))
				.andExpect(jsonPath("$.providerTypeBreakdown[1].code").value("EXTERNAL"))
				.andExpect(jsonPath("$.providerTypeBreakdown[1].scheduledCount").value(1))
				.andExpect(jsonPath("$.providerTypeBreakdown[1].cancelledCount").value(1));

		mockMvc.perform(get("/api/v1/maintenance/reports/summary/details")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("groupBy", "PROVIDER")
						.param("code", String.valueOf(externalProvider.getId()))
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.label").value("Internet Sul"))
				.andExpect(jsonPath("$.summary.scheduledCount").value(1))
				.andExpect(jsonPath("$.summary.cancelledCount").value(1))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].title").value("Revision de routers"))
				.andExpect(jsonPath("$.items[1].status").value("CANCELLED"));
	}

	private long createLocation(
			String locationType,
			String code,
			String label,
			String floor,
			String description
	) throws Exception {
		final String responseBody = mockMvc.perform(post("/api/v1/maintenance/locations")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "locationType": "%s",
								  "code": "%s",
								  "label": "%s",
								  "floor": "%s",
								  "description": "%s",
								  "active": true
								}
								""".formatted(locationType, code, label, floor, description)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(responseBody).get("id").asLong();
	}

	private long createProvider(
			String providerType,
			String name,
			String serviceLabel,
			String scopeDescription
	) throws Exception {
		final String responseBody = mockMvc.perform(post("/api/v1/maintenance/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "providerType": "%s",
								  "name": "%s",
								  "serviceLabel": "%s",
								  "scopeDescription": "%s",
								  "contact": "soporte@hotel.local",
								  "active": true
								}
								""".formatted(providerType, name, serviceLabel, scopeDescription)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(responseBody).get("id").asLong();
	}

	private long createOrder(
			long locationId,
			long providerId,
			String title,
			String description,
			String priority,
			String scheduledStartAt,
			String scheduledEndAt
	) throws Exception {
		final String responseBody = mockMvc.perform(post("/api/v1/maintenance/orders")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "locationId": %d,
								  "providerId": %d,
								  "title": "%s",
								  "description": "%s",
								  "priority": "%s",
								  "scheduledStartAt": "%s",
								  "scheduledEndAt": "%s"
								}
								""".formatted(
										locationId,
										providerId,
										title,
										description,
										priority,
										scheduledStartAt,
										scheduledEndAt
								)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(responseBody).get("id").asLong();
	}

	private String base64(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private String bearerToken() {
		return "Bearer " + accessToken;
	}

	private String authenticate() throws Exception {
		final String payload = """
				{
				  "username": "operador.demo",
				  "password": "Costanorte2026!"
				}
				""";

		final String responseBody = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode response = objectMapper.readTree(responseBody);
		return response.get("accessToken").asText();
	}
}
