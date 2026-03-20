package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageProvider;
import java.time.OffsetDateTime;

public record MassageProviderDto(
		Long id,
		String name,
		String specialty,
		String contact,
		boolean active,
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
				provider.getCreatedAt(),
				provider.getUpdatedAt()
		);
	}
}
