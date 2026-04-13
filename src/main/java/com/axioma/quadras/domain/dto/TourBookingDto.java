package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourBooking;
import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourPaymentMethod;
import com.axioma.quadras.domain.model.TourServiceType;
import com.axioma.quadras.repository.TourBookingListItemView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record TourBookingDto(
		Long id,
		TourServiceType serviceType,
		LocalDateTime startAt,
		LocalDateTime endAt,
		String clientName,
		String guestReference,
		Long providerId,
		String providerName,
		Boolean providerActive,
		Long providerOfferingId,
		String providerOfferingName,
		BigDecimal amount,
		BigDecimal commissionPercent,
		BigDecimal commissionAmount,
		String description,
		Boolean paid,
		TourPaymentMethod paymentMethod,
		LocalDate paymentDate,
		String paymentNotes,
		TourBookingStatus status,
		String cancellationNotes,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime cancelledAt,
		String createdBy,
		String updatedBy,
		String cancelledBy
) {
	public static TourBookingDto from(TourBooking booking) {
		return new TourBookingDto(
				booking.getId(),
				booking.getServiceType(),
				booking.getStartAt(),
				booking.getEndAt(),
				booking.getClientName(),
				booking.getGuestReference(),
				booking.getProvider().getId(),
				booking.getProvider().getName(),
				booking.getProvider().isActive(),
				booking.getProviderOffering() == null ? null : booking.getProviderOffering().getId(),
				booking.getProviderOfferingName(),
				booking.getAmount(),
				booking.getCommissionPercent(),
				booking.getCommissionAmount(),
				booking.getDescription(),
				booking.isPaid(),
				booking.getPaymentMethod(),
				booking.getPaymentDate(),
				booking.getPaymentNotes(),
				booking.getStatus(),
				booking.getCancellationNotes(),
				booking.getCreatedAt(),
				booking.getUpdatedAt(),
				booking.getCancelledAt(),
				booking.getCreatedBy(),
				booking.getUpdatedBy(),
				booking.getCancelledBy()
		);
	}

	public static TourBookingDto from(TourBookingListItemView booking) {
		return new TourBookingDto(
				booking.getId(),
				booking.getServiceType(),
				booking.getStartAt(),
				booking.getEndAt(),
				booking.getClientName(),
				booking.getGuestReference(),
				booking.getProviderId(),
				booking.getProviderName(),
				Boolean.TRUE.equals(booking.getProviderActive()),
				booking.getProviderOfferingId(),
				booking.getProviderOfferingName(),
				booking.getAmount(),
				booking.getCommissionPercent(),
				booking.getCommissionAmount(),
				booking.getDescription(),
				Boolean.TRUE.equals(booking.getPaid()),
				booking.getPaymentMethod(),
				booking.getPaymentDate(),
				booking.getPaymentNotes(),
				booking.getStatus(),
				booking.getCancellationNotes(),
				booking.getCreatedAt(),
				booking.getUpdatedAt(),
				booking.getCancelledAt(),
				booking.getCreatedBy(),
				booking.getUpdatedBy(),
				booking.getCancelledBy()
		);
	}
}
