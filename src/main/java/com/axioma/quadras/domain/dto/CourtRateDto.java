package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import com.axioma.quadras.domain.model.CourtRate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CourtRateDto(
		Long id,
		CourtCustomerType customerType,
		CourtPricingPeriod pricingPeriod,
		BigDecimal amount,
		boolean active,
		OffsetDateTime updatedAt,
		String updatedBy
) {
	public static CourtRateDto from(CourtRate rate) {
		return new CourtRateDto(
				rate.getId(),
				rate.getCustomerType(),
				rate.getPricingPeriod(),
				rate.getAmount(),
				rate.isActive(),
				rate.getUpdatedAt(),
				rate.getUpdatedBy()
		);
	}
}
