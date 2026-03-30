package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassagePaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record MassageProviderReportItemDto(
		Long bookingId,
		LocalDate bookingDate,
		LocalTime startTime,
		String clientName,
		String guestReference,
		String treatment,
		Long therapistId,
		String therapistName,
		BigDecimal amount,
		boolean paid,
		MassagePaymentMethod paymentMethod,
		LocalDate paymentDate,
		String paymentNotes,
		MassageBookingStatus status,
		String cancellationNotes
) {
	public static MassageProviderReportItemDto from(MassageBooking booking) {
		return new MassageProviderReportItemDto(
				booking.getId(),
				booking.getBookingDate(),
				booking.getStartTime(),
				booking.getClientName(),
				booking.getGuestReference(),
				booking.getTreatment(),
				booking.getTherapist().getId(),
				booking.getTherapist().getName(),
				booking.getAmount(),
				booking.isPaid(),
				booking.getPaymentMethod(),
				booking.getPaymentDate(),
				booking.getPaymentNotes(),
				booking.getStatus(),
				booking.getCancellationNotes()
		);
	}
}
