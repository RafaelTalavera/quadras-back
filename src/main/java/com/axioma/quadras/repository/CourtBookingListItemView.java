package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPaymentMethod;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public interface CourtBookingListItemView {
	Long getId();

	LocalDate getBookingDate();

	LocalTime getStartTime();

	LocalTime getEndTime();

	Integer getDurationMinutes();

	String getCustomerName();

	String getCustomerReference();

	CourtCustomerType getCustomerType();

	CourtPricingPeriod getPricingPeriod();

	LocalTime getSunriseEstimate();

	LocalTime getSunsetEstimate();

	BigDecimal getCourtAmount();

	BigDecimal getMaterialsAmount();

	BigDecimal getTotalAmount();

	Boolean getPaid();

	CourtPaymentMethod getPaymentMethod();

	LocalDate getPaymentDate();

	String getPaymentNotes();

	CourtBookingStatus getStatus();

	String getCancellationNotes();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();

	OffsetDateTime getCancelledAt();

	String getCreatedBy();

	String getUpdatedBy();

	String getCancelledBy();
}
