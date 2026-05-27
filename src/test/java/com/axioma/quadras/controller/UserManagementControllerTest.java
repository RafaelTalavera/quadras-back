package com.axioma.quadras.controller;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.repository.AppUserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
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

	private String loginPayload(String username, String password) {
		return """
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password);
	}
}
