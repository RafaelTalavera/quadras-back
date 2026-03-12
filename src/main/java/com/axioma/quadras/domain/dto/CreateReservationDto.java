package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationDto(
		@NotBlank(message = "guestName is required")
		@Size(max = 120, message = "guestName must be <= 120 chars")
		String guestName,
		@NotNull(message = "reservationDate is required")
		LocalDate reservationDate,
		@NotNull(message = "startTime is required")
		LocalTime startTime,
		@NotNull(message = "endTime is required")
		LocalTime endTime,
		@Size(max = 500, message = "notes must be <= 500 chars")
		String notes
) {
}
