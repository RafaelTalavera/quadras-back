package com.axioma.quadras.service;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceNotificationDetector {

	private final MaintenanceOrderRepository orderRepository;
	private final MaintenanceNotificationService notificationService;
	private final ScheduleLockService scheduleLockService;
	private final MaintenanceAlertProperties properties;
	private final Clock operationalClock;

	public MaintenanceNotificationDetector(
			MaintenanceOrderRepository orderRepository,
			MaintenanceNotificationService notificationService,
			ScheduleLockService scheduleLockService,
			MaintenanceAlertProperties properties,
			@Qualifier("maintenanceOperationalClock") Clock operationalClock
	) {
		this.orderRepository = orderRepository;
		this.notificationService = notificationService;
		this.scheduleLockService = scheduleLockService;
		this.properties = properties;
		this.operationalClock = operationalClock;
	}

	@Transactional
	public int detect() {
		if (!properties.inAppEnabled()) {
			return 0;
		}
		final LocalDateTime now = LocalDateTime.now(operationalClock);
		if (!isInsideOperationalWindow(now.toLocalTime())) {
			return 0;
		}
		scheduleLockService.acquireMaintenanceNotificationDate(now.toLocalDate());
		final PageRequest batch = PageRequest.of(0, properties.detectorBatchSize());
		final List<MaintenanceOrder> dueToStart = orderRepository.findDueToStartNotificationCandidates(
				now,
				MaintenanceNotificationType.DUE_TO_START,
				batch
		);
		final List<MaintenanceOrder> overdue = orderRepository.findOverdueNotificationCandidates(
				now,
				OffsetDateTime.ofInstant(
						operationalClock.instant().minus(properties.overdueReminderInterval()),
						ZoneOffset.UTC
				),
				MaintenanceNotificationType.OVERDUE,
				batch
		);
		return notificationService.createNotifications(
				dueToStart, MaintenanceNotificationType.DUE_TO_START, now)
				+ notificationService.createNotifications(overdue, MaintenanceNotificationType.OVERDUE, now);
	}

	private boolean isInsideOperationalWindow(LocalTime time) {
		return !time.isBefore(properties.operationalStart()) && time.isBefore(properties.operationalEnd());
	}
}
