package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaundryDailySummaryRowDto(
		LocalDate date,
		BigDecimal receivedQuantity,
		BigDecimal sentQuantity,
		BigDecimal rewashedQuantity
) { }
