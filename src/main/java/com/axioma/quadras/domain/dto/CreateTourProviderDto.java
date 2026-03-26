package com.axioma.quadras.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateTourProviderDto(
		@NotBlank(message = "name is required")
		@Size(max = 120, message = "name must be <= 120 chars")
		String name,
		@NotBlank(message = "contact is required")
		@Size(max = 160, message = "contact must be <= 160 chars")
		String contact,
		@NotNull(message = "defaultCommissionPercent is required")
		@DecimalMin(value = "0.00", message = "defaultCommissionPercent must be >= 0")
		@DecimalMax(value = "100.00", message = "defaultCommissionPercent must be <= 100")
		BigDecimal defaultCommissionPercent,
		List<@Valid TourProviderOfferingInputDto> offerings
) {
}
