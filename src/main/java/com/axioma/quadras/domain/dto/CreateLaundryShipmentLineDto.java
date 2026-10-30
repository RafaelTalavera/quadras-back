package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryRewashReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateLaundryShipmentLineDto(
		@NotNull Long itemId,
		@NotNull @DecimalMin(value = "0.001") @Digits(integer = 9, fraction = 3) BigDecimal quantity,
		LaundryRewashReason rewashReason
) { }
