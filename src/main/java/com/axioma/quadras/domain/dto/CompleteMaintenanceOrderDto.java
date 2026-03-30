package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CompleteMaintenanceOrderDto(
		OffsetDateTime completedAt,
		@NotBlank(message = "resolutionNotes is required")
		@Size(max = 1500, message = "resolutionNotes must be <= 1500 chars")
		String resolutionNotes
) {
}
