package com.axioma.quadras.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ScheduleSyncEventDto(
		String domain,
		String action,
		Long entityId,
		LocalDate dateFrom,
		LocalDate dateTo,
		Instant occurredAt
) {
}
