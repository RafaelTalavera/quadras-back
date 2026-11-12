package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.ProviderAccessType;
import jakarta.validation.constraints.NotNull;

public record UpdateAppUserDto(
		@NotNull(message = "role is required")
		AppUserRole role,
		ProviderAccessType providerType,
		Long providerResourceId,
		Long laundryProviderId,
		@NotNull(message = "enabled is required")
		Boolean enabled
) {
}
