package com.axioma.quadras.controller;

import com.axioma.quadras.domain.model.Reservation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.axioma.quadras.repository.ReservationRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		reservationRepository.deleteAll();
		accessToken = authenticate();
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
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
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
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
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

		mockMvc.perform(get("/api/v1/reservations/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.guestName").value("Mario Sosa"));
	}

	@Test
	void shouldReturnNotFoundWhenReservationDoesNotExist() throws Exception {
		mockMvc.perform(get("/api/v1/reservations/{id}", 999999L)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
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
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
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
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
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
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
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
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("duration")));
	}

	@Test
	void shouldUpdateReservation() throws Exception {
		final Reservation saved = reservationRepository.save(Reservation.schedule(
				"Guest Base",
				LocalDate.of(2026, 3, 21),
				LocalTime.of(9, 0),
				LocalTime.of(10, 0),
				"Nota inicial"
		));

		final String payload = """
				{
				  "guestName": "Guest Actualizado",
				  "reservationDate": "2026-03-21",
				  "startTime": "10:00:00",
				  "endTime": "11:00:00",
				  "notes": "Nota actualizada"
				}
				""";

		mockMvc.perform(put("/api/v1/reservations/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.guestName").value("Guest Actualizado"))
				.andExpect(jsonPath("$.startTime").value("10:00:00"))
				.andExpect(jsonPath("$.endTime").value("11:00:00"))
				.andExpect(jsonPath("$.status").value("SCHEDULED"));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingReservationDoesNotExist() throws Exception {
		final String payload = """
				{
				  "guestName": "Guest Inexistente",
				  "reservationDate": "2026-03-21",
				  "startTime": "10:00:00",
				  "endTime": "11:00:00"
				}
				""";

		mockMvc.perform(put("/api/v1/reservations/{id}", 999999L)
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Reservation 999999 not found"));
	}

	@Test
	void shouldReturnConflictWhenUpdatingReservationOverlaps() throws Exception {
		final Reservation base = reservationRepository.save(Reservation.schedule(
				"Guest Base",
				LocalDate.of(2026, 3, 21),
				LocalTime.of(10, 0),
				LocalTime.of(11, 0),
				null
		));
		reservationRepository.save(Reservation.schedule(
				"Guest Existing",
				LocalDate.of(2026, 3, 21),
				LocalTime.of(11, 0),
				LocalTime.of(12, 0),
				null
		));

		final String payload = """
				{
				  "guestName": "Guest Base",
				  "reservationDate": "2026-03-21",
				  "startTime": "11:30:00",
				  "endTime": "12:30:00"
				}
				""";

		mockMvc.perform(put("/api/v1/reservations/{id}", base.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Reservation overlaps with an existing booking."));
	}

	@Test
	void shouldReturnConflictWhenUpdatingCancelledReservation() throws Exception {
		final Reservation reservation = reservationRepository.save(Reservation.schedule(
				"Guest Cancelled",
				LocalDate.of(2026, 3, 22),
				LocalTime.of(12, 0),
				LocalTime.of(13, 0),
				null
		));
		reservation.markCancelled();
		reservationRepository.save(reservation);

		final String payload = """
				{
				  "guestName": "Guest Edit",
				  "reservationDate": "2026-03-22",
				  "startTime": "13:00:00",
				  "endTime": "14:00:00"
				}
				""";

		mockMvc.perform(put("/api/v1/reservations/{id}", reservation.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Cancelled reservations cannot be edited."));
	}

	@Test
	void shouldCancelReservation() throws Exception {
		final Reservation reservation = reservationRepository.save(Reservation.schedule(
				"Guest Cancel",
				LocalDate.of(2026, 3, 22),
				LocalTime.of(14, 0),
				LocalTime.of(15, 0),
				null
		));

		mockMvc.perform(patch("/api/v1/reservations/{id}/cancel", reservation.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(reservation.getId()))
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void shouldReturnNotFoundWhenCancellingReservationDoesNotExist() throws Exception {
		mockMvc.perform(patch("/api/v1/reservations/{id}/cancel", 999999L)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Reservation 999999 not found"));
	}

	@Test
	void shouldReturnConflictWhenCancellingCompletedReservation() throws Exception {
		final Reservation reservation = reservationRepository.save(Reservation.schedule(
				"Guest Completed",
				LocalDate.of(2026, 3, 22),
				LocalTime.of(16, 0),
				LocalTime.of(17, 0),
				null
		));
		reservation.markCompleted();
		reservationRepository.save(reservation);

		mockMvc.perform(patch("/api/v1/reservations/{id}/cancel", reservation.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Completed reservations cannot be cancelled."));
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
