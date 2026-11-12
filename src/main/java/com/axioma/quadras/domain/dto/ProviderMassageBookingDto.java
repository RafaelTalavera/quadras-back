package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.repository.ProviderMassageBookingView;
import java.time.LocalDate;
import java.time.LocalTime;

public record ProviderMassageBookingDto(
		Long id,
		LocalDate bookingDate,
		LocalTime startTime,
		String clientName,
		String guestReference,
		String treatment,
		MassageBookingStatus status,
		String cancellationNotes
) {
	public static ProviderMassageBookingDto from(ProviderMassageBookingView booking) {
		return new ProviderMassageBookingDto(
				booking.getId(),
				booking.getBookingDate(),
				booking.getStartTime(),
				booking.getClientName(),
				booking.getGuestReference(),
				booking.getTreatment(),
				booking.getStatus(),
				booking.getCancellationNotes()
		);
	}
}
