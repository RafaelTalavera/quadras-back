package com.axioma.quadras.service;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceNotificationEventPublisher {

	public static final String NOTIFICATIONS_CHANGED_ACTION = "notifications-changed";

	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final MaintenanceAlertProperties properties;

	public MaintenanceNotificationEventPublisher(
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			MaintenanceAlertProperties properties
	) {
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.properties = properties;
	}

	public void publishChanged() {
		if (!properties.sseEnabled()) {
			return;
		}
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MAINTENANCE,
				NOTIFICATIONS_CHANGED_ACTION,
				null,
				null,
				null
		);
	}
}
