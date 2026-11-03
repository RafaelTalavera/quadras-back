package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConfirmLaundryReturnLineDto(
		@NotNull Long itemId,
		@NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal acceptedClean,
		@NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal returnedDirty,
		@NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal retained,
		@NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal missing,
		@NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal damaged
) { }
