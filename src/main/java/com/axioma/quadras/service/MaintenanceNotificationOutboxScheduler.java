package com.axioma.quadras.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(MaintenanceNotificationOutboxDispatcher.class)
@ConditionalOnProperty(
		prefix = "costanorte.maintenance.alerts",
		name = "push-enabled",
		havingValue = "true"
)
public class MaintenanceNotificationOutboxScheduler {

	private final MaintenanceNotificationOutboxDispatcher dispatcher;

	public MaintenanceNotificationOutboxScheduler(MaintenanceNotificationOutboxDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Scheduled(fixedDelayString = "${costanorte.maintenance.alerts.outbox-dispatcher-interval}")
	public void dispatchNotifications() {
		dispatcher.dispatch();
	}
}
