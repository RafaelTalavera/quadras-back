package com.axioma.quadras.repository;

public interface MaintenanceSummaryBreakdownView {

	String getCode();

	String getLabel();

	long getOpenCount();

	long getScheduledCount();

	long getInProgressCount();

	long getCompletedCount();

	long getCancelledCount();

	long getUrgentCount();
}
