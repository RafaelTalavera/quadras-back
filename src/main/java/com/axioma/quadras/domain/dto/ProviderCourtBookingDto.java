package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.repository.ProviderCourtBookingView;
import java.time.LocalDate;
import java.time.LocalTime;

public record ProviderCourtBookingDto(
		Long id,
		LocalDate bookingDate,
		LocalTime startTime,
		LocalTime endTime,
		Integer durationMinutes,
		String customerName,
		String customerReference,
		CourtBookingStatus status,
		String cancellationNotes,
		String recurrenceGroupId,
		LocalDate recurrenceStartDate,
		LocalDate recurrenceEndDate
) {
	public static ProviderCourtBookingDto from(ProviderCourtBookingView booking) {
		return new ProviderCourtBookingDto(
				booking.getId(),
				booking.getBookingDate(),
				booking.getStartTime(),
				booking.getEndTime(),
				booking.getDurationMinutes(),
				booking.getCustomerName(),
				booking.getCustomerReference(),
				booking.getStatus(),
				booking.getCancellationNotes(),
				booking.getRecurrenceGroupId(),
				booking.getRecurrenceStartDate(),
				booking.getRecurrenceEndDate()
		);
	}
}
