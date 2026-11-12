package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPartnerCoach;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.CourtBookingRepository;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class ProviderCourtBookingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CourtBookingRepository courtBookingRepository;

	@Autowired
	private CourtPartnerCoachRepository courtPartnerCoachRepository;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanBookings() {
		courtBookingRepository.deleteAll();
	}

	@Test
	void shouldListOnlyOwnedBookingsInStableOrderWithReducedContract() throws Exception {
		final ProviderFixture owner = createProvider("owner");
		final ProviderFixture other = createProvider("other");
		final CourtBooking later = saveBooking(owner.coach(), LocalDate.of(2026, 11, 12),
				LocalTime.of(10, 0), "Owner later");
		final CourtBooking earlier = saveBooking(owner.coach(), LocalDate.of(2026, 11, 12),
				LocalTime.of(8, 0), "Owner earlier");
		saveBooking(other.coach(), LocalDate.of(2026, 11, 12), LocalTime.of(9, 0), "Other");

		mockMvc.perform(get("/api/v1/provider/courts/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearer(owner))
						.param("bookingDate", "2026-11-12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(earlier.getId()))
				.andExpect(jsonPath("$[1].id").value(later.getId()))
				.andExpect(jsonPath("$[0].customerName").value("Owner earlier"))
				.andExpect(jsonPath("$[0].customerReference").value("Apto 101"))
				.andExpect(jsonPath("$[0].status").value("SCHEDULED"))
				.andExpect(jsonPath("$[0].customerWhatsappNumber").doesNotExist())
				.andExpect(jsonPath("$[0].courtAmount").doesNotExist())
				.andExpect(jsonPath("$[0].paid").doesNotExist())
				.andExpect(jsonPath("$[0].paymentMethod").doesNotExist())
				.andExpect(jsonPath("$[0].createdBy").doesNotExist());
	}

	@Test
	void shouldReturnOwnedDetailAndHideAnotherCoachBooking() throws Exception {
		final ProviderFixture owner = createProvider("detail-owner");
		final ProviderFixture other = createProvider("detail-other");
		final CourtBooking owned = saveBooking(owner.coach(), LocalDate.of(2026, 11, 13),
				LocalTime.of(8, 0), "Owned");
		final CourtBooking foreign = saveBooking(other.coach(), LocalDate.of(2026, 11, 13),
				LocalTime.of(9, 0), "Foreign");

		mockMvc.perform(get("/api/v1/provider/courts/bookings/{id}", owned.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(owned.getId()));

		mockMvc.perform(get("/api/v1/provider/courts/bookings/{id}", foreign.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Court booking not found"));
	}

	@Test
	void shouldRejectInactiveCoachImmediately() throws Exception {
		final ProviderFixture provider = createProvider("inactive");
		provider.coach().update(provider.coach().getName(), false, "supervisor.demo");
		courtPartnerCoachRepository.save(provider.coach());

		mockMvc.perform(get("/api/v1/provider/courts/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearer(provider))
						.param("bookingDate", "2026-11-12"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message")
						.value("Provider access is inactive or no longer operational"));
	}

	@Test
	void shouldValidateProviderDateRange() throws Exception {
		final ProviderFixture provider = createProvider("range");

		mockMvc.perform(get("/api/v1/provider/courts/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearer(provider))
						.param("dateFrom", "2026-11-01")
						.param("dateTo", "2026-12-02"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Date range must not exceed 31 days"));
	}

	private ProviderFixture createProvider(String label) throws Exception {
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final CourtPartnerCoach coach = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create("Coach " + label + " " + suffix, "supervisor.demo")
		);
		final String username = "court." + label + "." + suffix;
		appUserRepository.save(AppUser.create(
				username,
				passwordEncoder.encode("123456"),
				AppUserRole.PROVIDER,
				ProviderAccessType.COURTS,
				coach,
				null,
				null,
				null,
				true
		));
		return new ProviderFixture(coach, username, authenticate(username));
	}

	private CourtBooking saveBooking(CourtPartnerCoach coach, LocalDate date, LocalTime start,
			String customerName) {
		final CourtBooking booking = CourtBooking.schedule(
				date,
				start,
				start.plusHours(1),
				customerName,
				"Apto 101",
				CourtCustomerType.PARTNER_COACH,
				CourtPricingPeriod.DAY,
				LocalTime.of(6, 0),
				LocalTime.of(18, 0),
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				false,
				null,
				null,
				null,
				null,
				null,
				null,
				List.of(),
				"operador.demo"
		);
		booking.configurePartnerCoach(coach);
		return courtBookingRepository.save(booking);
	}

	private String authenticate(String username) throws Exception {
		final String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"123456"}
								""".formatted(username)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return new com.fasterxml.jackson.databind.ObjectMapper().readTree(response)
				.path("accessToken").asText();
	}

	private String bearer(ProviderFixture provider) {
		return "Bearer " + provider.token();
	}

	private record ProviderFixture(CourtPartnerCoach coach, String username, String token) {
	}
}
