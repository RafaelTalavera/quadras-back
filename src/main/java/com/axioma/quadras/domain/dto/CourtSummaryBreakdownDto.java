package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;

public record CourtSummaryBreakdownDto(
		String code,
		String label,
		Integer scheduledCount,
		Integer paidCount,
		Integer pendingCount,
		BigDecimal totalHours,
		BigDecimal courtAmount,
		BigDecimal materialsAmount,
		BigDecimal totalAmount
) {
}
