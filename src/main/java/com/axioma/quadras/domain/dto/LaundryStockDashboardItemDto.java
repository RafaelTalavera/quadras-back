package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LaundryStockDashboardItemDto(
		Long itemId,
		String itemName,
		LaundryUnitType unitType,
		BigDecimal hotelAvailable,
		BigDecimal hotelRewashQueue,
		BigDecimal providerProcessing,
		BigDecimal providerRewash,
		BigDecimal missing,
		BigDecimal damaged,
		BigDecimal total,
		OffsetDateTime oldestHotelRewashQueueAt,
		OffsetDateTime oldestProviderProcessingAt,
		OffsetDateTime oldestProviderRewashAt,
		OffsetDateTime oldestMissingAt,
		OffsetDateTime oldestDamagedAt
) {
}
