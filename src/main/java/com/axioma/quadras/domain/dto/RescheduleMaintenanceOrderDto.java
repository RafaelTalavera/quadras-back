package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record RescheduleMaintenanceOrderDto(
		@NotNull(message = "scheduledStartAt is required")
		LocalDateTime scheduledStartAt,
		@NotNull(message = "scheduledEndAt is required")
		LocalDateTime scheduledEndAt,
		@NotBlank(message = "reason is required")
		@Size(max = 500, message = "reason must be <= 500 chars")
		String reason,
		@NotNull(message = "expectedScheduleVersion is required")
		@PositiveOrZero(message = "expectedScheduleVersion must be >= 0")
		Long expectedScheduleVersion
) {
}
