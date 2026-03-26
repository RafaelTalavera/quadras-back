package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCourtPartnerCoachDto(
		@NotBlank(message = "name is required")
		@Size(max = 120, message = "name must be <= 120 chars")
		String name,
		boolean active
) {
}
