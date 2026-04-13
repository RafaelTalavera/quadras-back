package com.axioma.quadras.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface MassageProviderSummaryView {

	Long getProviderId();

	String getProviderName();

	boolean getProviderActive();

	long getTherapistsCount();

	long getScheduledCount();

	long getCancelledCount();

	long getPaidCount();

	long getPendingCount();

	BigDecimal getGrossAmount();

	BigDecimal getPaidAmount();

	BigDecimal getPendingAmount();

	LocalDate getLastBookingDate();

	LocalTime getLastBookingTime();
}
