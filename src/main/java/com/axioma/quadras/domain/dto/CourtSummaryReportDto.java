package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourtSummaryReportDto(
		Integer scheduledCount,
		Integer cancelledCount,
		Integer paidCount,
		Integer pendingCount,
		BigDecimal totalHours,
		BigDecimal guestHours,
		BigDecimal vipHours,
		BigDecimal externalHours,
		BigDecimal partnerCoachHours,
		BigDecimal paidAmount,
		BigDecimal pendingAmount,
		BigDecimal courtAmount,
		BigDecimal materialsAmount,
		BigDecimal expectedAmount,
		BigDecimal averageTicket,
		List<CourtSummaryBreakdownDto> customerTypeBreakdown,
		List<CourtSummaryBreakdownDto> pricingPeriodBreakdown,
		List<CourtSummaryBreakdownDto> paymentMethodBreakdown
) {
}
