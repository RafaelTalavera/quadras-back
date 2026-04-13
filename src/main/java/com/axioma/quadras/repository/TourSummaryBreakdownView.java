package com.axioma.quadras.repository;

import java.math.BigDecimal;

public interface TourSummaryBreakdownView {

	String getCode();

	String getLabel();

	Boolean getActive();

	long getScheduledCount();

	long getPaidCount();

	long getPendingCount();

	long getTotalMinutes();

	BigDecimal getGrossAmount();

	BigDecimal getPaidAmount();

	BigDecimal getPendingAmount();

	BigDecimal getCommissionAmount();
}
