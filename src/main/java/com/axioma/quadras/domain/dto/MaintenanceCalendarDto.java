package com.axioma.quadras.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MaintenanceCalendarDto(
		LocalDateTime rangeStart,
		LocalDateTime rangeEndExclusive,
		List<MaintenanceOrderDto> scheduledOrders,
		List<MaintenanceOrderDto> backlogOrders,
		boolean backlogHasMore
) {
}
