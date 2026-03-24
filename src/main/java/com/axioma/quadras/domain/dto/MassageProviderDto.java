package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageProvider;
import java.time.OffsetDateTime;
import java.util.List;

public record MassageProviderDto(
		Long id,
		String name,
		String specialty,
		String contact,
		boolean active,
		List<MassageTherapistDto> therapists,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	public static MassageProviderDto from(MassageProvider provider) {
		return new MassageProviderDto(
				provider.getId(),
				provider.getName(),
				provider.getSpecialty(),
				provider.getContact(),
				provider.isActive(),
				provider.getTherapists().stream().map(MassageTherapistDto::from).toList(),
				provider.getCreatedAt(),
				provider.getUpdatedAt()
		);
	}
}
