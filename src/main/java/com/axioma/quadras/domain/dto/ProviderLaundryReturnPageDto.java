package com.axioma.quadras.domain.dto;

import java.util.List;

public record ProviderLaundryReturnPageDto(int page, int size, boolean hasNext,
		List<ProviderLaundryReturnSummaryDto> items) { }
