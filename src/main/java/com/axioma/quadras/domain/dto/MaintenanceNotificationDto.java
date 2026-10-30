package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceNotificationStatus;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import com.axioma.quadras.repository.MaintenanceNotificationItemView;
import java.time.OffsetDateTime;

public record MaintenanceNotificationDto(
		Long id,
		Long orderId,
		MaintenanceNotificationType notificationType,
		MaintenanceNotificationStatus status,
		String title,
		String body,
		OffsetDateTime dueAt,
		OffsetDateTime readAt,
		OffsetDateTime resolvedAt,
		OffsetDateTime dismissedAt,
		OffsetDateTime createdAt
) {
	public static MaintenanceNotificationDto from(MaintenanceNotificationItemView item) {
		return new MaintenanceNotificationDto(
				item.getId(),
				item.getOrderId(),
				item.getNotificationType(),
				item.getStatus(),
				item.getTitle(),
				item.getBody(),
				item.getDueAt(),
				item.getReadAt(),
				item.getResolvedAt(),
				item.getDismissedAt(),
				item.getCreatedAt()
		);
	}
}
