package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LoadMaintenanceSimulationDto(
		@Min(value = 1, message = "daysBack must be >= 1")
		@Max(value = 180, message = "daysBack must be <= 180")
		Integer daysBack,
		@Min(value = 0, message = "daysForward must be >= 0")
		@Max(value = 90, message = "daysForward must be <= 90")
		Integer daysForward,
		@Min(value = 1, message = "ordersPerDay must be >= 1")
		@Max(value = 40, message = "ordersPerDay must be <= 40")
		Integer ordersPerDay,
		@Min(value = 0, message = "backlogOrders must be >= 0")
		@Max(value = 100, message = "backlogOrders must be <= 100")
		Integer backlogOrders,
		Boolean resetPreviousSimulation,
		Long seed
) {
}
