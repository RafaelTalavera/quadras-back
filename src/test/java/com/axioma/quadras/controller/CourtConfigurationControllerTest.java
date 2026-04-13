package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CourtConfigurationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldListActivePartnerCoaches() throws Exception {
		mockMvc.perform(get("/api/v1/courts/partner-coaches")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].name").value("Professor parceiro 1"))
				.andExpect(jsonPath("$[1].name").value("Professor parceiro 2"))
				.andExpect(jsonPath("$[2].name").value("Professor parceiro 3"));
	}

	@Test
	void shouldListCourtRates() throws Exception {
		mockMvc.perform(get("/api/v1/courts/rates")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(8))
				.andExpect(jsonPath("$[0].customerType").value("EXTERNAL"))
				.andExpect(jsonPath("$[0].pricingPeriod").value("DAY"))
				.andExpect(jsonPath("$[0].amount").value(60.00));
	}

	@Test
	void shouldListCourtMaterials() throws Exception {
		mockMvc.perform(get("/api/v1/courts/materials")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].code").value("BALL"))
				.andExpect(jsonPath("$[0].label").value("Pelota"))
				.andExpect(jsonPath("$[1].code").value("RACKET"))
				.andExpect(jsonPath("$[1].label").value("Raqueta"));
	}

	@Test
	void shouldCreatePartnerCoach() throws Exception {
		final String payload = """
				{
				  "name": "Marcos Silva"
				}
				""";

		mockMvc.perform(post("/api/v1/courts/partner-coaches")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.name").value("Marcos Silva"))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void shouldUpdatePartnerCoach() throws Exception {
		final String payload = """
				{
				  "name": "Professor parceiro 1 atualizado",
				  "active": false
				}
				""";

		mockMvc.perform(put("/api/v1/courts/partner-coaches/1")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Professor parceiro 1 atualizado"))
				.andExpect(jsonPath("$.active").value(false));
	}

	private String bearerToken() throws Exception {
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
		return "Bearer " + response.get("accessToken").asText();
	}
}
