package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "maintenance_notifications")
public class MaintenanceNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private MaintenanceOrder order;

	@Column(name = "recipient_username", nullable = false, length = 120)
	private String recipientUsername;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false, length = 30)
	private MaintenanceNotificationType notificationType;

	@Column(name = "schedule_version", nullable = false)
	private long scheduleVersion;

	@Column(name = "occurrence_key", nullable = false, length = 80)
	private String occurrenceKey;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, length = 1000)
	private String body;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MaintenanceNotificationStatus status;

	@Column(name = "due_at", nullable = false)
	private OffsetDateTime dueAt;

	@Column(name = "read_at")
	private OffsetDateTime readAt;

	@Column(name = "resolved_at")
	private OffsetDateTime resolvedAt;

	@Column(name = "dismissed_at")
	private OffsetDateTime dismissedAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected MaintenanceNotification() {
	}

	public static MaintenanceNotification create(
			MaintenanceOrder order,
			String recipientUsername,
			MaintenanceNotificationType notificationType,
			long scheduleVersion,
			String occurrenceKey,
			String title,
			String body,
			OffsetDateTime dueAt
	) {
		if (order == null || notificationType == null || dueAt == null) {
			throw new IllegalArgumentException("order, notificationType and dueAt are required");
		}
		if (scheduleVersion < 0) {
			throw new IllegalArgumentException("scheduleVersion must be >= 0");
		}
		final MaintenanceNotification notification = new MaintenanceNotification();
		notification.order = order;
		notification.recipientUsername = requiredText(recipientUsername, "recipientUsername", 120);
		notification.notificationType = notificationType;
		notification.scheduleVersion = scheduleVersion;
		notification.occurrenceKey = requiredText(occurrenceKey, "occurrenceKey", 80);
		notification.title = requiredText(title, "title", 160);
		notification.body = requiredText(body, "body", 1000);
		notification.status = MaintenanceNotificationStatus.UNREAD;
		notification.dueAt = dueAt;
		return notification;
	}

	@PrePersist
	void onCreate() {
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public Long getId() {
		return id;
	}

	public MaintenanceOrder getOrder() {
		return order;
	}

	public String getRecipientUsername() {
		return recipientUsername;
	}

	public MaintenanceNotificationType getNotificationType() {
		return notificationType;
	}

	public long getScheduleVersion() {
		return scheduleVersion;
	}

	public String getOccurrenceKey() {
		return occurrenceKey;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public MaintenanceNotificationStatus getStatus() {
		return status;
	}

	public OffsetDateTime getDueAt() {
		return dueAt;
	}

	private static String requiredText(String value, String field, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(field + " must be <= " + maxLength + " chars");
		}
		return normalized;
	}
}
