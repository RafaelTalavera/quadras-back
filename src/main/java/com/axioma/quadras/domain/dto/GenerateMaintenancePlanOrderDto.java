package com.axioma.quadras.domain.dto;

import java.time.LocalDateTime;

public record GenerateMaintenancePlanOrderDto(
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt
) {
}
