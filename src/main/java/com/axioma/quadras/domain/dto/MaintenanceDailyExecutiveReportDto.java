package com.axioma.quadras.domain.dto;

import java.util.List;

public record MaintenanceDailyExecutiveReportDto(
		Integer carryOverOpenCount,
		Integer dayOpenCount,
		Integer totalOpenCount,
		Integer urgentCount,
		Integer scheduledCount,
		Integer inProgressCount,
		Integer unassignedCount,
		List<MaintenanceOrderDto> carryOverOrders,
		List<MaintenanceOrderDto> dayOpenOrders
) {
}
