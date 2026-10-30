package com.axioma.quadras.repository;

import java.time.OffsetDateTime;

public record NotificationOutboxDispatchItem(
		Long outboxId,
		String logicalDestination,
		String idempotencyKey,
		int attemptCount,
		Long orderId,
		String title,
		String body,
		OffsetDateTime dueAt
) {
}
