package com.axioma.quadras.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TourSummaryDetailItemView {

	Long getBookingId();

	LocalDateTime getStartAt();

	LocalDateTime getEndAt();

	String getServiceType();

	Long getProviderId();

	String getProviderName();

	Long getProviderOfferingId();

	String getProviderOfferingName();

	String getClientName();

	String getGuestReference();

	BigDecimal getAmount();

	BigDecimal getCommissionAmount();

	Boolean getPaid();

	String getPaymentMethod();

	LocalDate getPaymentDate();

	String getStatus();

	String getDescription();
}
