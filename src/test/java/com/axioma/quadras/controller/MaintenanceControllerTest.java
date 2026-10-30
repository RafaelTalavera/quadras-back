package com.axioma.quadras.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationCategory;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceNotification;
import com.axioma.quadras.domain.model.MaintenanceNotificationStatus;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import com.axioma.quadras.domain.model.NotificationChannel;
import com.axioma.quadras.domain.model.NotificationDeliveryStatus;
import com.axioma.quadras.domain.model.NotificationOutbox;
import com.axioma.quadras.repository.MaintenanceLocationRepository;
import com.axioma.quadras.repository.MaintenanceNotificationRepository;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentRepository;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import com.axioma.quadras.repository.MaintenancePlanRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import com.axioma.quadras.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.greaterThan;
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
		"spring.jpa.hibernate.ddl-auto=validate",
		"costanorte.push-subscriptions.encryption-key=costanorte-test-push-encryption-key-2026"
})
class MaintenanceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository;

	@Autowired
	private MaintenanceOrderRepository maintenanceOrderRepository;

	@Autowired
	private MaintenanceNotificationRepository maintenanceNotificationRepository;

	@Autowired
	private NotificationOutboxRepository notificationOutboxRepository;

	@Autowired
	private PushSubscriptionRepository pushSubscriptionRepository;

	@Autowired
	private MaintenanceProviderRepository maintenanceProviderRepository;

	@Autowired
	private MaintenancePlanRepository maintenancePlanRepository;

	@Autowired
	private MaintenanceLocationRepository maintenanceLocationRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		pushSubscriptionRepository.deleteAll();
		notificationOutboxRepository.deleteAll();
		maintenanceNotificationRepository.deleteAll();
		maintenanceOrderAttachmentRepository.deleteAll();
		maintenanceOrderRepository.deleteAll();
		maintenancePlanRepository.deleteAll();
		maintenanceProviderRepository.deleteAll();
		maintenanceLocationRepository.deleteAll();
		accessToken = authenticate();
	}

	@Test
	void shouldRegisterRefreshHeartbeatAndUnregisterPushSubscription() throws Exception {
		final String firstToken = "fcm-registration-token-value-000000000001";
		mockMvc.perform(post("/api/v1/push/subscriptions")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "installationId": "web.installation-001",
								  "platform": "WEB",
								  "provider": "FIREBASE",
								  "token": "%s"
								}
								""".formatted(firstToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.installationId").value("web.installation-001"))
				.andExpect(jsonPath("$.platform").value("WEB"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.token").doesNotExist());

		final var stored = pushSubscriptionRepository.findAll().get(0);
		assertThat(stored.getEncryptedToken()).doesNotContain(firstToken);
		assertThat(stored.getTokenHash()).hasSize(64);

		final String rotatedToken = "fcm-registration-token-value-000000000002";
		mockMvc.perform(post("/api/v1/push/subscriptions")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "installationId": "web.installation-001",
								  "platform": "WEB",
								  "provider": "FIREBASE",
								  "token": "%s"
								}
								""".formatted(rotatedToken)))
				.andExpect(status().isOk());
		assertThat(pushSubscriptionRepository.count()).isEqualTo(1);

		mockMvc.perform(patch("/api/v1/push/subscriptions/web.installation-001/heartbeat")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/api/v1/push/subscriptions/web.installation-001")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/v1/push/subscriptions/web.installation-001")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());

		final var inactive = pushSubscriptionRepository.findAll().get(0);
		assertThat(inactive.isActive()).isFalse();
		assertThat(inactive.getEncryptedToken()).isNull();
		assertThat(inactive.getTokenHash()).isNull();
	}

	@Test
	void shouldRequireAuthenticationAndValidPushSubscriptionPayload() throws Exception {
		mockMvc.perform(post("/api/v1/push/subscriptions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/push/subscriptions")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "installationId": "bad id",
								  "platform": "WEB",
								  "provider": "FIREBASE",
								  "token": "short"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldIsolatePushSubscriptionOwnershipAndReassignInstallation() throws Exception {
		final String installationId = "shared.installation-001";
		final String token = "fcm-shared-registration-token-000000000001";
		registerPushSubscription(accessToken, installationId, token);

		final String supervisorToken = authenticate("supervisor.demo", "654321");
		registerPushSubscription(supervisorToken, installationId, token);

		assertThat(pushSubscriptionRepository.findAll())
				.filteredOn(subscription -> subscription.getUsername().equals("operador.demo"))
				.singleElement()
				.satisfies(subscription -> {
					assertThat(subscription.isActive()).isFalse();
					assertThat(subscription.getEncryptedToken()).isNull();
				});
		assertThat(pushSubscriptionRepository.findAll())
				.filteredOn(subscription -> subscription.getUsername().equals("supervisor.demo"))
				.singleElement()
				.satisfies(subscription -> assertThat(subscription.isActive()).isTrue());

		mockMvc.perform(patch("/api/v1/push/subscriptions/{installationId}/heartbeat", installationId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldRejectLocationWhenNumericReferenceIsOmitted() throws Exception {
		mockMvc.perform(post("/api/v1/maintenance/locations")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "locationType": "ROOM",
								  "code": "",
								  "label": "Apartamento Vista Mar 301",
								  "floor": "3",
								  "description": "Unidad reformada",
								  "active": true
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRescheduleWithOptimisticScheduleVersion() throws Exception {
		final long locationId = createLocation("ROOM", "208", "Habitacion 208", "2", "Cuarto");
		final long providerId = createProvider(
				"INTERNAL",
				"GENERAL_MAINTENANCE",
				"Equipo interno",
				"Mantenimiento general",
				"Operacion del hotel"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Revisar cerradura",
				"Revision preventiva",
				"2026-04-10T10:00:00",
				"2026-04-10T11:00:00"
		);

		mockMvc.perform(patch("/api/v1/maintenance/orders/{orderId}/reschedule", orderId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "scheduledStartAt": "2026-04-10T12:00:00",
							  "scheduledEndAt": "2026-04-10T13:30:00",
							  "reason": "Cambio de disponibilidad",
							  "expectedScheduleVersion": 1
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduleVersion").value(2))
				.andExpect(jsonPath("$.scheduledStartAt").value("2026-04-10T12:00:00"))
				.andExpect(jsonPath("$.scheduledEndAt").value("2026-04-10T13:30:00"));

		mockMvc.perform(patch("/api/v1/maintenance/orders/{orderId}/reschedule", orderId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "scheduledStartAt": "2026-04-10T14:00:00",
							  "scheduledEndAt": "2026-04-10T15:00:00",
							  "reason": "Segundo cambio",
							  "expectedScheduleVersion": 1
							}
							"""))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldResolveActiveNotificationAndAbandonPendingPushWhenRescheduling() throws Exception {
		final long locationId = createLocation("ROOM", "210", "Habitacion 210", "2", "Cuarto");
		final long providerId = createProvider(
				"INTERNAL",
				"GENERAL_MAINTENANCE",
				"Equipo preventivo",
				"Mantenimiento general",
				"Operacion del hotel"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Revisar tablero",
				"Control preventivo",
				"2026-04-10T10:00:00",
				"2026-04-10T11:00:00"
		);
		final MaintenanceOrder order = maintenanceOrderRepository.findById(orderId).orElseThrow();
		final MaintenanceNotification notification = maintenanceNotificationRepository.save(
				MaintenanceNotification.create(
						order,
						"operador.demo",
						MaintenanceNotificationType.DUE_TO_START,
						order.getScheduleVersion(),
						"INITIAL",
						"Manutencao pronta para iniciar",
						"Habitacion 210 - Revisar tablero",
						OffsetDateTime.parse("2026-04-10T13:00:00Z")
				)
		);
		final NotificationOutbox outbox = notificationOutboxRepository.save(
				NotificationOutbox.pending(
						notification,
						NotificationChannel.PUSH,
						"operador.demo",
						"maintenance:test:reschedule",
						OffsetDateTime.parse("2026-04-10T13:00:00Z")
				)
		);

		mockMvc.perform(patch("/api/v1/maintenance/orders/{orderId}/reschedule", orderId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "scheduledStartAt": "2026-04-10T12:00:00",
							  "scheduledEndAt": "2026-04-10T13:00:00",
							  "reason": "Cambio operativo",
							  "expectedScheduleVersion": 1
							}
							"""))
				.andExpect(status().isOk());

		assertThat(maintenanceNotificationRepository.findById(notification.getId()).orElseThrow().getStatus())
				.isEqualTo(MaintenanceNotificationStatus.RESOLVED);
		assertThat(notificationOutboxRepository.findById(outbox.getId()).orElseThrow().getDeliveryStatus())
				.isEqualTo(NotificationDeliveryStatus.ABANDONED);
	}

	@Test
	void shouldExposeOnlyOwnNotificationInboxAndSupportReadAndDismiss() throws Exception {
		final long locationId = createLocation("ROOM", "211", "Habitacion 211", "2", "Cuarto");
		final long providerId = createProvider(
				"INTERNAL",
				"GENERAL_MAINTENANCE",
				"Equipo de avisos",
				"Mantenimiento general",
				"Operacion del hotel"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Revisar sensor",
				"Control de funcionamiento",
				"2026-04-10T10:00:00",
				"2026-04-10T11:00:00"
		);
		final MaintenanceOrder order = maintenanceOrderRepository.findById(orderId).orElseThrow();
		final MaintenanceNotification ownNotification = maintenanceNotificationRepository.save(
				MaintenanceNotification.create(
						order,
						"operador.demo",
						MaintenanceNotificationType.DUE_TO_START,
						order.getScheduleVersion(),
						"INITIAL",
						"Manutencao pronta para iniciar",
						"Habitacion 211 - Revisar sensor",
						OffsetDateTime.parse("2026-04-10T13:00:00Z")
				)
		);
		final MaintenanceNotification foreignNotification = maintenanceNotificationRepository.save(
				MaintenanceNotification.create(
						order,
						"supervisor.demo",
						MaintenanceNotificationType.DUE_TO_START,
						order.getScheduleVersion(),
						"INITIAL",
						"Manutencao pronta para iniciar",
						"Habitacion 211 - Revisar sensor",
						OffsetDateTime.parse("2026-04-10T13:00:00Z")
				)
		);

		mockMvc.perform(get("/api/v1/notifications")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("module", "maintenance")
					.param("status", "UNREAD")
					.param("page", "0")
					.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.hasNext").value(false))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(ownNotification.getId()))
				.andExpect(jsonPath("$.items[0].orderId").value(orderId))
				.andExpect(jsonPath("$.items[0].status").value("UNREAD"));

		mockMvc.perform(get("/api/v1/notifications/unread-count")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("module", "maintenance"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1));

		mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", foreignNotification.getId())
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());

		mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", ownNotification.getId())
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());
		mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", ownNotification.getId())
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());

		mockMvc.perform(patch("/api/v1/notifications/{notificationId}/dismiss", ownNotification.getId())
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNoContent());
		assertThat(maintenanceNotificationRepository.findById(ownNotification.getId()).orElseThrow().getStatus())
				.isEqualTo(MaintenanceNotificationStatus.DISMISSED);

		mockMvc.perform(get("/api/v1/notifications/unread-count")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0));
	}

	@Test
	void shouldListOverlappingCalendarOrdersAndSeparateBacklog() throws Exception {
		final long locationId = createLocation("ROOM", "209", "Habitacion 209", "2", "Cuarto");
		final long providerId = createProvider(
				"INTERNAL",
				"GENERAL_MAINTENANCE",
				"Equipo nocturno",
				"Mantenimiento general",
				"Operacion del hotel"
		);
		final long overlappingOrderId = createOrder(
				locationId,
				providerId,
				"Revision nocturna",
				"Cruza el inicio del rango",
				"2026-04-30T23:00:00",
				"2026-05-01T02:00:00"
		);
		final String backlogResponse = mockMvc.perform(post("/api/v1/maintenance/orders")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "locationId": %d,
							  "providerId": %d,
							  "title": "Reparacion pendiente",
							  "description": "Sin horario asignado",
							  "requestOrigin": "INTERNAL_ROLE",
							  "requestedForGuest": false
							}
							""".formatted(locationId, providerId)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		final long backlogOrderId = objectMapper.readTree(backlogResponse).get("id").asLong();

		mockMvc.perform(get("/api/v1/maintenance/orders/calendar")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("rangeStart", "2026-05-01T00:00:00")
					.param("rangeEndExclusive", "2026-05-02T00:00:00"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rangeStart").value("2026-05-01T00:00:00"))
				.andExpect(jsonPath("$.scheduledOrders.length()").value(1))
				.andExpect(jsonPath("$.scheduledOrders[0].id").value(overlappingOrderId))
				.andExpect(jsonPath("$.backlogOrders.length()").value(1))
				.andExpect(jsonPath("$.backlogOrders[0].id").value(backlogOrderId))
				.andExpect(jsonPath("$.backlogHasMore").value(false));

		mockMvc.perform(get("/api/v1/maintenance/orders/calendar")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("rangeStart", "2026-01-01T00:00:00")
					.param("rangeEndExclusive", "2026-04-01T00:00:00"))
				.andExpect(status().isBadRequest());
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
				"AIR_CONDITIONING",
				"Clima Sul",
				"Servicos de aires",
				"Mantenimiento preventivo y correctivo de aires"
		);
		final long firstOrderId = createOrder(
				locationId,
				providerId,
				"Revision del aire",
				"Chequeo de temperatura y filtros",
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
				"2026-04-10T11:00:00",
				"2026-04-10T12:00:00"
		);

		mockMvc.perform(post("/api/v1/maintenance/orders/{orderId}/attachments", firstOrderId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "attachmentType": "PHOTO",
								  "fileName": "aire-204.jpg",
								  "contentType": "image/jpeg",
								  "base64Content": "%s"
								}
								""".formatted(base64("foto-aire"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fileName").value("aire-204.jpg"));

		mockMvc.perform(get("/api/v1/maintenance/orders")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-10")
						.param("dateTo", "2026-04-10")
						.param("locationId", String.valueOf(locationId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].expectedCompletionAt").value("2026-04-10T11:30:00"))
				.andExpect(jsonPath("$[0].attachments.length()").value(1))
				.andExpect(jsonPath("$[0].attachments[0].fileName").value("aire-204.jpg"));

		mockMvc.perform(get("/api/v1/maintenance/locations")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].locationCategory").value("APARTMENT"));

		mockMvc.perform(get("/api/v1/maintenance/locations/{locationId}/history", locationId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("Chequeo de internet en paralelo"))
				.andExpect(jsonPath("$[1].title").value("Revision del aire"))
				.andExpect(jsonPath("$[1].attachments.length()").value(1))
				.andExpect(jsonPath("$[1].attachments[0].fileName").value("aire-204.jpg"));
	}

	@Test
	void shouldManageOrderAttachmentsAndLifecycle() throws Exception {
		final long locationId = createLocation(
				"COMMON_AREA",
				"500",
				"Recepcion",
				"PB",
				"Area de recepcion"
		);
		final long providerId = createProvider(
				"INTERNAL",
				"GENERAL_MAINTENANCE",
				"Equipo interno",
				"Manutencao geral",
				"Atiende incidencias generales del hotel"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Cambiar luminaria",
				"Se reporta una luz intermitente",
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
	void shouldListMaintenanceProvidersAndLocationsOrdered() throws Exception {
		createProvider(
				"INTERNAL",
				"GENERAL_MAINTENANCE",
				"Bravo interno",
				"Soporte interno",
				"Mantenimiento operativo"
		);
		createProvider(
				"EXTERNAL",
				"ELEVATORS",
				"Alfa externo",
				"Elevadores",
				"Soporte de ascensores"
		);
		createProvider(
				"EXTERNAL",
				"AIR_CONDITIONING",
				"Zulu externo",
				"Climatizacion",
				"Reparaciones HVAC"
		);

		createLocation(
				"COMMON_AREA",
				"514",
				"Piscina",
				"PB",
				"Zona humeda"
		);
		createLocation(
				"ROOM",
				"101",
				"Habitacion 101",
				"1",
				"Cuarto frente a jardin"
		);
		createLocation(
				"ROOM",
				"305",
				"Habitacion 305",
				"3",
				"Cuarto superior"
		);

		mockMvc.perform(get("/api/v1/maintenance/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].providerType").value("EXTERNAL"))
				.andExpect(jsonPath("$[0].name").value("Alfa externo"))
				.andExpect(jsonPath("$[1].providerType").value("EXTERNAL"))
				.andExpect(jsonPath("$[1].name").value("Zulu externo"))
				.andExpect(jsonPath("$[2].providerType").value("INTERNAL"))
				.andExpect(jsonPath("$[2].name").value("Bravo interno"));

		mockMvc.perform(get("/api/v1/maintenance/locations")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].locationType").value("ROOM"))
				.andExpect(jsonPath("$[0].code").value("101"))
				.andExpect(jsonPath("$[1].locationType").value("ROOM"))
				.andExpect(jsonPath("$[1].code").value("305"))
				.andExpect(jsonPath("$[2].locationType").value("COMMON_AREA"))
				.andExpect(jsonPath("$[2].code").value("514"));
	}

	@Test
	void shouldKeepInternetAndDoorsOrdersWithDifferentProviders() throws Exception {
		final long locationId = createLocation("ROOM", "212", "Quarto 212", "2", "Quarto de teste");
		final long internetProviderId = createProvider(
				"EXTERNAL", "INTERNET", "Rede Norte", "Servico de internet", "Wi-Fi e rede"
		);
		final long doorsProviderId = createProvider(
				"EXTERNAL", "DOORS_AND_WINDOWS", "Portas Norte", "Portas e janelas", "Fechaduras e esquadrias"
		);
		final long internetOrderId = createOrder(
				locationId, internetProviderId, "Revisar Wi-Fi", "Rede instavel",
				"2026-09-15T09:00:00", "2026-09-15T10:00:00"
		);
		final long doorsOrderId = createOrder(
				locationId, doorsProviderId, "Revisar fechadura", "Porta nao fecha",
				"2026-09-15T11:00:00", "2026-09-15T12:00:00"
		);

		assertThat(maintenanceOrderRepository.findById(internetOrderId).orElseThrow()
				.getProvider().getId()).isEqualTo(internetProviderId);
		assertThat(maintenanceOrderRepository.findById(doorsOrderId).orElseThrow()
				.getProvider().getId()).isEqualTo(doorsProviderId);
		assertThat(maintenanceProviderRepository.findById(internetProviderId).orElseThrow()
				.getSpecialty()).isEqualTo(MaintenanceProviderSpecialty.INTERNET);
		assertThat(maintenanceProviderRepository.findById(doorsProviderId).orElseThrow()
				.getSpecialty()).isEqualTo(MaintenanceProviderSpecialty.DOORS_AND_WINDOWS);
	}

	@Test
	void shouldCreateSeparateCamerasTelephonesAndItOrders() throws Exception {
		final long locationId = createLocation("ROOM", "213", "Quarto 213", "2", "Quarto de teste");
		final String[] specialties = {"CAMERAS", "TELEPHONES", "IT_SUPPORT"};
		final String[] titles = {"Revisar camera", "Revisar ramal", "Revisar computador"};
		for (int index = 0; index < specialties.length; index++) {
			final long providerId = createProvider(
					"EXTERNAL", specialties[index], "Prestador " + specialties[index],
					"Servico " + specialties[index], "Atendimento especifico"
			);
			final long orderId = createOrder(
					locationId, providerId, titles[index], "Solicitacao de teste",
					"2026-09-15T09:00:00", "2026-09-15T10:00:00"
			);
			assertThat(maintenanceOrderRepository.findById(orderId).orElseThrow()
					.getProvider().getId()).isEqualTo(providerId);
			assertThat(maintenanceProviderRepository.findById(providerId).orElseThrow()
					.getSpecialty().name()).isEqualTo(specialties[index]);
		}
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
				"GENERAL_MAINTENANCE",
				"Electric Sul",
				"Servicio electrico",
				"Atiende fallas y reparaciones de energia"
		);
		final long orderId = createOrder(
				locationId,
				providerId,
				"Revisar tomacorriente",
				"Cliente reporta chispa intermitente",
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
	void shouldReturnCompactMaintenanceOrdersPage() throws Exception {
		final long locationId = createLocation(
				"ROOM",
				"512",
				"Habitacion 512",
				"5",
				"Cuarto alto"
		);
		final long providerId = createProvider(
				"EXTERNAL",
				"GENERAL_MAINTENANCE",
				"Servico rapido",
				"Manutencao expressa",
				"Atiende correctivos"
		);
		createOrder(
				locationId,
				providerId,
				"Orden 1",
				"Detalle 1",
				"2026-04-18T08:00:00",
				"2026-04-18T09:00:00"
		);
		createOrder(
				locationId,
				providerId,
				"Orden 2",
				"Detalle 2",
				"2026-04-18T09:00:00",
				"2026-04-18T10:00:00"
		);
		createOrder(
				locationId,
				providerId,
				"Orden 3",
				"Detalle 3",
				"2026-04-18T10:00:00",
				"2026-04-18T11:00:00"
		);

		mockMvc.perform(get("/api/v1/maintenance/orders/compact")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-18")
						.param("dateTo", "2026-04-18")
						.param("page", "0")
						.param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].title").value("Orden 1"))
				.andExpect(jsonPath("$.items[0].expectedCompletionAt").value("2026-04-18T09:00:00"))
				.andExpect(jsonPath("$.items[0].paid").value(false))
				.andExpect(jsonPath("$.items[0].attachments").doesNotExist());
	}

	@Test
	void shouldCreateAndGeneratePreventiveMaintenancePlan() throws Exception {
		final long locationId = createLocation(
				"ROOM",
				"101",
				"Apartamento 101",
				"1",
				"Apartamento frente piscina"
		);
		final long providerId = createProvider(
				"EXTERNAL",
				"AIR_CONDITIONING",
				"Clima Norte",
				"Mantenimiento de aires",
				"Limpieza y mantenimiento preventivo"
		);

		final String planResponseBody = mockMvc.perform(post("/api/v1/maintenance/plans")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "locationId": %d,
								  "providerId": %d,
								  "title": "Mantenimiento anual aire 101",
								  "description": "Revision general y limpieza",
								  "recurrenceUnit": "YEARLY",
								  "recurrenceInterval": 1,
								  "nextDueDate": "2026-06-15",
								  "active": true
								}
								""".formatted(locationId, providerId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Mantenimiento anual aire 101"))
				.andExpect(jsonPath("$.recurrenceUnit").value("YEARLY"))
				.andExpect(jsonPath("$.nextDueDate").value("2026-06-15"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final long planId = objectMapper.readTree(planResponseBody).get("id").asLong();

		mockMvc.perform(get("/api/v1/maintenance/plans")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(planId))
				.andExpect(jsonPath("$[0].providerSpecialty").value("AIR_CONDITIONING"));

		mockMvc.perform(post("/api/v1/maintenance/plans/{planId}/generate-order", planId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "scheduledStartAt": "2026-06-15T09:00:00",
								  "scheduledEndAt": "2026-06-15T10:00:00"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderKind").value("PREVENTIVE"))
				.andExpect(jsonPath("$.planId").value(planId))
				.andExpect(jsonPath("$.title").value("Mantenimiento anual aire 101"));

		mockMvc.perform(get("/api/v1/maintenance/plans")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].lastGeneratedOn").exists())
				.andExpect(jsonPath("$[0].nextDueDate").value("2027-06-15"));
	}

	@Test
	void shouldRejectInvalidCompactMaintenancePageSize() throws Exception {
		mockMvc.perform(get("/api/v1/maintenance/orders/compact")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("size", "0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("size must be greater than zero"));
	}

	@Test
	void shouldReturnMaintenanceSummaryAndDetails() throws Exception {
		final MaintenanceLocation room = maintenanceLocationRepository.save(
				MaintenanceLocation.create(
						MaintenanceLocationType.ROOM,
						MaintenanceLocationCategory.APARTMENT,
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
						MaintenanceLocationCategory.COMMON_AREA,
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
						MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
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
						MaintenanceProviderSpecialty.ELEVATORS,
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
				MaintenanceRequestOrigin.GUEST_REQUEST,
				true,
				"Huesped 301",
				null,
				"equipo.interno",
				LocalDateTime.of(2026, 4, 2, 10, 0),
				LocalDateTime.of(2026, 4, 2, 11, 30),
				"system",
				"OPERATOR"
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
				MaintenanceRequestOrigin.INTERNAL_ROLE,
				false,
				null,
				null,
				"proveedor.elevadores",
				LocalDateTime.of(2026, 4, 3, 9, 0),
				LocalDateTime.of(2026, 4, 3, 10, 0),
				"system",
				"OPERATOR"
		);
		maintenanceOrderRepository.save(scheduledOrder);

		final MaintenanceOrder cancelledOrder = MaintenanceOrder.report(
				room,
				externalProvider,
				"Chequeo de access point",
				"Se pospone por falta de repuesto",
				MaintenancePriority.HIGH,
				MaintenanceRequestOrigin.INTERNAL_ROLE,
				false,
				null,
				null,
				"proveedor.elevadores",
				LocalDateTime.of(2026, 4, 4, 14, 0),
				LocalDateTime.of(2026, 4, 4, 15, 0),
				"system",
				"OPERATOR"
		);
		cancelledOrder.cancel("Proveedor reagendara la visita", "system");
		maintenanceOrderRepository.save(cancelledOrder);

		mockMvc.perform(get("/api/v1/maintenance/reports/summary")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openCount").value(0))
				.andExpect(jsonPath("$.assignedCount").value(0))
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
				.andExpect(jsonPath("$.providerTypeBreakdown[0].groupKey").value("INTERNAL"))
				.andExpect(jsonPath("$.providerTypeBreakdown[0].completedCount").value(1))
				.andExpect(jsonPath("$.providerTypeBreakdown[1].groupKey").value("EXTERNAL"))
				.andExpect(jsonPath("$.providerTypeBreakdown[1].scheduledCount").value(1))
				.andExpect(jsonPath("$.providerTypeBreakdown[0].code").value("INTERNAL"))
				.andExpect(jsonPath("$.providerTypeBreakdown[1].cancelledCount").value(1));

		mockMvc.perform(get("/api/v1/maintenance/reports/summary/details")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("groupBy", "PROVIDER")
						.param("groupKey", String.valueOf(externalProvider.getId()))
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.groupKey").value(String.valueOf(externalProvider.getId())))
				.andExpect(jsonPath("$.label").value("Internet Sul"))
				.andExpect(jsonPath("$.summary.groupKey").value(String.valueOf(externalProvider.getId())))
				.andExpect(jsonPath("$.summary.scheduledCount").value(1))
				.andExpect(jsonPath("$.summary.cancelledCount").value(1))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].expectedCompletionAt").value("2026-04-03T10:00:00"))
				.andExpect(jsonPath("$.items[0].title").value("Revision de routers"))
				.andExpect(jsonPath("$.items[1].status").value("CANCELLED"));

		mockMvc.perform(get("/api/v1/maintenance/reports/summary/daily-executive")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("date", "2026-04-03"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.carryOverOpenCount").value(0))
				.andExpect(jsonPath("$.dayOpenCount").value(1))
				.andExpect(jsonPath("$.totalOpenCount").value(1))
				.andExpect(jsonPath("$.scheduledCount").value(1))
				.andExpect(jsonPath("$.carryOverOrders.length()").value(0))
				.andExpect(jsonPath("$.dayOpenOrders.length()").value(1))
				.andExpect(jsonPath("$.dayOpenOrders[0].title").value("Revision de routers"));
	}

	@Test
	void shouldIncludeOpenOrdersFromPreviousDaysInDailyExecutiveSummary() throws Exception {
		final MaintenanceLocation room = maintenanceLocationRepository.save(
				MaintenanceLocation.create(
						MaintenanceLocationType.ROOM,
						MaintenanceLocationCategory.APARTMENT,
						"301",
						"Apartamento 301",
						"3",
						null,
						true,
						"system"
				)
		);
		final MaintenanceProvider internalProvider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						MaintenanceProviderType.INTERNAL,
						MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
						"Equipe interna",
						"Manutencao geral",
						null,
						null,
						true,
						"system"
				)
		);

		final MaintenanceOrder carryOverOrder = MaintenanceOrder.report(
				room,
				internalProvider,
				"Revisar ducha",
				"Pendencia do dia anterior",
				MaintenancePriority.URGENT,
				MaintenanceRequestOrigin.INTERNAL_ROLE,
				false,
				null,
				null,
				"operador.noite",
				LocalDateTime.of(2026, 4, 2, 18, 0),
				LocalDateTime.of(2026, 4, 2, 19, 0),
				"system",
				"OPERATOR"
		);
		maintenanceOrderRepository.save(carryOverOrder);

		final MaintenanceOrder sameDayOrder = MaintenanceOrder.report(
				room,
				null,
				"Trocar lampada",
				"Solicitado na manha",
				MaintenancePriority.MEDIUM,
				MaintenanceRequestOrigin.INTERNAL_ROLE,
				false,
				null,
				null,
				null,
				LocalDateTime.of(2026, 4, 3, 9, 0),
				LocalDateTime.of(2026, 4, 3, 10, 0),
				"system",
				"OPERATOR"
		);
		maintenanceOrderRepository.save(sameDayOrder);

		final MaintenanceOrder completedPreviousDay = MaintenanceOrder.report(
				room,
				internalProvider,
				"Consertar tomada",
				"Ja resolvido",
				MaintenancePriority.HIGH,
				MaintenanceRequestOrigin.INTERNAL_ROLE,
				false,
				null,
				null,
				"operador.noite",
				LocalDateTime.of(2026, 4, 1, 14, 0),
				LocalDateTime.of(2026, 4, 1, 15, 0),
				"system",
				"OPERATOR"
		);
		completedPreviousDay.start(OffsetDateTime.parse("2026-04-01T14:05:00Z"), "system");
		completedPreviousDay.complete(OffsetDateTime.parse("2026-04-01T14:50:00Z"), "Resolvido", "system");
		maintenanceOrderRepository.save(completedPreviousDay);

		mockMvc.perform(get("/api/v1/maintenance/reports/summary/daily-executive")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("date", "2026-04-03"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.carryOverOpenCount").value(1))
				.andExpect(jsonPath("$.dayOpenCount").value(1))
				.andExpect(jsonPath("$.totalOpenCount").value(2))
				.andExpect(jsonPath("$.urgentCount").value(1))
				.andExpect(jsonPath("$.scheduledCount").value(2))
				.andExpect(jsonPath("$.inProgressCount").value(0))
				.andExpect(jsonPath("$.unassignedCount").value(1))
				.andExpect(jsonPath("$.carryOverOrders.length()").value(1))
				.andExpect(jsonPath("$.carryOverOrders[0].title").value("Revisar ducha"))
				.andExpect(jsonPath("$.dayOpenOrders.length()").value(1))
				.andExpect(jsonPath("$.dayOpenOrders[0].title").value("Trocar lampada"));
	}

	@Test
	void shouldLoadAndResetMaintenanceSimulationData() throws Exception {
		final String firstResponseBody = mockMvc.perform(post("/api/v1/maintenance/simulation/load")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "daysBack": 3,
								  "daysForward": 2,
								  "ordersPerDay": 2,
								  "backlogOrders": 4,
								  "resetPreviousSimulation": true,
								  "seed": 42
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.locationsCreated").value(greaterThan(0)))
				.andExpect(jsonPath("$.providersCreated").value(greaterThan(0)))
				.andExpect(jsonPath("$.ordersCreated").value(greaterThan(0)))
				.andExpect(jsonPath("$.scheduledCount").value(greaterThan(0)))
				.andExpect(jsonPath("$.completedCount").value(greaterThan(0)))
				.andExpect(jsonPath("$.openCount").value(greaterThan(0)))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode firstResponse = objectMapper.readTree(firstResponseBody);
		final int firstOrdersCreated = firstResponse.get("ordersCreated").asInt();

		org.junit.jupiter.api.Assertions.assertEquals(
				firstOrdersCreated,
				maintenanceOrderRepository.count()
		);

		final String secondResponseBody = mockMvc.perform(post("/api/v1/maintenance/simulation/load")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "daysBack": 3,
								  "daysForward": 2,
								  "ordersPerDay": 2,
								  "backlogOrders": 4,
								  "resetPreviousSimulation": true,
								  "seed": 42
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ordersDeleted").value(firstOrdersCreated))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode secondResponse = objectMapper.readTree(secondResponseBody);
		final int secondOrdersCreated = secondResponse.get("ordersCreated").asInt();

		org.junit.jupiter.api.Assertions.assertEquals(
				secondOrdersCreated,
				maintenanceOrderRepository.count()
		);

		mockMvc.perform(get("/api/v1/maintenance/orders")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(greaterThan(0)))
				.andExpect(jsonPath("$[0].requestOrigin").exists())
				.andExpect(jsonPath("$[0].requestOrigin").exists());
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
			String specialty,
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
								  "specialty": "%s",
								  "name": "%s",
								  "serviceLabel": "%s",
								  "scopeDescription": "%s",
								  "contact": "soporte@hotel.local",
								  "active": true
								}
								""".formatted(
										providerType,
										specialty,
										name,
										serviceLabel,
										scopeDescription
								)))
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
								  "requestOrigin": "INTERNAL_ROLE",
								  "requestedForGuest": false,
								  "scheduledStartAt": "%s",
								  "scheduledEndAt": "%s"
								}
								""".formatted(
										locationId,
										providerId,
										title,
										description,
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
		return authenticate("operador.demo", "123456");
	}

	private void registerPushSubscription(String token, String installationId, String pushToken) throws Exception {
		mockMvc.perform(post("/api/v1/push/subscriptions")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "installationId": "%s",
								  "platform": "WEB",
								  "provider": "FIREBASE",
								  "token": "%s"
								}
								""".formatted(installationId, pushToken)))
				.andExpect(status().isOk());
	}

	private String authenticate(String username, String password) throws Exception {
		final String payload = """
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password);

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
