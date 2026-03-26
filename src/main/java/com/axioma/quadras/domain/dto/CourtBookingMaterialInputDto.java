package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtMaterialCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CourtBookingMaterialInputDto(
		@NotNull(message = "materialCode is required")
		CourtMaterialCode materialCode,
		@NotNull(message = "quantity is required")
		@Min(value = 0, message = "quantity must be greater than or equal to zero")
		Integer quantity
) {
}
