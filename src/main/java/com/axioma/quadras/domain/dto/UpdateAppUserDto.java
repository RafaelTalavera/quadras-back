package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.AppUserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateAppUserDto(
		@NotNull(message = "role is required")
		AppUserRole role,
		@NotNull(message = "enabled is required")
		Boolean enabled
) {
}
