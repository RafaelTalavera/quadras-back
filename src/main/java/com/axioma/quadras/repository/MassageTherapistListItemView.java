package com.axioma.quadras.repository;

import java.time.OffsetDateTime;

public interface MassageTherapistListItemView {
	Long getId();

	Long getProviderId();

	String getName();

	Boolean getActive();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();
}
