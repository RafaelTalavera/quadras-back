package com.axioma.quadras.domain.dto;

import java.util.List;

public record ProviderLaundryDocumentPageDto(int page, int size, boolean hasNext,
		List<ProviderLaundryDocumentSummaryDto> items) { }
