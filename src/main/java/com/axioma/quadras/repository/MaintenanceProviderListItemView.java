package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.OffsetDateTime;

public interface MaintenanceProviderListItemView {
	Long getId();

	MaintenanceProviderType getProviderType();

	MaintenanceProviderSpecialty getSpecialty();

	String getName();

	String getServiceLabel();

	String getScopeDescription();

	String getContact();

	Boolean getActive();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();

	String getCreatedBy();

	String getUpdatedBy();
}
