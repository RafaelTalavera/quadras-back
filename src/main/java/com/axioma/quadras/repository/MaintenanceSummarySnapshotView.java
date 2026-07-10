package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface MaintenanceSummarySnapshotView {

	Long getProviderId();

	String getProviderNameSnapshot();

	MaintenanceProviderType getProviderTypeSnapshot();

	MaintenanceLocationType getLocationTypeSnapshot();

	MaintenanceOrderStatus getStatus();

	MaintenancePriority getPriority();

	OffsetDateTime getReportedAt();

	LocalDateTime getScheduledStartAt();

	OffsetDateTime getStartedAt();

	OffsetDateTime getCompletedAt();
}
