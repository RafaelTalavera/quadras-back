package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryRewashReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReceiveLaundryLineDto(
		@NotNull Long lineId,
		@NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal acceptedQuantity,
		@NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal rewashQuantity,
		@NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal missingQuantity,
		@NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal damagedQuantity,
		LaundryRewashReason rewashReason
) { }
