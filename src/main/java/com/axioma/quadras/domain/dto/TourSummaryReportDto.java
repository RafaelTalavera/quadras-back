package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record TourSummaryReportDto(
		Integer scheduledCount,
		Integer cancelledCount,
		Integer paidCount,
		Integer pendingCount,
		BigDecimal totalHours,
		BigDecimal grossAmount,
		BigDecimal paidAmount,
		BigDecimal pendingAmount,
		BigDecimal commissionAmount,
		BigDecimal netAmount,
		BigDecimal averageTicket,
		List<TourSummaryBreakdownDto> providerBreakdown,
		List<TourSummaryBreakdownDto> serviceTypeBreakdown,
		List<TourSummaryBreakdownDto> paymentMethodBreakdown
) {
}
