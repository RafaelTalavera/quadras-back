package com.axioma.quadras.controller;

import com.axioma.quadras.config.JwtProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.flyway.enabled=true",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtProperties jwtProperties;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldAuthenticateAndReturnJwt() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload("operador.demo", "Costanorte2026!")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresInSeconds").value(28800))
				.andExpect(jsonPath("$.username").value("operador.demo"))
				.andExpect(jsonPath("$.role").value("OPERATOR"));
	}

	@Test
	void shouldRejectInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload("operador.demo", "clave-invalida")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid username or password."));
	}

	@Test
	void shouldRequireTokenForProtectedReservationsEndpoint() throws Exception {
		mockMvc.perform(get("/api/v1/reservations"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
	}

	@Test
	void shouldReturnCurrentUserWhenTokenIsValid() throws Exception {
		final String accessToken = authenticate();

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("operador.demo"))
				.andExpect(jsonPath("$.role").value("OPERATOR"));
	}

	@Test
	void shouldRejectJwtWhenRoleClaimIsInvalid() throws Exception {
		final String forgedToken = forgeToken("operador.demo", "ADMIN");

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + forgedToken))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid JWT token."));
	}

	private String authenticate() throws Exception {
		final String responseBody = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload("operador.demo", "Costanorte2026!")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode response = objectMapper.readTree(responseBody);
		return response.get("accessToken").asText();
	}

	private String forgeToken(String username, String role) {
		final Instant issuedAt = Instant.now();
		final JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(jwtProperties.expirationSeconds()))
				.subject(username)
				.claim("role", role)
				.build();
		final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
				.type("JWT")
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
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
