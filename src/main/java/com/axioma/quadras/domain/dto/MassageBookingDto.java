package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassagePaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record MassageBookingDto(
		Long id,
		LocalDate bookingDate,
		LocalTime startTime,
		String clientName,
		String guestReference,
		String treatment,
		BigDecimal amount,
		Long providerId,
		String providerName,
		boolean providerActive,
		boolean paid,
		MassagePaymentMethod paymentMethod,
		LocalDate paymentDate,
		String paymentNotes,
		MassageBookingStatus status,
		String cancellationNotes,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime cancelledAt,
		String createdBy,
		String updatedBy,
		String cancelledBy
) {
	public static MassageBookingDto from(MassageBooking booking) {
		return new MassageBookingDto(
				booking.getId(),
				booking.getBookingDate(),
				booking.getStartTime(),
				booking.getClientName(),
				booking.getGuestReference(),
				booking.getTreatment(),
				booking.getAmount(),
				booking.getProvider().getId(),
				booking.getProvider().getName(),
				booking.getProvider().isActive(),
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
}
