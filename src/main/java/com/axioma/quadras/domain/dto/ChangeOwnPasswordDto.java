package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeOwnPasswordDto(
		@NotBlank(message = "currentPassword is required")
		String currentPassword,
		@NotBlank(message = "newPassword is required")
		String newPassword
) {
}
