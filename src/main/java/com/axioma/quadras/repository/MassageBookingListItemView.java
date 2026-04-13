package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassagePaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public interface MassageBookingListItemView {
	Long getId();

	LocalDate getBookingDate();

	LocalTime getStartTime();

	String getClientName();

	String getGuestReference();

	String getTreatment();

	BigDecimal getAmount();

	Long getProviderId();

	String getProviderName();

	Boolean getProviderActive();

	Long getTherapistId();

	String getTherapistName();

	Boolean getTherapistActive();

	Boolean getPaid();

	MassagePaymentMethod getPaymentMethod();

	LocalDate getPaymentDate();

	String getPaymentNotes();

	MassageBookingStatus getStatus();

	String getCancellationNotes();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();

	OffsetDateTime getCancelledAt();

	String getCreatedBy();

	String getUpdatedBy();

	String getCancelledBy();
}
