package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
		"spring.jpa.hibernate.ddl-auto=validate",
		"costanorte.laundry.v1.write-enabled=false"
})
class LaundryV1WriteCutoffTest {
	@Autowired private MockMvc mockMvc;

	@Test
	void shouldKeepV1ReadsAndRejectV1WritesAfterCutoff() throws Exception {
		String token = authenticate();
		mockMvc.perform(get("/api/v1/laundry/items")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/laundry/providers")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Bloqueado\",\"active\":true}"))
				.andExpect(status().isGone())
				.andExpect(jsonPath("$.message").value(
						"Laundry V1 writes are disabled; use /api/v2/laundry"));
	}

	private String authenticate() throws Exception {
		String body = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"operador.demo\",\"password\":\"123456\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return new com.fasterxml.jackson.databind.ObjectMapper().readTree(body)
				.get("accessToken").asText();
	}
}
