package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtBookingMaterial;
import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtMaterialCode;
import com.axioma.quadras.domain.model.CourtPaymentMethod;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import com.axioma.quadras.repository.CourtBookingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class CourtBookingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CourtBookingRepository courtBookingRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		courtBookingRepository.deleteAll();
		accessToken = authenticate();
	}

	@Test
	void shouldReturnExpandedCourtSummary() throws Exception {
		courtBookingRepository.saveAll(List.of(
				scheduledBooking(
						LocalDate.of(2026, 3, 10),
						LocalTime.of(9, 0),
						LocalTime.of(10, 0),
						"Helena",
						CourtCustomerType.GUEST,
						CourtPricingPeriod.DAY,
						new BigDecimal("0.00"),
						new BigDecimal("40.00"),
						new BigDecimal("40.00"),
						true,
						CourtPaymentMethod.PIX
				),
				scheduledBooking(
						LocalDate.of(2026, 3, 10),
						LocalTime.of(19, 0),
						LocalTime.of(20, 30),
						"Bruno",
						CourtCustomerType.EXTERNAL,
						CourtPricingPeriod.NIGHT,
						new BigDecimal("120.00"),
						new BigDecimal("20.00"),
						new BigDecimal("140.00"),
						false,
						null
				),
				cancelledBooking(
						LocalDate.of(2026, 3, 12),
						LocalTime.of(18, 0),
						LocalTime.of(19, 0),
						"Carla",
						CourtCustomerType.VIP,
						CourtPricingPeriod.NIGHT,
						new BigDecimal("0.00"),
						new BigDecimal("0.00"),
						new BigDecimal("0.00")
				)
		));

		mockMvc.perform(get("/api/v1/courts/bookings/summary")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("dateFrom", "2026-03-01")
						.param("dateTo", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledCount").value(2))
				.andExpect(jsonPath("$.cancelledCount").value(1))
				.andExpect(jsonPath("$.paidCount").value(1))
				.andExpect(jsonPath("$.pendingCount").value(1))
				.andExpect(jsonPath("$.totalHours").value(2.50))
				.andExpect(jsonPath("$.paidAmount").value(40.00))
				.andExpect(jsonPath("$.pendingAmount").value(140.00))
				.andExpect(jsonPath("$.courtAmount").value(120.00))
				.andExpect(jsonPath("$.materialsAmount").value(60.00))
				.andExpect(jsonPath("$.expectedAmount").value(180.00))
				.andExpect(jsonPath("$.averageTicket").value(90.00))
				.andExpect(jsonPath("$.customerTypeBreakdown.length()").value(4))
				.andExpect(jsonPath("$.customerTypeBreakdown[0].code").value("GUEST"))
				.andExpect(jsonPath("$.customerTypeBreakdown[0].scheduledCount").value(1))
				.andExpect(jsonPath("$.customerTypeBreakdown[0].materialsAmount").value(40.00))
				.andExpect(jsonPath("$.customerTypeBreakdown[2].code").value("EXTERNAL"))
				.andExpect(jsonPath("$.customerTypeBreakdown[2].pendingCount").value(1))
				.andExpect(jsonPath("$.pricingPeriodBreakdown[0].code").value("DAY"))
				.andExpect(jsonPath("$.pricingPeriodBreakdown[0].totalAmount").value(40.00))
				.andExpect(jsonPath("$.pricingPeriodBreakdown[1].code").value("NIGHT"))
				.andExpect(jsonPath("$.pricingPeriodBreakdown[1].scheduledCount").value(1))
				.andExpect(jsonPath("$.paymentMethodBreakdown[0].code").value("PIX"))
				.andExpect(jsonPath("$.paymentMethodBreakdown[0].scheduledCount").value(1))
				.andExpect(jsonPath("$.paymentMethodBreakdown[1].scheduledCount").value(0));
	}

	@Test
	void shouldListCourtBookingsWithFiltersAndMaterials() throws Exception {
		courtBookingRepository.saveAll(List.of(
				CourtBooking.schedule(
						LocalDate.of(2026, 3, 14),
						LocalTime.of(8, 0),
						LocalTime.of(9, 0),
						"Helena",
						"Apto 201",
						CourtCustomerType.GUEST,
						CourtPricingPeriod.DAY,
						LocalTime.of(6, 0),
						LocalTime.of(18, 0),
						new BigDecimal("0.00"),
						new BigDecimal("35.00"),
						new BigDecimal("35.00"),
						true,
						CourtPaymentMethod.PIX,
						LocalDate.of(2026, 3, 14),
						"Pago registrado",
						List.of(
								CourtBookingMaterial.of(
										CourtMaterialCode.RACKET,
										"Raquete",
										2,
										new BigDecimal("10.00"),
										new BigDecimal("20.00")
								),
								CourtBookingMaterial.of(
										CourtMaterialCode.BALL,
										"Tubo de bolas",
										1,
										new BigDecimal("15.00"),
										new BigDecimal("15.00")
								)
						),
						"operador.demo"
				),
				scheduledBooking(
						LocalDate.of(2026, 3, 14),
						LocalTime.of(10, 0),
						LocalTime.of(11, 0),
						"Bruno",
						CourtCustomerType.EXTERNAL,
						CourtPricingPeriod.DAY,
						new BigDecimal("80.00"),
						new BigDecimal("0.00"),
						new BigDecimal("80.00"),
						false,
						null
				)
		));

		mockMvc.perform(get("/api/v1/courts/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("bookingDate", "2026-03-14")
						.param("paid", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].customerName").value("Helena"))
				.andExpect(jsonPath("$[0].materials.length()").value(2))
				.andExpect(jsonPath("$[0].materials[0].materialCode").value("RACKET"))
				.andExpect(jsonPath("$[0].materials[1].materialCode").value("BALL"));
	}

	@Test
	void shouldRejectPartnerCoachBookingWithUnknownName() throws Exception {
		final String payload = """
				{
				  "bookingDate": "2026-03-28",
				  "startTime": "09:00",
				  "endTime": "10:00",
				  "customerName": "Professor inexistente",
				  "customerReference": "Cancha 1",
				  "customerType": "PARTNER_COACH",
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null,
				  "materials": []
				}
				""";

		mockMvc.perform(post("/api/v1/courts/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Partner coach name must match an active predefined coach."));
	}

	private CourtBooking scheduledBooking(
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			String customerName,
			CourtCustomerType customerType,
			CourtPricingPeriod pricingPeriod,
			BigDecimal courtAmount,
			BigDecimal materialsAmount,
			BigDecimal totalAmount,
			boolean paid,
			CourtPaymentMethod paymentMethod
	) {
		return CourtBooking.schedule(
				bookingDate,
				startTime,
				endTime,
				customerName,
				"Apto 101",
				customerType,
				pricingPeriod,
				LocalTime.of(6, 0),
				LocalTime.of(18, 0),
				courtAmount,
				materialsAmount,
				totalAmount,
				paid,
				paymentMethod,
				paid ? bookingDate : null,
				paid ? "Pago no balcao" : null,
				List.of(),
				"operador.demo"
		);
	}

	private CourtBooking cancelledBooking(
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			String customerName,
			CourtCustomerType customerType,
			CourtPricingPeriod pricingPeriod,
			BigDecimal courtAmount,
			BigDecimal materialsAmount,
			BigDecimal totalAmount
	) {
		final CourtBooking booking = scheduledBooking(
				bookingDate,
				startTime,
				endTime,
				customerName,
				customerType,
				pricingPeriod,
				courtAmount,
				materialsAmount,
				totalAmount,
				false,
				null
		);
		booking.markCancelled("Cliente desistiu", "operador.demo");
		return booking;
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
