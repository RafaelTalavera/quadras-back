package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourSummaryGroupBy;
import java.util.List;

public record TourSummaryDetailDto(
		TourSummaryGroupBy groupBy,
		String code,
		String label,
		Boolean active,
		TourSummaryBreakdownDto summary,
		List<TourSummaryDetailItemDto> items
) {
}
