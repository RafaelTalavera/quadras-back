package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LaundryOperationalMetricsDto(
		OffsetDateTime generatedAt,
		Long providerId,
		LaundryStockTotalsDto stock,
		OffsetDateTime oldestPendingAt,
		long observedDocuments,
		long returnsAwaitingHotel,
		BigDecimal cleanAcceptedQuantity,
		BigDecimal rewashQuantity,
		BigDecimal classifiedQuantity,
		BigDecimal rewashRatePercent
) { }
