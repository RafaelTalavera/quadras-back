package com.axioma.quadras.domain.dto;

import java.util.List;

public record MaintenanceNotificationPageDto(
		int page,
		int size,
		boolean hasNext,
		List<MaintenanceNotificationDto> items
) {
}
