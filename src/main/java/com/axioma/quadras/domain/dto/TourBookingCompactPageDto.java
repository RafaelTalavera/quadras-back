package com.axioma.quadras.domain.dto;

import java.util.List;

public record TourBookingCompactPageDto(
		int page,
		int size,
		boolean hasNext,
		List<TourBookingCompactDto> items
) {
}
