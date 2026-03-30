package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateMaintenanceOrderDto(
		@NotNull(message = "locationId is required")
		Long locationId,
		@NotNull(message = "providerId is required")
		Long providerId,
		@NotBlank(message = "title is required")
		@Size(max = 160, message = "title must be <= 160 chars")
		String title,
		@Size(max = 1500, message = "description must be <= 1500 chars")
		String description,
		@NotNull(message = "priority is required")
		MaintenancePriority priority,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt
) {
}
