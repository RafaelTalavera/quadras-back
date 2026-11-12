package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderKind;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.repository.ProviderMaintenanceOrderView;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ProviderMaintenanceOrderDto(
		Long id,
		MaintenanceLocationType locationType,
		String locationLabel,
		MaintenanceOrderKind orderKind,
		String title,
		String description,
		MaintenancePriority priority,
		Integer estimatedExecutionMinutes,
		MaintenanceOrderStatus status,
		OffsetDateTime reportedAt,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt,
		OffsetDateTime startedAt,
		OffsetDateTime completedAt,
		String resolutionNotes,
		String cancellationNotes
) {
	public static ProviderMaintenanceOrderDto from(ProviderMaintenanceOrderView order) {
		return new ProviderMaintenanceOrderDto(
				order.getId(), order.getLocationType(), order.getLocationLabel(),
				order.getOrderKind(), order.getTitle(), order.getDescription(), order.getPriority(),
				order.getEstimatedExecutionMinutes(), order.getStatus(), order.getReportedAt(),
				order.getScheduledStartAt(), order.getScheduledEndAt(), order.getStartedAt(),
				order.getCompletedAt(), order.getResolutionNotes(), order.getCancellationNotes()
		);
	}

	public static ProviderMaintenanceOrderDto from(MaintenanceOrderDto order) {
		return new ProviderMaintenanceOrderDto(
				order.id(), order.locationTypeSnapshot(), order.locationLabelSnapshot(), order.orderKind(),
				order.title(), order.description(), order.priority(), order.estimatedExecutionMinutes(),
				order.status(), order.reportedAt(), order.scheduledStartAt(), order.scheduledEndAt(),
				order.startedAt(), order.completedAt(), order.resolutionNotes(), order.cancellationNotes()
		);
	}
}
