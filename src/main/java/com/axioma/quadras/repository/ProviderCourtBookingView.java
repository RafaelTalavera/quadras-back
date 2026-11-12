package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtBookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public interface ProviderCourtBookingView {
	Long getId();

	LocalDate getBookingDate();

	LocalTime getStartTime();

	LocalTime getEndTime();

	Integer getDurationMinutes();

	String getCustomerName();

	String getCustomerReference();

	CourtBookingStatus getStatus();

	String getCancellationNotes();

	String getRecurrenceGroupId();

	LocalDate getRecurrenceStartDate();

	LocalDate getRecurrenceEndDate();
}
