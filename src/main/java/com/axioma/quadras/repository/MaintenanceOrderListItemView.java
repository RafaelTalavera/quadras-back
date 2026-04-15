package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface MaintenanceOrderListItemView {
	Long getId();

	Long getLocationId();

	MaintenanceLocationType getLocationTypeSnapshot();

	String getLocationCodeSnapshot();

	String getLocationLabelSnapshot();

	Long getProviderId();

	MaintenanceProviderType getProviderTypeSnapshot();

	String getProviderNameSnapshot();

	String getServiceLabelSnapshot();

	String getTitle();

	MaintenancePriority getPriority();

	Integer getEstimatedExecutionMinutes();

	MaintenanceOrderStatus getStatus();

	OffsetDateTime getReportedAt();

	LocalDateTime getScheduledStartAt();

	LocalDateTime getScheduledEndAt();

	OffsetDateTime getStartedAt();

	OffsetDateTime getCompletedAt();

	Boolean getPaid();
}
