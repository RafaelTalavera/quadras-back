package com.axioma.quadras.repository;

import java.math.BigDecimal;

public interface CourtSummaryBreakdownView {

	String getCode();

	long getScheduledCount();

	long getPaidCount();

	long getPendingCount();

	long getTotalMinutes();

	BigDecimal getCourtAmount();

	BigDecimal getMaterialsAmount();

	BigDecimal getTotalAmount();
}
