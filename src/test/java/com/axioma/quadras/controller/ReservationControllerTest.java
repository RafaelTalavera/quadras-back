package com.axioma.quadras.controller;

import com.axioma.quadras.domain.model.Reservation;
import com.axioma.quadras.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.flyway.enabled=true",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class ReservationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReservationRepository reservationRepository;

	@BeforeEach
	void cleanDb() {
		reservationRepository.deleteAll();
	}

	@Test
	void shouldCreateReservation() throws Exception {
		final String payload = """
				{
				  "guestName": "Ana Torres",
				  "reservationDate": "2026-03-13",
				  "startTime": "09:00:00",
				  "endTime": "10:00:00",
				  "notes": "Habitacion 304"
				}
				""";

		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.guestName").value("Ana Torres"))
				.andExpect(jsonPath("$.status").value("SCHEDULED"));
	}

	@Test
	void shouldListReservations() throws Exception {
		reservationRepository.save(Reservation.schedule(
				"Guest 1",
				LocalDate.of(2026, 3, 13),
				LocalTime.of(8, 0),
				LocalTime.of(9, 0),
				null
		));
		reservationRepository.save(Reservation.schedule(
				"Guest 2",
				LocalDate.of(2026, 3, 13),
				LocalTime.of(10, 0),
				LocalTime.of(11, 0),
				null
		));

		mockMvc.perform(get("/api/v1/reservations")
						.param("reservationDate", "2026-03-13"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].guestName").value("Guest 1"))
				.andExpect(jsonPath("$[1].guestName").value("Guest 2"));
	}

	@Test
	void shouldFindReservationById() throws Exception {
		final Reservation saved = reservationRepository.save(Reservation.schedule(
				"Mario Sosa",
				LocalDate.of(2026, 3, 14),
				LocalTime.of(15, 0),
				LocalTime.of(16, 0),
				"Cancha preferente"
		));

		mockMvc.perform(get("/api/v1/reservations/{id}", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.guestName").value("Mario Sosa"));
	}

	@Test
	void shouldReturnNotFoundWhenReservationDoesNotExist() throws Exception {
		mockMvc.perform(get("/api/v1/reservations/{id}", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Reservation 999999 not found"));
	}

	@Test
	void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
		final String payload = """
				{
				  "guestName": "   ",
				  "reservationDate": "2026-03-13",
				  "startTime": "09:00:00",
				  "endTime": "10:00:00"
				}
				""";

		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("guestName")));
	}

	@Test
	void shouldReturnConflictWhenReservationOverlaps() throws Exception {
		reservationRepository.save(Reservation.schedule(
				"Guest Existing",
				LocalDate.of(2026, 3, 13),
				LocalTime.of(9, 0),
				LocalTime.of(10, 0),
				null
		));

		final String payload = """
				{
				  "guestName": "Nuevo Huesped",
				  "reservationDate": "2026-03-13",
				  "startTime": "09:30:00",
				  "endTime": "10:30:00"
				}
				""";

		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Reservation overlaps with an existing booking."));
	}

	@Test
	void shouldReturnBadRequestWhenReservationIsOutsideOperatingHours() throws Exception {
		final String payload = """
				{
				  "guestName": "Horario invalido",
				  "reservationDate": "2026-03-13",
				  "startTime": "06:00:00",
				  "endTime": "07:00:00"
				}
				""";

		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("operating hours")));
	}

	@Test
	void shouldReturnBadRequestWhenReservationDurationIsNotAllowed() throws Exception {
		final String payload = """
				{
				  "guestName": "Duracion invalida",
				  "reservationDate": "2026-03-13",
				  "startTime": "09:00:00",
				  "endTime": "09:45:00"
				}
				""";

		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("duration")));
	}
}
