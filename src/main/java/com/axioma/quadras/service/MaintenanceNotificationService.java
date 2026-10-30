package com.axioma.quadras.service;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.MaintenanceNotification;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.NotificationChannel;
import com.axioma.quadras.domain.model.NotificationOutbox;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.MaintenanceNotificationRepository;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceNotificationService {

	private final AppUserRepository appUserRepository;
	private final MaintenanceNotificationRepository notificationRepository;
	private final NotificationOutboxRepository outboxRepository;
	private final MaintenanceAlertProperties properties;
	private final MaintenanceNotificationEventPublisher eventPublisher;

	public MaintenanceNotificationService(
			AppUserRepository appUserRepository,
			MaintenanceNotificationRepository notificationRepository,
			NotificationOutboxRepository outboxRepository,
			MaintenanceAlertProperties properties,
			MaintenanceNotificationEventPublisher eventPublisher
	) {
		this.appUserRepository = appUserRepository;
		this.notificationRepository = notificationRepository;
		this.outboxRepository = outboxRepository;
		this.properties = properties;
		this.eventPublisher = eventPublisher;
	}

	public int createNotifications(
			List<MaintenanceOrder> orders,
			MaintenanceNotificationType type,
			LocalDateTime detectedAt
	) {
		if (orders.isEmpty()) {
			return 0;
		}
		final List<AppUser> activeUsers = appUserRepository.findAllByEnabledTrueOrderByUsernameAsc();
		final Set<String> operationalRecipients = new LinkedHashSet<>();
		for (AppUser user : activeUsers) {
			operationalRecipients.add(user.getUsername());
		}

		int created = 0;
		for (MaintenanceOrder order : orders) {
			final String occurrenceKey = occurrenceKey(order, type, detectedAt);
			final Set<String> recipients = new LinkedHashSet<>(operationalRecipients);
			if (order.getAssignedUsername() != null) {
				activeUsers.stream()
						.filter(user -> user.getUsername().equals(order.getAssignedUsername()))
						.findFirst()
						.ifPresent(user -> recipients.add(user.getUsername()));
			}
			for (String recipient : recipients) {
				created += createNotification(order, type, recipient, occurrenceKey);
			}
		}
		if (created > 0) {
			eventPublisher.publishChanged();
		}
		return created;
	}

	private int createNotification(
			MaintenanceOrder order,
			MaintenanceNotificationType type,
			String recipient,
			String occurrenceKey
	) {
		if (notificationRepository.existsByOrderIdAndNotificationTypeAndScheduleVersionAndRecipientUsernameAndOccurrenceKey(
				order.getId(), type, order.getScheduleVersion(), recipient, occurrenceKey)) {
			return 0;
		}
		final LocalDateTime localDueAt = type == MaintenanceNotificationType.DUE_TO_START
				? order.getScheduledStartAt()
				: order.getScheduledEndAt();
		final OffsetDateTime dueAt = localDueAt.atZone(properties.zoneId()).toOffsetDateTime();
		final String title = type == MaintenanceNotificationType.DUE_TO_START
				? "Manutencao pronta para iniciar"
				: "Manutencao atrasada";
		final String body = order.getLocationLabelSnapshot() + " - " + order.getTitle();
		final MaintenanceNotification notification = notificationRepository.save(
				MaintenanceNotification.create(
						order,
						recipient,
						type,
						order.getScheduleVersion(),
						occurrenceKey,
						title,
						body,
						dueAt
				)
		);
		if (properties.pushEnabled()) {
			final String idempotencyKey = String.join(":",
					"maintenance",
					String.valueOf(order.getId()),
					type.name(),
					String.valueOf(order.getScheduleVersion()),
					occurrenceKey,
					recipient,
					NotificationChannel.PUSH.name()
			);
			outboxRepository.save(NotificationOutbox.pending(
					notification,
					NotificationChannel.PUSH,
					recipient,
					idempotencyKey,
					OffsetDateTime.now()
			));
		}
		return 1;
	}

	private String occurrenceKey(
			MaintenanceOrder order,
			MaintenanceNotificationType type,
			LocalDateTime detectedAt
	) {
		if (type == MaintenanceNotificationType.DUE_TO_START) {
			return "INITIAL";
		}
		final long elapsedSeconds = Math.max(
				0,
				Duration.between(order.getScheduledEndAt(), detectedAt).toSeconds()
		);
		final long intervalSeconds = properties.overdueReminderInterval().toSeconds();
		return "OVERDUE-" + (elapsedSeconds / intervalSeconds);
	}
}
