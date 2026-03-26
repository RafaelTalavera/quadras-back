package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateCourtMaterialSettingDto(
		@NotBlank(message = "label is required")
		@Size(max = 120, message = "label must be <= 120 chars")
		String label,
		@NotNull(message = "unitPrice is required")
		@DecimalMin(value = "0.00", inclusive = true, message = "unitPrice must be greater than or equal to zero")
		BigDecimal unitPrice,
		@NotNull(message = "chargeGuest is required")
		Boolean chargeGuest,
		@NotNull(message = "chargeVip is required")
		Boolean chargeVip,
		@NotNull(message = "chargeExternal is required")
		Boolean chargeExternal,
		@NotNull(message = "chargePartnerCoach is required")
		Boolean chargePartnerCoach,
		@NotNull(message = "active is required")
		Boolean active
) {
}
