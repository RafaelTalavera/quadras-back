package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface CourtRateListItemView {
	Long getId();

	CourtCustomerType getCustomerType();

	CourtPricingPeriod getPricingPeriod();

	BigDecimal getAmount();

	Boolean getActive();

	OffsetDateTime getUpdatedAt();

	String getUpdatedBy();
}
