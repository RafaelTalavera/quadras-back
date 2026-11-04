package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record LaundryReconciliationDto(
		OffsetDateTime generatedAt,
		boolean consistent,
		BigDecimal balanceTotal,
		BigDecimal lotTotal,
		BigDecimal ledgerTotal,
		List<LaundryReconciliationMismatchDto> mismatches
) { }
