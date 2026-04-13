package com.axioma.quadras.repository;

import java.time.OffsetDateTime;

public interface CourtPartnerCoachListItemView {
	Long getId();

	String getName();

	Boolean getActive();

	OffsetDateTime getUpdatedAt();

	String getUpdatedBy();
}
