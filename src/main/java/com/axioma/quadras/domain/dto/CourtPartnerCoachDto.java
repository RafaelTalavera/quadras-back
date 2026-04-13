package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtPartnerCoach;
import com.axioma.quadras.repository.CourtPartnerCoachListItemView;
import java.time.OffsetDateTime;

public record CourtPartnerCoachDto(
		Long id,
		String name,
		boolean active,
		OffsetDateTime updatedAt,
		String updatedBy
) {
	public static CourtPartnerCoachDto from(CourtPartnerCoach coach) {
		return new CourtPartnerCoachDto(
				coach.getId(),
				coach.getName(),
				coach.isActive(),
				coach.getUpdatedAt(),
				coach.getUpdatedBy()
		);
	}

	public static CourtPartnerCoachDto from(CourtPartnerCoachListItemView coach) {
		return new CourtPartnerCoachDto(
				coach.getId(),
				coach.getName(),
				Boolean.TRUE.equals(coach.getActive()),
				coach.getUpdatedAt(),
				coach.getUpdatedBy()
		);
	}
}
