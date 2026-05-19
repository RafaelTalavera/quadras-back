package com.axioma.quadras.service;

import java.time.Instant;
import java.time.LocalDate;

public record ScheduleSyncChangeRequestedEvent(
		ScheduleSyncDomain domain,
		String action,
		Long entityId,
		LocalDate dateFrom,
		LocalDate dateTo,
		Instant occurredAt
) {
}
