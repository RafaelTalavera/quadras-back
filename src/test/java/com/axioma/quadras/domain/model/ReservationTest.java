package com.axioma.quadras.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationTest {

	@Test
	void shouldCreateScheduledReservationWithValidValues() {
		final Reservation reservation = Reservation.schedule(
				"   Juan Perez   ",
				LocalDate.of(2026, 3, 13),
				LocalTime.of(9, 0),
				LocalTime.of(10, 0),
				" Cancha techada "
		);

		assertThat(reservation.getGuestName()).isEqualTo("Juan Perez");
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.SCHEDULED);
		assertThat(reservation.getNotes()).isEqualTo("Cancha techada");
		assertThat(reservation.durationInMinutes()).isEqualTo(60);
	}

	@Test
	void shouldRejectInvalidTimeWindow() {
		assertThatThrownBy(() -> Reservation.schedule(
				"Hotel Guest",
				LocalDate.of(2026, 3, 13),
				LocalTime.of(10, 0),
				LocalTime.of(10, 0),
				null
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("startTime must be before endTime");
	}

	@Test
	void shouldRejectBlankGuestName() {
		assertThatThrownBy(() -> Reservation.schedule(
				"  ",
				LocalDate.of(2026, 3, 13),
				LocalTime.of(10, 0),
				LocalTime.of(11, 0),
				null
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("guestName is required");
	}
}
