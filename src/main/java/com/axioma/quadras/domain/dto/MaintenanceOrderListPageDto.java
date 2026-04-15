package com.axioma.quadras.domain.dto;

import java.util.List;

public record MaintenanceOrderListPageDto(
		int page,
		int size,
		boolean hasNext,
		List<MaintenanceOrderListDto> items
) {
}
