package com.axioma.quadras.domain.dto;

import java.time.OffsetDateTime;

public record StartMaintenanceOrderDto(
		OffsetDateTime startedAt
) {
}
