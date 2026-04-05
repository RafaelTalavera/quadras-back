package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record MaintenanceSummaryDetailItemDto(
		Long orderId,
		MaintenanceLocationType locationType,
		String locationLabel,
		MaintenanceProviderType providerType,
		String providerName,
		String serviceLabel,
		String title,
		MaintenancePriority priority,
		MaintenanceBusinessPriority businessPriority,
		MaintenanceRequestOrigin requestOrigin,
		Boolean requestedForGuest,
		String assignedUsername,
		Integer estimatedExecutionMinutes,
		MaintenanceOrderStatus status,
		OffsetDateTime reportedAt,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt,
		OffsetDateTime startedAt,
		OffsetDateTime completedAt
) {
	public static MaintenanceSummaryDetailItemDto from(MaintenanceOrder order) {
		return new MaintenanceSummaryDetailItemDto(
				order.getId(),
				order.getLocationTypeSnapshot(),
				order.getLocationLabelSnapshot(),
				order.getProviderTypeSnapshot(),
				order.getProviderNameSnapshot(),
				order.getServiceLabelSnapshot(),
				order.getTitle(),
				order.getPriority(),
				order.getBusinessPriority(),
				order.getRequestOrigin(),
				order.isRequestedForGuest(),
				order.getAssignedUsername(),
				order.getEstimatedExecutionMinutes(),
				order.getStatus(),
				order.getReportedAt(),
				order.getScheduledStartAt(),
				order.getScheduledEndAt(),
				order.getStartedAt(),
				order.getCompletedAt()
		);
	}
}
