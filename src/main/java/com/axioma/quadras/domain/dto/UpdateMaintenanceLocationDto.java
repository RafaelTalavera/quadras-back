package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceLocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMaintenanceLocationDto(
		@NotNull(message = "locationType is required")
		MaintenanceLocationType locationType,
		@NotBlank(message = "code is required")
		@Size(max = 60, message = "code must be <= 60 chars")
		String code,
		@NotBlank(message = "label is required")
		@Size(max = 160, message = "label must be <= 160 chars")
		String label,
		@Size(max = 40, message = "floor must be <= 40 chars")
		String floor,
		@Size(max = 500, message = "description must be <= 500 chars")
		String description,
		@NotNull(message = "active is required")
		Boolean active
) {
}
