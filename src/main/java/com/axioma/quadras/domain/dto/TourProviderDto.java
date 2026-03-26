package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourProvider;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TourProviderDto(
		Long id,
		String name,
		String contact,
		BigDecimal defaultCommissionPercent,
		Boolean active,
		OffsetDateTime updatedAt,
		String updatedBy
) {
	public static TourProviderDto from(TourProvider provider) {
		return new TourProviderDto(
				provider.getId(),
				provider.getName(),
				provider.getContact(),
				provider.getDefaultCommissionPercent(),
				provider.isActive(),
				provider.getUpdatedAt(),
				provider.getUpdatedBy()
		);
	}
}
