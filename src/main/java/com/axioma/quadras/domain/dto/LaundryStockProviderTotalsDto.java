package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;

public record LaundryStockProviderTotalsDto(
		Long providerId,
		String providerName,
		BigDecimal providerProcessing,
		BigDecimal providerRewash,
		BigDecimal missing,
		BigDecimal damaged,
		BigDecimal total
) {
}
