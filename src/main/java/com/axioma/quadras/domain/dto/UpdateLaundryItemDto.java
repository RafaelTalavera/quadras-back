package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryUnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateLaundryItemDto(
		@NotBlank @Size(max = 120) String name,
		@NotNull LaundryUnitType unitType,
		@NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal currentUnitPrice,
		boolean active
) { }
