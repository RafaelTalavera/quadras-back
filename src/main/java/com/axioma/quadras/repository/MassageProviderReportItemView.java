package com.axioma.quadras.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface MassageProviderReportItemView {

	Long getBookingId();

	LocalDate getBookingDate();

	LocalTime getStartTime();

	String getClientName();

	String getGuestReference();

	String getTreatment();

	Long getTherapistId();

	String getTherapistName();

	BigDecimal getAmount();

	Boolean getPaid();

	String getPaymentMethod();

	LocalDate getPaymentDate();

	String getPaymentNotes();

	String getStatus();

	String getCancellationNotes();
}
