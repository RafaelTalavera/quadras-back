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
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "notification_id", nullable = false)
	private MaintenanceNotification notification;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationChannel channel;

	@Column(name = "logical_destination", nullable = false, length = 160)
	private String logicalDestination;

	@Enumerated(EnumType.STRING)
	@Column(name = "delivery_status", nullable = false, length = 20)
	private NotificationDeliveryStatus deliveryStatus;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "next_attempt_at")
	private OffsetDateTime nextAttemptAt;

	@Column(name = "claimed_at")
	private OffsetDateTime claimedAt;

	@Column(name = "last_error", length = 500)
	private String lastError;

	@Column(name = "idempotency_key", nullable = false, length = 220)
	private String idempotencyKey;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "delivered_at")
	private OffsetDateTime deliveredAt;

	@Column(name = "abandoned_at")
	private OffsetDateTime abandonedAt;

	protected NotificationOutbox() {
	}

	public static NotificationOutbox pending(
			MaintenanceNotification notification,
			NotificationChannel channel,
			String logicalDestination,
			String idempotencyKey,
			OffsetDateTime nextAttemptAt
	) {
		if (notification == null || channel == null) {
			throw new IllegalArgumentException("notification and channel are required");
		}
		final NotificationOutbox outbox = new NotificationOutbox();
		outbox.notification = notification;
		outbox.channel = channel;
		outbox.logicalDestination = requiredText(logicalDestination, "logicalDestination", 160);
		outbox.idempotencyKey = requiredText(idempotencyKey, "idempotencyKey", 220);
		outbox.deliveryStatus = NotificationDeliveryStatus.PENDING;
		outbox.attemptCount = 0;
		outbox.nextAttemptAt = nextAttemptAt;
		return outbox;
	}

	@PrePersist
	void onCreate() {
		createdAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public Long getId() {
		return id;
	}

	public MaintenanceNotification getNotification() {
		return notification;
	}

	public NotificationChannel getChannel() {
		return channel;
	}

	public String getLogicalDestination() {
		return logicalDestination;
	}

	public NotificationDeliveryStatus getDeliveryStatus() {
		return deliveryStatus;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public OffsetDateTime getNextAttemptAt() {
		return nextAttemptAt;
	}

	public String getLastError() {
		return lastError;
	}

	public OffsetDateTime getClaimedAt() {
		return claimedAt;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public OffsetDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public OffsetDateTime getAbandonedAt() {
		return abandonedAt;
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
