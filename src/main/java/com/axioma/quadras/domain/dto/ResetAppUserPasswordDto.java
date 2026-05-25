package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetAppUserPasswordDto(
		@NotBlank(message = "newPassword is required")
		String newPassword
) {
}
