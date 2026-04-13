package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceLocationCategory;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import java.time.OffsetDateTime;

public interface MaintenanceLocationListItemView {
	Long getId();

	MaintenanceLocationType getLocationType();

	MaintenanceLocationCategory getLocationCategory();

	String getCode();

	String getLabel();

	String getFloor();

	String getDescription();

	Boolean getActive();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();

	String getCreatedBy();

	String getUpdatedBy();
}
