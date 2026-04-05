package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMaintenanceProviderDto(
		@NotNull(message = "providerType is required")
		MaintenanceProviderType providerType,
		@NotNull(message = "specialty is required")
		MaintenanceProviderSpecialty specialty,
		@NotBlank(message = "name is required")
		@Size(max = 120, message = "name must be <= 120 chars")
		String name,
		@NotBlank(message = "serviceLabel is required")
		@Size(max = 120, message = "serviceLabel must be <= 120 chars")
		String serviceLabel,
		@Size(max = 500, message = "scopeDescription must be <= 500 chars")
		String scopeDescription,
		@Size(max = 160, message = "contact must be <= 160 chars")
		String contact,
		@NotNull(message = "active is required")
		Boolean active
) {
}
