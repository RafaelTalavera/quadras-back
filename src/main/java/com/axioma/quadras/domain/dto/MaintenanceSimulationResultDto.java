package com.axioma.quadras.domain.dto;

import java.time.LocalDate;

public record MaintenanceSimulationResultDto(
		LocalDate dateFrom,
		LocalDate dateTo,
		long seed,
		boolean resetPreviousSimulation,
		int locationsCreated,
		int providersCreated,
		int ordersDeleted,
		int locationsDeleted,
		int providersDeleted,
		int ordersCreated,
		int openCount,
		int assignedCount,
		int scheduledCount,
		int inProgressCount,
		int completedCount,
		int cancelledCount
) {
}
