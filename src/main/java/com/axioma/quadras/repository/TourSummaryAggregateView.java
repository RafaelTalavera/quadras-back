package com.axioma.quadras.repository;

import java.math.BigDecimal;

public interface TourSummaryAggregateView {

	long getScheduledCount();

	long getCancelledCount();

	long getPaidCount();

	long getPendingCount();

	long getTotalMinutes();

	BigDecimal getGrossAmount();

	BigDecimal getPaidAmount();

	BigDecimal getPendingAmount();

	BigDecimal getCommissionAmount();
}
