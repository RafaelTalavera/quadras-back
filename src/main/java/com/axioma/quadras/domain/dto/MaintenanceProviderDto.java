package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.repository.MaintenanceProviderListItemView;
import java.time.OffsetDateTime;

public record MaintenanceProviderDto(
		Long id,
		MaintenanceProviderType providerType,
		MaintenanceProviderSpecialty specialty,
		String name,
		String serviceLabel,
		String scopeDescription,
		String contact,
		String whatsappNumber,
		Boolean whatsappEnabled,
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
				provider.getSpecialty(),
				provider.getName(),
				provider.getServiceLabel(),
				provider.getScopeDescription(),
				provider.getContact(),
				provider.getWhatsappNumber(),
				provider.isWhatsappEnabled(),
				provider.isActive(),
				provider.getCreatedAt(),
				provider.getUpdatedAt(),
				provider.getCreatedBy(),
				provider.getUpdatedBy()
		);
	}

	public static MaintenanceProviderDto from(MaintenanceProviderListItemView provider) {
		return new MaintenanceProviderDto(
				provider.getId(),
				provider.getProviderType(),
				provider.getSpecialty(),
				provider.getName(),
				provider.getServiceLabel(),
				provider.getScopeDescription(),
				provider.getContact(),
				provider.getWhatsappNumber(),
				Boolean.TRUE.equals(provider.getWhatsappEnabled()),
				Boolean.TRUE.equals(provider.getActive()),
				provider.getCreatedAt(),
				provider.getUpdatedAt(),
				provider.getCreatedBy(),
				provider.getUpdatedBy()
		);
	}
}
