package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MassageProviderSummaryDto(
		Long providerId,
		String providerName,
		boolean providerActive,
		int therapistsCount,
		int scheduledCount,
		int cancelledCount,
		int attendedCount,
		int paidCount,
		int pendingCount,
		BigDecimal grossAmount,
		BigDecimal paidAmount,
		BigDecimal pendingAmount,
		OffsetDateTime lastBookingAt
) {
}
