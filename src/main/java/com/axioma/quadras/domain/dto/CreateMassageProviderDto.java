package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMassageProviderDto(
		@NotBlank(message = "name is required")
		@Size(max = 120, message = "name must be <= 120 chars")
		String name,
		@NotBlank(message = "specialty is required")
		@Size(max = 120, message = "specialty must be <= 120 chars")
		String specialty,
		@NotBlank(message = "contact is required")
		@Size(max = 120, message = "contact must be <= 120 chars")
		String contact
) {
}
