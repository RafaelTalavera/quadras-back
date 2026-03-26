package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record TourProviderDto(
		Long id,
		String name,
		String contact,
		BigDecimal defaultCommissionPercent,
		Boolean active,
		OffsetDateTime updatedAt,
		String updatedBy,
		List<TourProviderOfferingDto> offerings
) {
}
