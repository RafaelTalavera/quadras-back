package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceSummaryGroupBy;
import java.util.List;

public record MaintenanceSummaryDetailDto(
		MaintenanceSummaryGroupBy groupBy,
		String code,
		String label,
		MaintenanceSummaryBreakdownDto summary,
		List<MaintenanceSummaryDetailItemDto> items
) {
}
