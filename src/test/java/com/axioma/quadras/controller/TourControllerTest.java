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
	void shouldListBookingsUsingFiltersAndOrdering() throws Exception {
		final TourProvider providerA = tourProviderRepository.save(
				TourProvider.create("Agencia A", "a@demo.local", new BigDecimal("10.00"), "system")
		);
		final TourProvider providerB = tourProviderRepository.save(
				TourProvider.create("Agencia B", "b@demo.local", new BigDecimal("12.50"), "system")
		);
		final TourProviderOffering offering = tourProviderOfferingRepository.save(
				TourProviderOffering.create(
						providerA,
						TourServiceType.TOUR,
						"Ilha do Campeche",
						new BigDecimal("350.00"),
						"Passeio com desembarque",
						true,
						"system"
				)
		);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TRAVEL,
						LocalDateTime.of(2026, 4, 1, 8, 0),
						LocalDateTime.of(2026, 4, 1, 9, 0),
						"Cliente fuera",
						"Apto 100",
						providerA,
						null,
						new BigDecimal("180.00"),
						new BigDecimal("10.00"),
						"Fuera de rango",
						false,
						null,
						null,
						null,
						"operador.demo"
				)
		);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 2, 9, 0),
						LocalDateTime.of(2026, 4, 2, 11, 0),
						"Helena",
						"Apto 101",
						providerA,
						offering,
						new BigDecimal("350.00"),
						new BigDecimal("10.00"),
						"Passeio principal",
						true,
						TourPaymentMethod.PIX,
						LocalDate.of(2026, 4, 2),
						"Pix",
						"operador.demo"
				)
		);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 2, 12, 0),
						LocalDateTime.of(2026, 4, 2, 14, 0),
						"Bruno",
						"Apto 102",
						providerB,
						null,
						new BigDecimal("420.00"),
						new BigDecimal("12.50"),
						"Otro proveedor",
						true,
						TourPaymentMethod.CARD,
						LocalDate.of(2026, 4, 2),
						"Tarjeta",
						"operador.demo"
				)
		);

		mockMvc.perform(get("/api/v1/tours/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-02")
						.param("dateTo", "2026-04-02")
						.param("providerId", String.valueOf(providerA.getId()))
						.param("paid", "true")
						.param("serviceType", "TOUR"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].clientName").value("Helena"))
				.andExpect(jsonPath("$[0].providerId").value(providerA.getId()))
				.andExpect(jsonPath("$[0].providerOfferingId").value(offering.getId()))
				.andExpect(jsonPath("$[0].providerOfferingName").value("Ilha do Campeche"))
				.andExpect(jsonPath("$[0].paymentMethod").value("PIX"));
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
	void shouldReturnPeriodSummary() throws Exception {
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
						LocalDateTime.of(2026, 4, 11, 7, 30),
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
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 13, 8, 0),
						LocalDateTime.of(2026, 4, 13, 10, 30),
						"Maria",
						"Apto 104",
						providerB,
						null,
						new BigDecimal("500.00"),
						new BigDecimal("20.00"),
						"Tour ilha",
						true,
						TourPaymentMethod.PIX,
						LocalDate.of(2026, 4, 13),
						"Pix",
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

		mockMvc.perform(get("/api/v1/tours/reports/summary")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledCount").value(3))
				.andExpect(jsonPath("$.cancelledCount").value(1))
				.andExpect(jsonPath("$.paidCount").value(2))
				.andExpect(jsonPath("$.pendingCount").value(1))
				.andExpect(jsonPath("$.totalHours").value(7.00))
				.andExpect(jsonPath("$.grossAmount").value(1200.00))
				.andExpect(jsonPath("$.paidAmount").value(900.00))
				.andExpect(jsonPath("$.pendingAmount").value(300.00))
				.andExpect(jsonPath("$.commissionAmount").value(170.00))
				.andExpect(jsonPath("$.netAmount").value(1030.00))
				.andExpect(jsonPath("$.averageTicket").value(400.00))
				.andExpect(jsonPath("$.providerBreakdown.length()").value(2))
				.andExpect(jsonPath("$.providerBreakdown[0].label").value("Agencia A"))
				.andExpect(jsonPath("$.providerBreakdown[0].scheduledCount").value(2))
				.andExpect(jsonPath("$.providerBreakdown[0].grossAmount").value(700.00))
				.andExpect(jsonPath("$.serviceTypeBreakdown[0].code").value("TOUR"))
				.andExpect(jsonPath("$.serviceTypeBreakdown[0].scheduledCount").value(2))
				.andExpect(jsonPath("$.serviceTypeBreakdown[1].code").value("TRAVEL"))
				.andExpect(jsonPath("$.serviceTypeBreakdown[1].pendingAmount").value(300.00))
				.andExpect(jsonPath("$.paymentMethodBreakdown[0].code").value("PIX"))
				.andExpect(jsonPath("$.paymentMethodBreakdown[0].grossAmount").value(500.00))
				.andExpect(jsonPath("$.paymentMethodBreakdown[1].code").value("CARD"))
				.andExpect(jsonPath("$.paymentMethodBreakdown[1].grossAmount").value(400.00))
				.andExpect(jsonPath("$.paymentMethodBreakdown[2].code").value("CASH"))
				.andExpect(jsonPath("$.paymentMethodBreakdown[2].grossAmount").value(0.00))
				.andExpect(jsonPath("$.paymentMethodBreakdown[3].code").value("TRANSFER"))
				.andExpect(jsonPath("$.paymentMethodBreakdown[3].grossAmount").value(0.00));
	}

	@Test
	void shouldRejectInvalidSummaryRange() throws Exception {
		mockMvc.perform(get("/api/v1/tours/reports/summary")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-04-30")
						.param("dateTo", "2026-04-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("dateFrom must be before or equal to dateTo"));
	}

	@Test
	void shouldReturnProviderSummaryDetails() throws Exception {
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
						LocalDateTime.of(2026, 4, 11, 7, 30),
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
						providerA,
						null,
						new BigDecimal("800.00"),
						new BigDecimal("10.00"),
						"Tour cancelado",
						false,
						null,
						null,
						null,
						"operador.demo"
				)
		);
		cancelled.markCancelled("Clima ruim", "operador.demo");
		tourBookingRepository.save(cancelled);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TOUR,
						LocalDateTime.of(2026, 4, 13, 8, 0),
						LocalDateTime.of(2026, 4, 13, 10, 30),
						"Maria",
						"Apto 104",
						providerB,
						null,
						new BigDecimal("500.00"),
						new BigDecimal("20.00"),
						"Tour ilha",
						true,
						TourPaymentMethod.PIX,
						LocalDate.of(2026, 4, 13),
						"Pix",
						"operador.demo"
				)
		);

		mockMvc.perform(get("/api/v1/tours/reports/summary/details")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("groupBy", "PROVIDER")
						.param("code", String.valueOf(providerA.getId()))
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.groupBy").value("PROVIDER"))
				.andExpect(jsonPath("$.code").value(String.valueOf(providerA.getId())))
				.andExpect(jsonPath("$.label").value("Agencia A"))
				.andExpect(jsonPath("$.summary.scheduledCount").value(2))
				.andExpect(jsonPath("$.summary.grossAmount").value(700.00))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].clientName").value("Laura"))
				.andExpect(jsonPath("$.items[1].serviceType").value("TRAVEL"));
	}

	@Test
	void shouldReturnServiceTypeSummaryDetails() throws Exception {
		final TourProvider provider = tourProviderRepository.save(
				TourProvider.create("Agencia A", "a@demo.local", new BigDecimal("10.00"), "system")
		);
		tourBookingRepository.save(
				TourBooking.schedule(
						TourServiceType.TRAVEL,
						LocalDateTime.of(2026, 4, 10, 9, 0),
						LocalDateTime.of(2026, 4, 10, 10, 0),
						"Laura",
						"Apto 101",
						provider,
						null,
						new BigDecimal("200.00"),
						new BigDecimal("10.00"),
						"Transfer hotel",
						true,
						TourPaymentMethod.CARD,
						LocalDate.of(2026, 4, 10),
						"Pago",
						"operador.demo"
				)
		);

		mockMvc.perform(get("/api/v1/tours/reports/summary/details")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("groupBy", "SERVICE_TYPE")
						.param("code", "TRAVEL")
						.param("dateFrom", "2026-04-01")
						.param("dateTo", "2026-04-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.label").value("Traslado"))
				.andExpect(jsonPath("$.summary.scheduledCount").value(1))
				.andExpect(jsonPath("$.items[0].description").value("Transfer hotel"));
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
	void shouldListTourProvidersOrderedWithOfferingsAndActiveFilter() throws Exception {
		final TourProvider inactiveProvider = tourProviderRepository.save(
				TourProvider.create("Zeta Travel", "zeta@demo.local", new BigDecimal("15.00"), "system")
		);
		inactiveProvider.update("Zeta Travel", "zeta@demo.local", new BigDecimal("15.00"), false, "system");
		tourProviderRepository.save(inactiveProvider);
		final TourProvider activeProvider = tourProviderRepository.save(
				TourProvider.create("Agencia Azul", "azul@demo.local", new BigDecimal("12.50"), "system")
		);
		tourProviderOfferingRepository.save(
				TourProviderOffering.create(
						activeProvider,
						TourServiceType.TRAVEL,
						"Transfer Privado",
						new BigDecimal("140.00"),
						"Saida hotel",
						true,
						"system"
				)
		);
		tourProviderOfferingRepository.save(
				TourProviderOffering.create(
						activeProvider,
						TourServiceType.TOUR,
						"Passeio Lagoa",
						new BigDecimal("280.00"),
						"Tour panoramico",
						true,
						"system"
				)
		);
		tourProviderOfferingRepository.save(
				TourProviderOffering.create(
						inactiveProvider,
						TourServiceType.TOUR,
						"Ilha Norte",
						new BigDecimal("310.00"),
						"Rota completa",
						true,
						"system"
				)
		);

		mockMvc.perform(get("/api/v1/tours/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("activeOnly", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(activeProvider.getId()))
				.andExpect(jsonPath("$[0].name").value("Agencia Azul"))
				.andExpect(jsonPath("$[0].offerings.length()").value(2))
				.andExpect(jsonPath("$[0].offerings[0].name").value("Passeio Lagoa"))
				.andExpect(jsonPath("$[0].offerings[1].name").value("Transfer Privado"));
	}

	@Test
	void shouldPreserveExistingOfferingWhenProviderUpdateKeepsSameName() throws Exception {
		final String createResponse = mockMvc.perform(post("/api/v1/tours/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Agencia Delta",
								  "contact": "delta@demo.local",
								  "defaultCommissionPercent": 15.00,
								  "offerings": [
								    {
								      "serviceType": "TOUR",
								      "name": "City Tour",
								      "amount": 210.00,
								      "description": "Circuito historico",
								      "active": true
								    }
								  ]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode createdProvider = objectMapper.readTree(createResponse);
		final long providerId = createdProvider.get("id").asLong();
		final long existingOfferingId = createdProvider.get("offerings").get(0).get("id").asLong();

		final String updateResponse = mockMvc.perform(put("/api/v1/tours/providers/{id}", providerId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Agencia Delta",
								  "contact": "delta@demo.local",
								  "defaultCommissionPercent": 16.50,
								  "active": true,
								  "offerings": [
								    {
								      "serviceType": "TOUR",
								      "name": "City Tour",
								      "amount": 240.00,
								      "description": "Circuito historico premium",
								      "active": false
								    },
								    {
								      "serviceType": "TRAVEL",
								      "name": "Transfer Aeropuerto",
								      "amount": 130.00,
								      "description": "Traslado privado",
								      "active": true
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.offerings.length()").value(2))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode updatedProvider = objectMapper.readTree(updateResponse);
		final JsonNode preservedOffering = updatedProvider.get("offerings").get(0);

		org.junit.jupiter.api.Assertions.assertEquals(existingOfferingId, preservedOffering.get("id").asLong());
		org.junit.jupiter.api.Assertions.assertEquals(2, tourProviderOfferingRepository.count());
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
