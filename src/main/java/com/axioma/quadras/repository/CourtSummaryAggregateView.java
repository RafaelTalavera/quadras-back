package com.axioma.quadras.repository;

import java.math.BigDecimal;

public interface CourtSummaryAggregateView {

	long getScheduledCount();

	long getCancelledCount();

	long getPaidCount();

	long getPendingCount();

	long getTotalMinutes();

	long getGuestMinutes();

	long getVipMinutes();

	long getExternalMinutes();

	long getPartnerCoachMinutes();

	BigDecimal getPaidAmount();

	BigDecimal getPendingAmount();

	BigDecimal getCourtAmount();

	BigDecimal getMaterialsAmount();
}
