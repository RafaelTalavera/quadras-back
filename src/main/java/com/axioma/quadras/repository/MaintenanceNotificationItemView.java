package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceNotificationStatus;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import java.time.OffsetDateTime;

public interface MaintenanceNotificationItemView {
	Long getId();
	Long getOrderId();
	MaintenanceNotificationType getNotificationType();
	MaintenanceNotificationStatus getStatus();
	String getTitle();
	String getBody();
	OffsetDateTime getDueAt();
	OffsetDateTime getReadAt();
	OffsetDateTime getResolvedAt();
	OffsetDateTime getDismissedAt();
	OffsetDateTime getCreatedAt();
}
