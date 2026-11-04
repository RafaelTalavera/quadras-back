package com.axioma.quadras.domain.dto;

import java.util.List;

public record LaundryReturnPageDto(int page, int size, boolean hasNext,
		List<LaundryReturnSummaryDto> items) { }
