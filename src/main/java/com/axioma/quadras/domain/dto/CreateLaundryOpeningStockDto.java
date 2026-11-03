package com.axioma.quadras.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateLaundryOpeningStockDto(
		@NotNull @Min(0) Long version,
		@NotNull OffsetDateTime effectiveAt,
		@Size(max = 500) String notes,
		@NotEmpty @Valid List<LaundryOpeningStockLineInputDto> lines
) {
}
