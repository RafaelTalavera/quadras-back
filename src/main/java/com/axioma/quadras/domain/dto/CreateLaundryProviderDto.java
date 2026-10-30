package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLaundryProviderDto(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 160) String contact,
		boolean active
) { }
