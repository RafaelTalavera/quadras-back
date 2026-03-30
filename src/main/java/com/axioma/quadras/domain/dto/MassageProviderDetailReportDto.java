package com.axioma.quadras.domain.dto;

import java.util.List;

public record MassageProviderDetailReportDto(
		Long providerId,
		String providerName,
		boolean providerActive,
		MassageProviderSummaryDto summary,
		List<MassageProviderReportItemDto> items
) {
}
