package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourBooking;
import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourPaymentMethod;
import com.axioma.quadras.domain.model.TourServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TourSummaryDetailItemDto(
		Long bookingId,
		LocalDateTime startAt,
		LocalDateTime endAt,
		TourServiceType serviceType,
		Long providerId,
		String providerName,
		Long providerOfferingId,
		String providerOfferingName,
		String clientName,
		String guestReference,
		BigDecimal amount,
		BigDecimal commissionAmount,
		Boolean paid,
		TourPaymentMethod paymentMethod,
		LocalDate paymentDate,
		TourBookingStatus status,
		String description
) {
	public static TourSummaryDetailItemDto from(TourBooking booking) {
		return new TourSummaryDetailItemDto(
				booking.getId(),
				booking.getStartAt(),
				booking.getEndAt(),
				booking.getServiceType(),
				booking.getProvider().getId(),
				booking.getProvider().getName(),
				booking.getProviderOffering() == null ? null : booking.getProviderOffering().getId(),
				booking.getProviderOfferingName(),
				booking.getClientName(),
				booking.getGuestReference(),
				booking.getAmount(),
				booking.getCommissionAmount(),
				booking.isPaid(),
				booking.getPaymentMethod(),
				booking.getPaymentDate(),
				booking.getStatus(),
				booking.getDescription()
		);
	}
}
