package com.axioma.quadras.domain.dto;

import java.util.List;

public record LaundryDocumentPageDto(int page, int size, boolean hasNext,
		List<LaundryDocumentSummaryDto> items) { }
