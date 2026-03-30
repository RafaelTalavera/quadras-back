package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.OffsetDateTime;

public record MaintenanceProviderDto(
		Long id,
		MaintenanceProviderType providerType,
		String name,
		String serviceLabel,
		String scopeDescription,
		String contact,
		Boolean active,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		String createdBy,
		String updatedBy
) {
	public static MaintenanceProviderDto from(MaintenanceProvider provider) {
		return new MaintenanceProviderDto(
				provider.getId(),
				provider.getProviderType(),
				provider.getName(),
				provider.getServiceLabel(),
				provider.getScopeDescription(),
				provider.getContact(),
				provider.isActive(),
				provider.getCreatedAt(),
				provider.getUpdatedAt(),
				provider.getCreatedBy(),
				provider.getUpdatedBy()
		);
	}
}
