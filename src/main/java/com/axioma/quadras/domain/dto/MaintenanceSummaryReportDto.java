package com.axioma.quadras.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record MaintenanceSummaryReportDto(
		Integer openCount,
		Integer scheduledCount,
		Integer inProgressCount,
		Integer completedCount,
		Integer cancelledCount,
		Integer internalCount,
		Integer externalCount,
		Integer roomsCount,
		Integer commonAreasCount,
		Integer urgentCount,
		BigDecimal averageResolutionHours,
		List<MaintenanceSummaryBreakdownDto> providerBreakdown,
		List<MaintenanceSummaryBreakdownDto> providerTypeBreakdown,
		List<MaintenanceSummaryBreakdownDto> locationTypeBreakdown,
		List<MaintenanceSummaryBreakdownDto> statusBreakdown
) {
}
