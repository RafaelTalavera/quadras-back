package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCourtRateDto(
		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.00", inclusive = true, message = "amount must be greater than or equal to zero")
		BigDecimal amount,
		@NotNull(message = "active is required")
		Boolean active
) {
}
