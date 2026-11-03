package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LaundryOpeningStockLineInputDto(
		@NotNull Long itemId,
		@NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal quantity
) {
}
