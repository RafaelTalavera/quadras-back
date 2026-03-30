package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import java.time.LocalDateTime;

public record MaintenanceConflictDto(
		Long id,
		String title,
		MaintenanceOrderStatus status,
		String locationLabelSnapshot,
		String providerNameSnapshot,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt
) {
	public static MaintenanceConflictDto from(MaintenanceOrder order) {
		return new MaintenanceConflictDto(
				order.getId(),
				order.getTitle(),
				order.getStatus(),
				order.getLocationLabelSnapshot(),
				order.getProviderNameSnapshot(),
				order.getScheduledStartAt(),
				order.getScheduledEndAt()
		);
	}
}
