package com.axioma.quadras.service;

import com.axioma.quadras.repository.MaintenanceNotificationRepository;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceNotificationLifecycleService {

	private static final String ORDER_CHANGED_REASON = "Maintenance order lifecycle changed";

	private final MaintenanceNotificationRepository notificationRepository;
	private final NotificationOutboxRepository outboxRepository;
	private final Clock operationalClock;
	private final MaintenanceNotificationEventPublisher eventPublisher;

	public MaintenanceNotificationLifecycleService(
			MaintenanceNotificationRepository notificationRepository,
			NotificationOutboxRepository outboxRepository,
			@Qualifier("maintenanceOperationalClock") Clock operationalClock,
			MaintenanceNotificationEventPublisher eventPublisher
	) {
		this.notificationRepository = notificationRepository;
		this.outboxRepository = outboxRepository;
		this.operationalClock = operationalClock;
		this.eventPublisher = eventPublisher;
	}

	public int resolveActiveForOrder(Long orderId) {
		final OffsetDateTime resolvedAt = OffsetDateTime.ofInstant(
				operationalClock.instant(),
				ZoneOffset.UTC
		);
		outboxRepository.abandonPendingForActiveNotificationsByOrderId(
				orderId,
				resolvedAt,
				ORDER_CHANGED_REASON
		);
		final int resolved = notificationRepository.resolveActiveByOrderId(orderId, resolvedAt);
		if (resolved > 0) {
			eventPublisher.publishChanged();
		}
		return resolved;
	}
}
