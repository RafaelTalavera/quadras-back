package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.repository.MassageTherapistListItemView;
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

	public static MassageTherapistDto from(MassageTherapistListItemView therapist) {
		return new MassageTherapistDto(
				therapist.getId(),
				therapist.getName(),
				Boolean.TRUE.equals(therapist.getActive()),
				therapist.getCreatedAt(),
				therapist.getUpdatedAt()
		);
	}
}
