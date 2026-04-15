package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourServiceType;
import com.axioma.quadras.repository.TourBookingCompactItemView;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TourBookingCompactDto(
		Long id,
		TourServiceType serviceType,
		LocalDateTime startAt,
		LocalDateTime endAt,
		String clientName,
		String guestReference,
		Long providerId,
		String providerName,
		Long providerOfferingId,
		String providerOfferingName,
		BigDecimal amount,
		boolean paid,
		TourBookingStatus status
) {
	public static TourBookingCompactDto from(TourBookingCompactItemView item) {
		return new TourBookingCompactDto(
				item.getId(),
				item.getServiceType(),
				item.getStartAt(),
				item.getEndAt(),
				item.getClientName(),
				item.getGuestReference(),
				item.getProviderId(),
				item.getProviderName(),
				item.getProviderOfferingId(),
				item.getProviderOfferingName(),
				item.getAmount(),
				Boolean.TRUE.equals(item.getPaid()),
				item.getStatus()
		);
	}
}
