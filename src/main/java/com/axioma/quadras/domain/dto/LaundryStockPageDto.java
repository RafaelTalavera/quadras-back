package com.axioma.quadras.domain.dto;

import java.util.List;

public record LaundryStockPageDto(
		int page,
		int size,
		boolean hasNext,
		List<LaundryStockRowDto> items
) {
}
