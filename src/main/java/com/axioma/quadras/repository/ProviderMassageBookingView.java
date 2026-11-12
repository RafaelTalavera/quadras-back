package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageBookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public interface ProviderMassageBookingView {
	Long getId();

	LocalDate getBookingDate();

	LocalTime getStartTime();

	String getClientName();

	String getGuestReference();

	String getTreatment();

	MassageBookingStatus getStatus();

	String getCancellationNotes();
}
