package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateMaintenanceOrderDto(
		@NotNull(message = "locationId is required")
		Long locationId,
		Long providerId,
		@NotBlank(message = "title is required")
		@Size(max = 160, message = "title must be <= 160 chars")
		String title,
		@Size(max = 1500, message = "description must be <= 1500 chars")
		String description,
		@NotNull(message = "priority is required")
		MaintenancePriority priority,
		@NotNull(message = "requestOrigin is required")
		MaintenanceRequestOrigin requestOrigin,
		Boolean requestedForGuest,
		@Size(max = 160, message = "guestName must be <= 160 chars")
		String guestName,
		@Size(max = 80, message = "guestReference must be <= 80 chars")
		String guestReference,
		@NotNull(message = "businessPriority is required")
		MaintenanceBusinessPriority businessPriority,
		@Positive(message = "estimatedExecutionMinutes must be positive")
		Integer estimatedExecutionMinutes,
		@Size(max = 120, message = "assignedUsername must be <= 120 chars")
		String assignedUsername,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt
) {
}
