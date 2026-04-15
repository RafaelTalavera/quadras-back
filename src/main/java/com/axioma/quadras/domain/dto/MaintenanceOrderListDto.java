package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.repository.MaintenanceOrderListItemView;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record MaintenanceOrderListDto(
		Long id,
		Long locationId,
		MaintenanceLocationType locationTypeSnapshot,
		String locationCodeSnapshot,
		String locationLabelSnapshot,
		Long providerId,
		MaintenanceProviderType providerTypeSnapshot,
		String providerNameSnapshot,
		String serviceLabelSnapshot,
		String title,
		MaintenancePriority priority,
		MaintenanceOrderStatus status,
		OffsetDateTime reportedAt,
		LocalDateTime expectedCompletionAt,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt,
		OffsetDateTime startedAt,
		OffsetDateTime completedAt,
		boolean paid
) {
	public static MaintenanceOrderListDto from(MaintenanceOrderListItemView item) {
		return new MaintenanceOrderListDto(
				item.getId(),
				item.getLocationId(),
				item.getLocationTypeSnapshot(),
				item.getLocationCodeSnapshot(),
				item.getLocationLabelSnapshot(),
				item.getProviderId(),
				item.getProviderTypeSnapshot(),
				item.getProviderNameSnapshot(),
				item.getServiceLabelSnapshot(),
				item.getTitle(),
				item.getPriority(),
				item.getStatus(),
				item.getReportedAt(),
				expectedCompletionAt(
						item.getScheduledEndAt(),
						item.getEstimatedExecutionMinutes(),
						item.getStartedAt(),
						item.getReportedAt()
				),
				item.getScheduledStartAt(),
				item.getScheduledEndAt(),
				item.getStartedAt(),
				item.getCompletedAt(),
				Boolean.TRUE.equals(item.getPaid())
		);
	}

	private static LocalDateTime expectedCompletionAt(
			LocalDateTime scheduledEndAt,
			Integer estimatedExecutionMinutes,
			OffsetDateTime startedAt,
			OffsetDateTime reportedAt
	) {
		if (scheduledEndAt != null) {
			return scheduledEndAt;
		}
		if (estimatedExecutionMinutes == null) {
			return null;
		}
		if (startedAt != null) {
			return startedAt.toLocalDateTime().plusMinutes(estimatedExecutionMinutes);
		}
		return reportedAt == null ? null : reportedAt.toLocalDateTime().plusMinutes(estimatedExecutionMinutes);
	}
}
