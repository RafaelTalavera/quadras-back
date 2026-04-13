package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface MaintenanceSummaryDetailItemView {

	Long getOrderId();

	MaintenanceLocationType getLocationTypeSnapshot();

	String getLocationLabelSnapshot();

	MaintenanceProviderType getProviderTypeSnapshot();

	String getProviderNameSnapshot();

	String getServiceLabelSnapshot();

	String getTitle();

	MaintenancePriority getPriority();

	MaintenanceBusinessPriority getBusinessPriority();

	MaintenanceRequestOrigin getRequestOrigin();

	Boolean getRequestedForGuest();

	String getAssignedUsername();

	Integer getEstimatedExecutionMinutes();

	MaintenanceOrderStatus getStatus();

	OffsetDateTime getReportedAt();

	LocalDateTime getScheduledStartAt();

	LocalDateTime getScheduledEndAt();

	OffsetDateTime getStartedAt();

	OffsetDateTime getCompletedAt();
}
