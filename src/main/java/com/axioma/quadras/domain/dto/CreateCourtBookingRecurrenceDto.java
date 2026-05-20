package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCourtBookingRecurrenceDto(
		@NotNull(message = "endDate is required")
		LocalDate endDate
) {
}
