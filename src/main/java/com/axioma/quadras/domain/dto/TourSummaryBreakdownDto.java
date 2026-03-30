package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;

public record TourSummaryBreakdownDto(
		String code,
		String label,
		Boolean active,
		Integer scheduledCount,
		Integer paidCount,
		Integer pendingCount,
		BigDecimal totalHours,
		BigDecimal grossAmount,
		BigDecimal paidAmount,
		BigDecimal pendingAmount,
		BigDecimal commissionAmount
) {
}
