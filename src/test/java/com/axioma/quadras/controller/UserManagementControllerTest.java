package com.axioma.quadras.controller;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.CourtPartnerCoach;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.axioma.quadras.repository.MassageProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import com.axioma.quadras.config.JwtProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.flyway.enabled=true",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class UserManagementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private LaundryProviderRepository laundryProviderRepository;

	@Autowired
	private CourtPartnerCoachRepository courtPartnerCoachRepository;

	@Autowired
	private MassageProviderRepository massageProviderRepository;

	@Autowired
	private MassageTherapistRepository massageTherapistRepository;

	@Autowired
	private MaintenanceProviderRepository maintenanceProviderRepository;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtProperties jwtProperties;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		final Set<String> preservedUsers = Set.of("operador.demo", "supervisor.demo");
		appUserRepository.findAll().stream()
				.filter(user -> !preservedUsers.contains(user.getUsername()))
				.map(AppUser::getId)
				.toList()
				.forEach(appUserRepository::deleteById);
		createOrUpdateUser("operador.demo", "123456", AppUserRole.OPERATOR, true);
		createOrUpdateUser("supervisor.demo", "654321", AppUserRole.SUPERVISOR, true);
	}

	@Test
	void supervisorShouldListUsers() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");

		mockMvc.perform(get("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].username").value("operador.demo"))
				.andExpect(jsonPath("$[1].username").value("supervisor.demo"));
	}

	@Test
	void supervisorShouldAccessOperatorEndpoints() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");

		mockMvc.perform(get("/api/v1/courts/rates")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void operatorShouldNotAccessUserAdministrationEndpoints() throws Exception {
		final String operatorToken = authenticate("operador.demo", "123456");

		mockMvc.perform(get("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("The current role is not allowed to access this resource."));
	}

	@Test
	void supervisorShouldCreateOperatorUser() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");

		mockMvc.perform(post("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "nuevo.operador",
								  "password": "111111",
								  "role": "OPERATOR",
								  "enabled": true
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("nuevo.operador"))
				.andExpect(jsonPath("$.role").value("OPERATOR"))
				.andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	void supervisorShouldCreateProviderUserWithRequiredOwnership() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final LaundryProvider provider = laundryProviderRepository.save(LaundryProvider.create(
				"Portal " + UUID.randomUUID().toString().substring(0, 8), null, true));

		mockMvc.perform(post("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"portal.proveedor","password":"111111","role":"PROVIDER","laundryProviderId":%d,"enabled":true}
								""".formatted(provider.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("PROVIDER"))
				.andExpect(jsonPath("$.laundryProviderId").value(provider.getId()));

		final String providerToken = authenticate("portal.proveedor", "111111");
		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("PROVIDER"))
				.andExpect(jsonPath("$.laundryProviderId").value(provider.getId()));
	}

	@Test
	void supervisorShouldCreateAndAuthenticateEveryGenericProviderType() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final CourtPartnerCoach coach = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create("Coach " + suffix, "supervisor.demo"));
		final MassageProvider massageProvider = massageProviderRepository.save(
				MassageProvider.create("Spa " + suffix, "Massagem", "contato"));
		final MassageTherapist therapist = massageTherapistRepository.save(
				MassageTherapist.create(massageProvider, "Therapist " + suffix));
		final MaintenanceProvider maintenanceProvider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						MaintenanceProviderType.EXTERNAL,
						MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
						"Maintenance " + suffix,
						"General", null, "contato", true, "supervisor.demo"));
		final LaundryProvider laundryProvider = laundryProviderRepository.save(
				LaundryProvider.create("Laundry " + suffix, null, true));

		assertGenericProvider(supervisorToken, "provider.courts." + suffix,
				"COURTS", coach.getId(), coach.getName(), null);
		assertGenericProvider(supervisorToken, "provider.massage." + suffix,
				"MASSAGE", therapist.getId(), therapist.getName(), null);
		assertGenericProvider(supervisorToken, "provider.maintenance." + suffix,
				"MAINTENANCE", maintenanceProvider.getId(), maintenanceProvider.getName(), null);
		assertGenericProvider(supervisorToken, "provider.laundry." + suffix,
				"LAUNDRY", laundryProvider.getId(), laundryProvider.getName(), laundryProvider.getId());
	}

	@Test
	void supervisorShouldNotCreateTwoAccessAccountsForTheSameProviderResource() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final CourtPartnerCoach coach = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create("Coach " + UUID.randomUUID(), "supervisor.demo"));

		assertGenericProvider(supervisorToken, "provider.first." + coach.getId(),
				"COURTS", coach.getId(), coach.getName(), null);

		mockMvc.perform(post("/api/v1/users")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"username":"provider.second.%d","password":"111111","role":"PROVIDER","providerType":"COURTS","providerResourceId":%d,"enabled":true}
							""".formatted(coach.getId(), coach.getId())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("The provider resource already has an access account."));
	}

	@Test
	void shouldRejectInactiveOrInternalProviderResources() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final CourtPartnerCoach inactiveCoach = CourtPartnerCoach.create(
				"Inactive " + suffix, "supervisor.demo");
		inactiveCoach.update(inactiveCoach.getName(), false, "supervisor.demo");
		courtPartnerCoachRepository.save(inactiveCoach);
		final MaintenanceProvider internalProvider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						MaintenanceProviderType.INTERNAL,
						MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
						"Internal " + suffix,
						"General", null, null, true, "supervisor.demo"));

		createProviderRequest(supervisorToken, "inactive." + suffix, "COURTS", inactiveCoach.getId())
				.andExpect(status().isBadRequest());
		createProviderRequest(supervisorToken, "internal." + suffix, "MAINTENANCE", internalProvider.getId())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectJwtWhenProviderResourceClaimDoesNotMatchPersistedAccess() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final CourtPartnerCoach coach = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create("JWT Coach " + suffix, "supervisor.demo"));
		final String username = "provider.jwt." + suffix;
		createProviderRequest(supervisorToken, username, "COURTS", coach.getId())
				.andExpect(status().isCreated());
		final AppUser user = appUserRepository.findByUsername(username).orElseThrow();
		final String forgedToken = forgeProviderToken(
				username, user.getSecurityVersion(), "COURTS", coach.getId() + 1L);

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + forgedToken))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid JWT token."));
	}

	@Test
	void shouldInvalidateRealJwtAfterProviderReassignmentRoleChangeAndDisable() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final CourtPartnerCoach firstCoach = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create("First Coach " + suffix, "supervisor.demo"));
		final CourtPartnerCoach secondCoach = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create("Second Coach " + suffix, "supervisor.demo"));
		final String username = "provider.lifecycle." + suffix;

		createProviderRequest(supervisorToken, username, "COURTS", firstCoach.getId())
				.andExpect(status().isCreated());
		final Long userId = appUserRepository.findByUsername(username).orElseThrow().getId();
		final String firstToken = authenticate(username, "111111");

		mockMvc.perform(put("/api/v1/users/{userId}", userId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"role":"PROVIDER","providerType":"COURTS","providerResourceId":%d,"enabled":true}
							""".formatted(secondCoach.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.providerAccess.resourceId").value(secondCoach.getId()));
		assertTokenInvalid(firstToken);

		final String secondToken = authenticate(username, "111111");
		mockMvc.perform(put("/api/v1/users/{userId}", userId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"role\":\"OPERATOR\",\"enabled\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("OPERATOR"))
				.andExpect(jsonPath("$.providerAccess").doesNotExist());
		assertTokenInvalid(secondToken);

		final String operatorToken = authenticate(username, "111111");
		mockMvc.perform(put("/api/v1/users/{userId}", userId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"role\":\"OPERATOR\",\"enabled\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled").value(false));
		assertTokenInvalid(operatorToken);
	}

	@Test
	void shouldRejectProviderWithoutOwnershipAndInternalUserWithProvider() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final LaundryProvider provider = laundryProviderRepository.save(LaundryProvider.create(
				"Validacao " + UUID.randomUUID().toString().substring(0, 8), null, true));

		mockMvc.perform(post("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"sem.proveedor","password":"111111","role":"PROVIDER","enabled":true}
								"""))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"operador.vinculado","password":"111111","role":"OPERATOR","laundryProviderId":%d,"enabled":true}
								""".formatted(provider.getId())))
				.andExpect(status().isBadRequest());
	}

	@Test
	void supervisorShouldResetPasswordAndInvalidatePreviousJwt() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final String operatorToken = authenticate("operador.demo", "123456");
		final Long operatorId = appUserRepository.findByUsername("operador.demo").orElseThrow().getId();

		mockMvc.perform(patch("/api/v1/users/{userId}/password", operatorId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "newPassword": "222222"
								}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid JWT token."));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload("operador.demo", "222222")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("operador.demo"));
	}

	@Test
	void userShouldChangeOwnPasswordAndInvalidatePreviousJwt() throws Exception {
		final String operatorToken = authenticate("operador.demo", "123456");

		mockMvc.perform(patch("/api/v1/users/me/password")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "123456",
								  "newPassword": "333333"
								}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid JWT token."));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload("operador.demo", "333333")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("operador.demo"));
	}

	@Test
	void shouldRejectDisablingLastEnabledSupervisor() throws Exception {
		final String supervisorToken = authenticate("supervisor.demo", "654321");
		final Long supervisorId = appUserRepository.findByUsername("supervisor.demo").orElseThrow().getId();

		mockMvc.perform(put("/api/v1/users/{userId}", supervisorId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "role": "SUPERVISOR",
								  "enabled": false
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You cannot disable your own account."));
	}

	private void createOrUpdateUser(String username, String rawPassword, AppUserRole role, boolean enabled) {
		final String normalizedUsername = AppUser.normalizeUsername(username);
		final AppUser appUser = appUserRepository.findByUsername(normalizedUsername)
				.orElseGet(() -> AppUser.create(
						normalizedUsername,
						passwordEncoder.encode(rawPassword),
						role,
						enabled
				));
		appUser.updateRoleAndStatus(role, enabled);
		if (!passwordEncoder.matches(rawPassword, appUser.getPasswordHash())) {
			appUser.changePasswordHash(passwordEncoder.encode(rawPassword));
		}
		appUserRepository.save(appUser);
	}

	private String authenticate(String username, String password) throws Exception {
		final String responseBody = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload(username, password)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode response = objectMapper.readTree(responseBody);
		return response.get("accessToken").asText();
	}

	private void assertTokenInvalid(String token) throws Exception {
		mockMvc.perform(get("/api/v1/users/me")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid JWT token."));
	}

	private void assertGenericProvider(String supervisorToken, String username, String type,
			Long resourceId, String displayName, Long legacyLaundryProviderId) throws Exception {
		createProviderRequest(supervisorToken, username, type, resourceId)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.providerAccess.type").value(type))
				.andExpect(jsonPath("$.providerAccess.resourceId").value(resourceId))
				.andExpect(jsonPath("$.providerAccess.displayName").value(displayName));

		final String responseBody = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload(username, "111111")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.providerAccess.type").value(type))
				.andExpect(jsonPath("$.providerAccess.resourceId").value(resourceId))
				.andReturn().getResponse().getContentAsString();
		final String token = objectMapper.readTree(responseBody).get("accessToken").asText();

		final var currentUser = mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.providerAccess.type").value(type))
				.andExpect(jsonPath("$.providerAccess.resourceId").value(resourceId));
		if (legacyLaundryProviderId == null) {
			currentUser.andExpect(jsonPath("$.laundryProviderId").doesNotExist());
		} else {
			currentUser.andExpect(jsonPath("$.laundryProviderId").value(legacyLaundryProviderId));
		}
	}

	private org.springframework.test.web.servlet.ResultActions createProviderRequest(
			String supervisorToken, String username, String type, Long resourceId) throws Exception {
		return mockMvc.perform(post("/api/v1/users")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supervisorToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"111111","role":"PROVIDER","providerType":"%s","providerResourceId":%d,"enabled":true}
						""".formatted(username, type, resourceId)));
	}

	private String forgeProviderToken(String username, long securityVersion,
			String providerType, Long providerResourceId) {
		final Instant issuedAt = Instant.now();
		final JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(jwtProperties.expirationSeconds()))
				.subject(username)
				.claim("role", "PROVIDER")
				.claim("securityVersion", securityVersion)
				.claim("providerType", providerType)
				.claim("providerResourceId", providerResourceId)
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
	}

	private String loginPayload(String username, String password) {
		return """
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password);
	}
}
