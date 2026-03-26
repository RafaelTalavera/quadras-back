package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TourProviderOfferingInputDto(
		@NotNull(message = "serviceType is required")
		TourServiceType serviceType,
		@NotBlank(message = "name is required")
		@Size(max = 120, message = "name must be <= 120 chars")
		String name,
		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.00", message = "amount must be >= 0")
		BigDecimal amount,
		@Size(max = 1000, message = "description must be <= 1000 chars")
		String description,
		@NotNull(message = "active is required")
		Boolean active
) {
}
