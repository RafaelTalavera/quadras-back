package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderKind;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface ProviderMaintenanceOrderView {

	Long getId();

	MaintenanceLocationType getLocationType();

	String getLocationLabel();

	MaintenanceOrderKind getOrderKind();

	String getTitle();

	String getDescription();

	MaintenancePriority getPriority();

	Integer getEstimatedExecutionMinutes();

	MaintenanceOrderStatus getStatus();

	OffsetDateTime getReportedAt();

	LocalDateTime getScheduledStartAt();

	LocalDateTime getScheduledEndAt();

	OffsetDateTime getStartedAt();

	OffsetDateTime getCompletedAt();

	String getResolutionNotes();

	String getCancellationNotes();
}
