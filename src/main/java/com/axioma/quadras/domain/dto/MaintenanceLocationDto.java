package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import java.time.OffsetDateTime;

public record MaintenanceLocationDto(
		Long id,
		MaintenanceLocationType locationType,
		String code,
		String label,
		String floor,
		String description,
		Boolean active,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		String createdBy,
		String updatedBy
) {
	public static MaintenanceLocationDto from(MaintenanceLocation location) {
		return new MaintenanceLocationDto(
				location.getId(),
				location.getLocationType(),
				location.getCode(),
				location.getLabel(),
				location.getFloor(),
				location.getDescription(),
				location.isActive(),
				location.getCreatedAt(),
				location.getUpdatedAt(),
				location.getCreatedBy(),
				location.getUpdatedBy()
		);
	}
}
