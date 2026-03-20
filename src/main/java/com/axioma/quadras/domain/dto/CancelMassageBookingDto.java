package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelMassageBookingDto(
		@NotBlank(message = "cancellationNotes is required")
		String cancellationNotes
) {
}
