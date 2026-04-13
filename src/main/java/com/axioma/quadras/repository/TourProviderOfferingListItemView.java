package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourServiceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface TourProviderOfferingListItemView {
	Long getId();

	Long getProviderId();

	TourServiceType getServiceType();

	String getName();

	BigDecimal getAmount();

	String getDescription();

	Boolean getActive();

	OffsetDateTime getUpdatedAt();

	String getUpdatedBy();
}
