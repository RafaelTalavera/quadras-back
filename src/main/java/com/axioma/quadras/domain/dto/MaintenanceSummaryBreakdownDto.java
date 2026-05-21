package com.axioma.quadras.domain.dto;

public record MaintenanceSummaryBreakdownDto(
		String groupKey,
		String code,
		String label,
		Integer openCount,
		Integer scheduledCount,
		Integer inProgressCount,
		Integer completedCount,
		Integer cancelledCount,
		Integer urgentCount
) {
}
