package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenancePlanRecurrenceUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateMaintenancePlanDto(
		@NotNull(message = "locationId is required")
		Long locationId,
		Long providerId,
		@NotBlank(message = "title is required")
		@Size(max = 160, message = "title must be <= 160 chars")
		String title,
		@Size(max = 1500, message = "description must be <= 1500 chars")
		String description,
		@NotNull(message = "recurrenceUnit is required")
		MaintenancePlanRecurrenceUnit recurrenceUnit,
		@NotNull(message = "recurrenceInterval is required")
		Integer recurrenceInterval,
		@NotNull(message = "nextDueDate is required")
		LocalDate nextDueDate,
		@NotNull(message = "active is required")
		Boolean active
) {
}
