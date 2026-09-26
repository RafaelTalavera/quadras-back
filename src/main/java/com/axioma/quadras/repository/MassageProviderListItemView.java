package com.axioma.quadras.repository;

import java.time.OffsetDateTime;

public interface MassageProviderListItemView {
	Long getId();

	String getName();

	String getSpecialty();

	String getContact();
	String getWhatsappNumber();
	Boolean getWhatsappEnabled();

	Boolean getActive();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();
}
