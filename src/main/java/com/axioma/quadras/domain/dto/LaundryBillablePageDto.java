package com.axioma.quadras.domain.dto;

import java.util.List;

public record LaundryBillablePageDto(int page, int size, boolean hasNext,
		List<LaundryBillableEventDto> items) { }
