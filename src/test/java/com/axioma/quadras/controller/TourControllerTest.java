package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.TourBooking;
import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourPaymentMethod;
import com.axioma.quadras.domain.model.TourProvider;
import com.axioma.quadras.domain.model.TourProviderOffering;
import com.axioma.quadras.domain.model.TourServiceType;
import com.axioma.quadras.repository.TourBookingRepository;
import com.axioma.quadras.repository.TourProviderRepository;
import com.axioma.quadras.repository.TourProviderOfferingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class TourControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TourBookingRepository tourBookingRepository;

	@Autowired
	private TourProviderRepository tourProviderRepository;

	@Autowired
	private TourProviderOfferingRepository tourProviderOfferingRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		tourBookingRepository.deleteAll();
		tourProviderOfferingRepository.deleteAll();
		tourProviderRepository.deleteAll();
		accessToken = authenticate();
	}

	@Test
	void shouldCreateOverlappingBookings() throws Exception {
		final TourProvider providerA = tourProviderRepository.save(
				TourProvider.create("Agencia A", "a@demo.local", new BigDecimal("10.00"), "system")
		);
		final TourProvider providerB = tourProviderRepository.save(
				TourProvider.create("Agencia B", "b@demo.local", new BigDecimal("12.50"), "system")
		);

		final String firstPayload = """
				{
				  "serviceType": "TOUR",
				  "startAt": "2026-04-02T09:00:00",
				  "endAt": "2026-04-02T11:00:00",
				  "clientName": "Helena",
				  "guestReference": "Apto 101",
				  "providerId": %d,
				  "providerOfferingId": null,
				  "amount": 350.00,
				  "commissionPercent": 10.00,
				  "description": "Passeio de barco",
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null
				}
				""".formatted(providerA.getId());
		final String secondPayload = """
				{
				  "serviceType": "TRAVEL",
				  "startAt": "2026-04-02T09:30:00",
				  "endAt": "2026-04-02T12:30:00",
				  "clientName": "Bruno",
				  "guestReference": "Apto 202",
				  "providerId": %d,
				  "providerOfferingId": null,
				  "amount": 500.00,
				  "commissionPercent": 12.50,
				  "description": "Traslado executivo",
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null
				}
				""".formatted(providerB.getId());

		mockMvc.perform(post("/api/v1/tours/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(firstPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commissionAmount").value(35.00));

		mockMvc.perform(post("/api/v1/tours/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(secondPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commissionAmount").value(62.50));

		mockMvc.perform(get("/api/v1/tours/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-02")
						.param("dateTo", "2026-04-02"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void shouldUpdatePaymentAndCancelBooking() throws Exception {
		final TourProvider provider = tourProviderRepository.save(
				TourProvider.create("Agencia A", "a@demo.local", new BigDecimal("10.00"), "system")
		);
		final TourBooking booking = tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 5, 10, 0),
						LocalDateTime.of(2026, 4, 5, 13, 0),
						"Camila",
						"Apto 305",
						provider,
						null,
						new BigDecimal("600.00"),
						new BigDecimal("15.00"),
						"Tour historico",
						false,
						null,
						null,
						null,
						"operador.demo"
				)
		);

		mockMvc.perform(patch("/api/v1/tours/bookings/{id}/payment", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "paymentMethod": "PIX",
								  "paymentDate": "2026-04-05",
								  "paymentNotes": "Pago no check-out"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paid").value(true))
				.andExpect(jsonPath("$.paymentMethod").value("PIX"));

		mockMvc.perform(patch("/api/v1/tours/bookings/{id}/cancel", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "cancellationNotes": "Cliente desistiu"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"))
				.andExpect(jsonPath("$.cancellationNotes").value("Cliente desistiu"));
	}

	@Test
	void shouldReturnProviderSummary() throws Exception {
		final TourProvider providerA = tourProviderRepository.save(
				TourProvider.create("Agencia A", "a@demo.local", new BigDecimal("10.00"), "system")
		);
		final TourProvider providerB = tourProviderRepository.save(
				TourProvider.create("Agencia B", "b@demo.local", new BigDecimal("20.00"), "system")
		);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 10, 9, 0),
						LocalDateTime.of(2026, 4, 10, 12, 0),
						"Laura",
						"Apto 101",
						providerA,
						null,
						new BigDecimal("400.00"),
						new BigDecimal("10.00"),
						"Paseo maritimo",
						true,
						TourPaymentMethod.CARD,
						LocalDate.of(2026, 4, 10),
						"Pago",
						"operador.demo"
				)
		);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TRAVEL,
						LocalDateTime.of(2026, 4, 11, 7, 0),
						LocalDateTime.of(2026, 4, 11, 9, 0),
						"Diego",
						"Apto 102",
						providerA,
						null,
						new BigDecimal("300.00"),
						new BigDecimal("10.00"),
						"Traslado",
						false,
						null,
						null,
						null,
						"operador.demo"
				)
		);
		final TourBooking cancelled = tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 12, 8, 0),
						LocalDateTime.of(2026, 4, 12, 11, 0),
						"Pedro",
						"Apto 103",
						providerB,
						null,
						new BigDecimal("800.00"),
						new BigDecimal("20.00"),
						"Tour integral",
						false,
						null,
						null,
						null,
						"operador.demo"
				)
		);
		cancelled.markCancelled("Clima ruim", "operador.demo");
		tourBookingRepository.save(cancelled);

		mockMvc.perform(get("/api/v1/tours/reports/providers/summary")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].providerName").value("Agencia A"))
				.andExpect(jsonPath("$[0].scheduledCount").value(2))
				.andExpect(jsonPath("$[0].paidCount").value(1))
				.andExpect(jsonPath("$[0].pendingCount").value(1))
				.andExpect(jsonPath("$[0].grossAmount").value(700.00))
				.andExpect(jsonPath("$[0].commissionAmount").value(70.00))
				.andExpect(jsonPath("$[1].providerName").value("Agencia B"))
				.andExpect(jsonPath("$[1].cancelledCount").value(1))
				.andExpect(jsonPath("$[1].scheduledCount").value(0));
	}

	@Test
	void shouldCreateAndUpdateProvider() throws Exception {
		final String createPayload = """
				{
				  "name": "Agencia Nova",
				  "contact": "nova@demo.local",
				  "defaultCommissionPercent": 18.50,
				  "offerings": [
				    {
				      "serviceType": "TOUR",
				      "name": "Ilha do Campeche",
				      "amount": 350.00,
				      "description": "Passeio com desembarque",
				      "active": true
				    }
				  ]
				}
				""";

		final String responseBody = mockMvc.perform(post("/api/v1/tours/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(createPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Agencia Nova"))
				.andExpect(jsonPath("$.offerings.length()").value(1))
				.andExpect(jsonPath("$.offerings[0].name").value("Ilha do Campeche"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final long providerId = objectMapper.readTree(responseBody).get("id").asLong();

		mockMvc.perform(put("/api/v1/tours/providers/{id}", providerId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Agencia Nova",
								  "contact": "nova@demo.local",
								  "defaultCommissionPercent": 20.00,
								  "active": false,
								  "offerings": [
								    {
								      "serviceType": "TRAVEL",
								      "name": "Traslado VIP",
								      "amount": 500.00,
								      "description": "Transfer privado",
								      "active": true
								    },
								    {
								      "serviceType": "TOUR",
								      "name": "Passeio Centro",
								      "amount": 180.00,
								      "description": "Com paradas",
								      "active": false
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.defaultCommissionPercent").value(20.00))
				.andExpect(jsonPath("$.active").value(false))
				.andExpect(jsonPath("$.offerings.length()").value(2))
				.andExpect(jsonPath("$.offerings[0].name").value("Passeio Centro"))
				.andExpect(jsonPath("$.offerings[1].name").value("Traslado VIP"));
	}

	@Test
	void shouldCreateBookingUsingProviderOffering() throws Exception {
		final TourProvider provider = tourProviderRepository.save(
				TourProvider.create("Agencia A", "a@demo.local", new BigDecimal("10.00"), "system")
		);
		final TourProviderOffering offering = tourProviderOfferingRepository.save(
				TourProviderOffering.create(
						provider,
						TourServiceType.TOUR,
						"Ilha do Campeche",
						new BigDecimal("350.00"),
						"Passeio com desembarque",
						true,
						"system"
				)
		);

		mockMvc.perform(post("/api/v1/tours/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "serviceType": "TOUR",
								  "startAt": "2026-04-02T09:00:00",
								  "endAt": "2026-04-02T11:00:00",
								  "clientName": "Helena",
								  "guestReference": "Apto 101",
								  "providerId": %d,
								  "providerOfferingId": %d,
								  "amount": 350.00,
								  "commissionPercent": 10.00,
								  "description": "Passeio com desembarque e traslado",
								  "paid": false,
								  "paymentMethod": null,
								  "paymentDate": null,
								  "paymentNotes": null
								}
								""".formatted(provider.getId(), offering.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.providerOfferingId").value(offering.getId()))
				.andExpect(jsonPath("$.providerOfferingName").value("Ilha do Campeche"));
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
