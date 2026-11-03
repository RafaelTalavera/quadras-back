package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockOperation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateLaundryStockAdjustmentDto(
		@NotNull @Min(0) Long version,
		@NotNull Long itemId,
		@NotNull LaundryStockOperation operation,
		LaundryStockBucket fromBucket,
		LaundryStockBucket toBucket,
		Long providerId,
		@NotNull @DecimalMin("0.001") @Digits(integer = 9, fraction = 3) BigDecimal quantity,
		@NotBlank @Size(min = 5, max = 500) String reason,
		@NotNull OffsetDateTime effectiveAt
) {
}
