package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.MassageBookingRepository;
import com.axioma.quadras.repository.MassageProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
class ProviderMassageBookingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MassageBookingRepository massageBookingRepository;

	@Autowired
	private MassageProviderRepository massageProviderRepository;

	@Autowired
	private MassageTherapistRepository massageTherapistRepository;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void cleanBookings() {
		massageBookingRepository.deleteAll();
	}

	@Test
	void shouldListOnlyOwnedBookingsInStableOrderWithReducedContract() throws Exception {
		final ProviderFixture owner = createProvider("owner");
		final ProviderFixture other = createProvider("other");
		final MassageBooking later = saveBooking(owner, LocalDate.of(2026, 11, 12),
				LocalTime.of(10, 0), "Owner later");
		final MassageBooking earlier = saveBooking(owner, LocalDate.of(2026, 11, 12),
				LocalTime.of(8, 0), "Owner earlier");
		saveBooking(other, LocalDate.of(2026, 11, 12), LocalTime.of(9, 0), "Other");

		mockMvc.perform(get("/api/v1/provider/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearer(owner))
						.param("bookingDate", "2026-11-12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(earlier.getId()))
				.andExpect(jsonPath("$[1].id").value(later.getId()))
				.andExpect(jsonPath("$[0].clientName").value("Owner earlier"))
				.andExpect(jsonPath("$[0].guestReference").value("Apto 101"))
				.andExpect(jsonPath("$[0].treatment").value("Relaxante"))
				.andExpect(jsonPath("$[0].status").value("SCHEDULED"))
				.andExpect(jsonPath("$[0].amount").doesNotExist())
				.andExpect(jsonPath("$[0].paid").doesNotExist())
				.andExpect(jsonPath("$[0].paymentMethod").doesNotExist())
				.andExpect(jsonPath("$[0].providerName").doesNotExist())
				.andExpect(jsonPath("$[0].createdBy").doesNotExist());
	}

	@Test
	void shouldReturnOwnedDetailAndHideAnotherTherapistBooking() throws Exception {
		final ProviderFixture owner = createProvider("detail-owner");
		final ProviderFixture other = createProvider("detail-other");
		final MassageBooking owned = saveBooking(owner, LocalDate.of(2026, 11, 13),
				LocalTime.of(8, 0), "Owned");
		final MassageBooking foreign = saveBooking(other, LocalDate.of(2026, 11, 13),
				LocalTime.of(9, 0), "Foreign");

		mockMvc.perform(get("/api/v1/provider/massages/bookings/{id}", owned.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(owned.getId()));

		mockMvc.perform(get("/api/v1/provider/massages/bookings/{id}", foreign.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(owner)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Massage booking not found"));
	}

	@Test
	void shouldRejectInactiveMassageProviderImmediately() throws Exception {
		final ProviderFixture fixture = createProvider("inactive");
		fixture.provider().update(fixture.provider().getName(), "Relaxante", "Agenda interna", false);
		massageProviderRepository.save(fixture.provider());

		mockMvc.perform(get("/api/v1/provider/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearer(fixture))
						.param("bookingDate", "2026-11-12"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message")
						.value("Provider access is inactive or no longer operational"));
	}

	@Test
	void shouldValidateProviderDateRange() throws Exception {
		final ProviderFixture fixture = createProvider("range");

		mockMvc.perform(get("/api/v1/provider/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearer(fixture))
						.param("dateFrom", "2026-11-01")
						.param("dateTo", "2026-12-02"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Date range must not exceed 31 days"));
	}

	private ProviderFixture createProvider(String label) throws Exception {
		final String suffix = UUID.randomUUID().toString().substring(0, 8);
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Spa " + label + " " + suffix, "Relaxante", "Agenda interna")
		);
		final MassageTherapist therapist = massageTherapistRepository.save(
				MassageTherapist.create(provider, "Therapist " + label + " " + suffix)
		);
		final String username = "massage." + label + "." + suffix;
		appUserRepository.save(AppUser.create(
				username,
				passwordEncoder.encode("123456"),
				AppUserRole.PROVIDER,
				ProviderAccessType.MASSAGE,
				null,
				therapist,
				null,
				null,
				true
		));
		return new ProviderFixture(provider, therapist, authenticate(username));
	}

	private MassageBooking saveBooking(ProviderFixture fixture, LocalDate date,
			LocalTime startTime, String clientName) {
		return massageBookingRepository.save(MassageBooking.schedule(
				date,
				startTime,
				clientName,
				"Apto 101",
				"Relaxante",
				new BigDecimal("200.00"),
				fixture.provider(),
				fixture.therapist(),
				false,
				null,
				null,
				null,
				"operador.demo"
		));
	}

	private String authenticate(String username) throws Exception {
		final String response = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"123456"}
								""".formatted(username)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).path("accessToken").asText();
	}

	private String bearer(ProviderFixture fixture) {
		return "Bearer " + fixture.token();
	}

	private record ProviderFixture(MassageProvider provider, MassageTherapist therapist,
			String token) {
	}
}
