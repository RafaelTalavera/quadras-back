package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelTourBookingDto(
		@NotBlank(message = "cancellationNotes is required")
		@Size(max = 500, message = "cancellationNotes must be <= 500 chars")
		String cancellationNotes
) {
}
