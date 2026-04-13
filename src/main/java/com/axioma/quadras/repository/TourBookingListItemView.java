package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourPaymentMethod;
import com.axioma.quadras.domain.model.TourServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface TourBookingListItemView {
	Long getId();

	TourServiceType getServiceType();

	LocalDateTime getStartAt();

	LocalDateTime getEndAt();

	String getClientName();

	String getGuestReference();

	Long getProviderId();

	String getProviderName();

	Boolean getProviderActive();

	Long getProviderOfferingId();

	String getProviderOfferingName();

	BigDecimal getAmount();

	BigDecimal getCommissionPercent();

	BigDecimal getCommissionAmount();

	String getDescription();

	Boolean getPaid();

	TourPaymentMethod getPaymentMethod();

	LocalDate getPaymentDate();

	String getPaymentNotes();

	TourBookingStatus getStatus();

	String getCancellationNotes();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();

	OffsetDateTime getCancelledAt();

	String getCreatedBy();

	String getUpdatedBy();

	String getCancelledBy();
}
