package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record LaundrySummaryDto(
		List<LaundryDailySummaryRowDto> rows,
		BigDecimal totalReceivedQuantity,
		BigDecimal totalSentQuantity,
		BigDecimal totalRewashedQuantity
) { }
