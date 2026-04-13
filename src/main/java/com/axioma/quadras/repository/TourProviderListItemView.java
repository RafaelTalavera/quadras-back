package com.axioma.quadras.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface TourProviderListItemView {
	Long getId();

	String getName();

	String getContact();

	BigDecimal getDefaultCommissionPercent();

	Boolean getActive();

	OffsetDateTime getUpdatedAt();

	String getUpdatedBy();
}
