package com.axioma.quadras.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record LaundryStockDashboardDto(
		OffsetDateTime generatedAt,
		Long providerId,
		LaundryStockTotalsDto totals,
		List<LaundryStockDashboardItemDto> items,
		List<LaundryStockProviderTotalsDto> providers
) {
}
