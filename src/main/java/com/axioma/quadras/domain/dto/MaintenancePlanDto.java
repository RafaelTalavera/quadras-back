package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenancePlan;
import com.axioma.quadras.domain.model.MaintenancePlanRecurrenceUnit;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MaintenancePlanDto(
		Long id,
		Long locationId,
		String locationCode,
		String locationLabel,
		Long providerId,
		MaintenanceProviderType providerType,
		MaintenanceProviderSpecialty providerSpecialty,
		String providerName,
		String serviceLabel,
		String title,
		String description,
		MaintenancePlanRecurrenceUnit recurrenceUnit,
		Integer recurrenceInterval,
		LocalDate nextDueDate,
		LocalDate lastGeneratedOn,
		boolean active,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		String createdBy,
		String updatedBy
) {
	public static MaintenancePlanDto from(MaintenancePlan plan) {
		return new MaintenancePlanDto(
				plan.getId(),
				plan.getLocation().getId(),
				plan.getLocation().getCode(),
				plan.getLocation().getLabel(),
				plan.getProvider() == null ? null : plan.getProvider().getId(),
				plan.getProvider() == null ? null : plan.getProvider().getProviderType(),
				plan.getProvider() == null ? null : plan.getProvider().getSpecialty(),
				plan.getProvider() == null ? null : plan.getProvider().getName(),
				plan.getProvider() == null ? null : plan.getProvider().getServiceLabel(),
				plan.getTitle(),
				plan.getDescription(),
				plan.getRecurrenceUnit(),
				plan.getRecurrenceInterval(),
				plan.getNextDueDate(),
				plan.getLastGeneratedOn(),
				plan.isActive(),
				plan.getCreatedAt(),
				plan.getUpdatedAt(),
				plan.getCreatedBy(),
				plan.getUpdatedBy()
		);
	}
}
