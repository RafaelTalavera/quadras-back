package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppUserDto(
		@NotBlank(message = "username is required")
		@Size(max = 80, message = "username must be <= 80 chars")
		String username,
		@NotBlank(message = "password is required")
		String password,
		@NotNull(message = "role is required")
		AppUserRole role,
		Boolean enabled
) {
}
