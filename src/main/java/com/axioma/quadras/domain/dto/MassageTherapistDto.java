package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageTherapist;
import java.time.OffsetDateTime;

public record MassageTherapistDto(
		Long id,
		String name,
		boolean active,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	public static MassageTherapistDto from(MassageTherapist therapist) {
		return new MassageTherapistDto(
				therapist.getId(),
				therapist.getName(),
				therapist.isActive(),
				therapist.getCreatedAt(),
				therapist.getUpdatedAt()
		);
	}
}
