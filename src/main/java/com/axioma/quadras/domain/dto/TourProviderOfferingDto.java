package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourProviderOffering;
import com.axioma.quadras.domain.model.TourServiceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TourProviderOfferingDto(
		Long id,
		Long providerId,
		TourServiceType serviceType,
		String name,
		BigDecimal amount,
		String description,
		Boolean active,
		OffsetDateTime updatedAt,
		String updatedBy
) {
	public static TourProviderOfferingDto from(TourProviderOffering offering) {
		return new TourProviderOfferingDto(
				offering.getId(),
				offering.getProvider().getId(),
				offering.getServiceType(),
				offering.getName(),
				offering.getAmount(),
				offering.getDescription(),
				offering.isActive(),
				offering.getUpdatedAt(),
				offering.getUpdatedBy()
		);
	}
}
