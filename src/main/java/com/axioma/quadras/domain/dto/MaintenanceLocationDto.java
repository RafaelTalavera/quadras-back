package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationCategory;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.repository.MaintenanceLocationListItemView;
import java.time.OffsetDateTime;

public record MaintenanceLocationDto(
		Long id,
		MaintenanceLocationType locationType,
		MaintenanceLocationCategory locationCategory,
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
				location.getLocationCategory(),
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

	public static MaintenanceLocationDto from(MaintenanceLocationListItemView location) {
		return new MaintenanceLocationDto(
				location.getId(),
				location.getLocationType(),
				location.getLocationCategory(),
				location.getCode(),
				location.getLabel(),
				location.getFloor(),
				location.getDescription(),
				Boolean.TRUE.equals(location.getActive()),
				location.getCreatedAt(),
				location.getUpdatedAt(),
				location.getCreatedBy(),
				location.getUpdatedBy()
		);
	}
}
