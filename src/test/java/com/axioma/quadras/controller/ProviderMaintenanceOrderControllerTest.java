package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationCategory;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.MaintenanceLocationRepository;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentRepository;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class ProviderMaintenanceOrderControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private MaintenanceOrderRepository orderRepository;
	@Autowired private MaintenanceOrderAttachmentRepository attachmentRepository;
	@Autowired private MaintenanceProviderRepository providerRepository;
	@Autowired private MaintenanceLocationRepository locationRepository;
	@Autowired private AppUserRepository appUserRepository;
	@Autowired private PasswordEncoder passwordEncoder;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void cleanOrders() {
		attachmentRepository.deleteAll();
		orderRepository.deleteAll();
	}

	@Test
	void shouldListOnlyOwnedOrdersInStableOrderWithReducedContract() throws Exception {
		final ProviderFixture owner = createProvider("owner");
		final ProviderFixture other = createProvider("other");
		final MaintenanceOrder later = saveOrder(owner.provider(), "Later", LocalDateTime.of(2026, 11, 12, 10, 0));
		final MaintenanceOrder earlier = saveOrder(owner.provider(), "Earlier", LocalDateTime.of(2026, 11, 12, 8, 0));
		saveOrder(other.provider(), "Foreign", LocalDateTime.of(2026, 11, 12, 9, 0));

		mockMvc.perform(get("/api/v1/provider/maintenance/orders")
					.header(HttpHeaders.AUTHORIZATION, bearer(owner))
					.param("dateFrom", "2026-11-12").param("dateTo", "2026-11-12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(earlier.getId()))
				.andExpect(jsonPath("$[1].id").value(later.getId()))
				.andExpect(jsonPath("$[0].title").value("Earlier"))
				.andExpect(jsonPath("$[0].locationLabel").exists())
				.andExpect(jsonPath("$[0].providerId").doesNotExist())
				.andExpect(jsonPath("$[0].paid").doesNotExist())
				.andExpect(jsonPath("$[0].paymentMethod").doesNotExist())
				.andExpect(jsonPath("$[0].requestedByUsername").doesNotExist())
				.andExpect(jsonPath("$[0].createdBy").doesNotExist());
	}

	@Test
	void shouldHideForeignOrderInDetailAndMutations() throws Exception {
		final ProviderFixture owner = createProvider("detail-owner");
		final ProviderFixture other = createProvider("detail-other");
		final MaintenanceOrder foreign = saveOrder(other.provider(), "Foreign", LocalDateTime.of(2026, 11, 13, 9, 0));

		mockMvc.perform(get("/api/v1/provider/maintenance/orders/{id}", foreign.getId())
					.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Maintenance order not found"));

		mockMvc.perform(patch("/api/v1/provider/maintenance/orders/{id}/start", foreign.getId())
					.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isNotFound());
		org.assertj.core.api.Assertions.assertThat(orderRepository.findById(foreign.getId()).orElseThrow().getStatus())
				.isEqualTo(MaintenanceOrderStatus.SCHEDULED);
	}

	@Test
	void shouldStartAttachEvidenceAndCompleteOwnedOrder() throws Exception {
		final ProviderFixture owner = createProvider("workflow");
		final MaintenanceOrder order = saveOrder(owner.provider(), "Workflow", LocalDateTime.of(2026, 11, 14, 9, 0));

		mockMvc.perform(patch("/api/v1/provider/maintenance/orders/{id}/start", order.getId())
					.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		mockMvc.perform(post("/api/v1/provider/maintenance/orders/{id}/attachments", order.getId())
					.header(HttpHeaders.AUTHORIZATION, bearer(owner))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"attachmentType":"PHOTO","fileName":"evidence.jpg","contentType":"image/jpeg","base64Content":"AQID"}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fileName").value("evidence.jpg"))
				.andExpect(jsonPath("$.createdBy").doesNotExist());

		mockMvc.perform(patch("/api/v1/provider/maintenance/orders/{id}/complete", order.getId())
					.header(HttpHeaders.AUTHORIZATION, bearer(owner))
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"resolutionNotes\":\"Trabajo finalizado\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.resolutionNotes").value("Trabajo finalizado"));
	}

	@Test
	void shouldRejectInactiveProviderAndOversizedRange() throws Exception {
		final ProviderFixture fixture = createProvider("inactive");

		mockMvc.perform(get("/api/v1/provider/maintenance/orders")
					.header(HttpHeaders.AUTHORIZATION, bearer(fixture))
					.param("dateFrom", "2026-11-01").param("dateTo", "2026-12-02"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Date range must not exceed 31 days"));

		fixture.provider().update(MaintenanceProviderType.EXTERNAL, fixture.provider().getSpecialty(),
				fixture.provider().getName(), fixture.provider().getServiceLabel(), null, null, false, "supervisor.demo");
		providerRepository.save(fixture.provider());
		mockMvc.perform(get("/api/v1/provider/maintenance/orders")
					.header(HttpHeaders.AUTHORIZATION, bearer(fixture)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Provider access is inactive or no longer operational"));
	}

	private ProviderFixture createProvider(String label) throws Exception {
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final MaintenanceProvider provider = providerRepository.save(MaintenanceProvider.create(
				MaintenanceProviderType.EXTERNAL, MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
				"Provider " + label + " " + suffix, "General", null, null, true, "supervisor.demo"
		));
		final String username = "maintenance." + label + "." + suffix;
		appUserRepository.save(AppUser.create(username, passwordEncoder.encode("123456"), AppUserRole.PROVIDER,
				ProviderAccessType.MAINTENANCE, null, null, provider, null, true));
		return new ProviderFixture(provider, authenticate(username));
	}

	private MaintenanceOrder saveOrder(MaintenanceProvider provider, String title, LocalDateTime start) {
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final MaintenanceLocation location = locationRepository.save(MaintenanceLocation.create(
				MaintenanceLocationType.COMMON_AREA, MaintenanceLocationCategory.COMMON_AREA,
				"AREA-" + suffix, "Area " + suffix, null, null, true, "operator.demo"
		));
		return orderRepository.save(MaintenanceOrder.report(
				location, provider, title, "Technical description", MaintenancePriority.MEDIUM,
				MaintenanceRequestOrigin.INTERNAL_ROLE, false, null, 60, null, start, start.plusHours(1),
				"operator.demo", AppUserRole.OPERATOR.name()
		));
	}

	private String authenticate(String username) throws Exception {
		final String response = mockMvc.perform(post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"%s\",\"password\":\"123456\"}".formatted(username)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).path("accessToken").asText();
	}

	private String bearer(ProviderFixture fixture) {
		return "Bearer " + fixture.token();
	}

	private record ProviderFixture(MaintenanceProvider provider, String token) {
	}
}
