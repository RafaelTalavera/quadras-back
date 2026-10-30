package com.axioma.quadras.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
		prefix = "costanorte.maintenance.alerts",
		name = "detector-enabled",
		havingValue = "true"
)
public class MaintenanceNotificationScheduler {

	private final MaintenanceNotificationDetector detector;

	public MaintenanceNotificationScheduler(MaintenanceNotificationDetector detector) {
		this.detector = detector;
	}

	@Scheduled(fixedDelayString = "${costanorte.maintenance.alerts.detector-interval}")
	public void detectNotifications() {
		detector.detect();
	}
}
