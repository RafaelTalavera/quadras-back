package com.axioma.quadras.repository;

public interface MaintenanceSummaryAggregateView {

	long getOpenCount();

	long getAssignedCount();

	long getScheduledCount();

	long getInProgressCount();

	long getCompletedCount();

	long getCancelledCount();

	long getInternalCount();

	long getExternalCount();

	long getUnassignedCount();

	long getRoomsCount();

	long getCommonAreasCount();

	long getUrgentCount();

	long getGuestPriorityCount();

	Double getAverageResolutionMinutes();
}
