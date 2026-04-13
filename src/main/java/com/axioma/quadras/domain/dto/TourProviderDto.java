package com.axioma.quadras.domain.dto;

import com.axioma.quadras.repository.TourProviderListItemView;
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
	public static TourProviderDto from(
			TourProviderListItemView provider,
			List<TourProviderOfferingDto> offerings
	) {
		return new TourProviderDto(
				provider.getId(),
				provider.getName(),
				provider.getContact(),
				provider.getDefaultCommissionPercent(),
				Boolean.TRUE.equals(provider.getActive()),
				provider.getUpdatedAt(),
				provider.getUpdatedBy(),
				offerings == null ? List.of() : List.copyOf(offerings)
		);
	}
}
