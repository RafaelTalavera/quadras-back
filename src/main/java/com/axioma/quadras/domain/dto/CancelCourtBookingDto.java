package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtBookingCancellationScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelCourtBookingDto(
		@NotBlank(message = "cancellationNotes is required")
		@Size(max = 500, message = "cancellationNotes must be <= 500 chars")
		String cancellationNotes,
		CourtBookingCancellationScope cancellationScope
) {
}
