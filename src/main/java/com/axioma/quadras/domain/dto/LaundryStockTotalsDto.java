package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;

public record LaundryStockTotalsDto(
		BigDecimal hotelAvailable,
		BigDecimal hotelRewashQueue,
		BigDecimal providerProcessing,
		BigDecimal providerRewash,
		BigDecimal missing,
		BigDecimal damaged,
		BigDecimal total
) {
	public static LaundryStockTotalsDto zero() {
		return new LaundryStockTotalsDto(
				BigDecimal.ZERO.setScale(3),
				BigDecimal.ZERO.setScale(3),
				BigDecimal.ZERO.setScale(3),
				BigDecimal.ZERO.setScale(3),
				BigDecimal.ZERO.setScale(3),
				BigDecimal.ZERO.setScale(3),
				BigDecimal.ZERO.setScale(3)
		);
	}
}
