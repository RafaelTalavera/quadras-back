package com.axioma.quadras.domain.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.axioma.quadras.domain.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationDtoTest {

	@Test
	void shouldMapReservationDomainToDto() {
		final Reservation reservation = Reservation.schedule(
				"Maria Gomez",
				LocalDate.of(2026, 3, 14),
				LocalTime.of(8, 30),
				LocalTime.of(9, 30),
				"Cliente del hotel"
		);

		final ReservationDto dto = ReservationDto.from(reservation);

		assertThat(dto.id()).isNull();
		assertThat(dto.guestName()).isEqualTo("Maria Gomez");
		assertThat(dto.reservationDate()).isEqualTo(LocalDate.of(2026, 3, 14));
		assertThat(dto.startTime()).isEqualTo(LocalTime.of(8, 30));
		assertThat(dto.endTime()).isEqualTo(LocalTime.of(9, 30));
		assertThat(dto.status().name()).isEqualTo("SCHEDULED");
		assertThat(dto.notes()).isEqualTo("Cliente del hotel");
	}
}
