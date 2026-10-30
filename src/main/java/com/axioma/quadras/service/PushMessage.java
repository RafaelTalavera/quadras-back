package com.axioma.quadras.service;

import java.time.OffsetDateTime;

public record PushMessage(
		String logicalDestination,
		String idempotencyKey,
		Long orderId,
		String title,
		String body,
		OffsetDateTime dueAt
) {
}
