package com.axioma.quadras.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TourProviderSummaryView {

	Long getProviderId();

	String getProviderName();

	boolean getProviderActive();

	long getScheduledCount();

	long getCancelledCount();

	long getPaidCount();

	long getPendingCount();

	BigDecimal getGrossAmount();

	BigDecimal getPaidAmount();

	BigDecimal getPendingAmount();

	BigDecimal getCommissionAmount();

	LocalDateTime getLastBookingAt();
}
