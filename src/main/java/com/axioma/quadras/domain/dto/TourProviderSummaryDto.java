package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TourProviderSummaryDto(
		Long providerId,
		String providerName,
		Boolean providerActive,
		Integer scheduledCount,
		Integer cancelledCount,
		Integer paidCount,
		Integer pendingCount,
		BigDecimal grossAmount,
		BigDecimal paidAmount,
		BigDecimal pendingAmount,
		BigDecimal commissionAmount,
		LocalDateTime lastBookingAt
) {
}
