package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.repository.MassageProviderListItemView;
import java.time.OffsetDateTime;
import java.util.List;

public record MassageProviderDto(
		Long id,
		String name,
		String specialty,
		String contact,
		String whatsappNumber,
		boolean whatsappEnabled,
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
				provider.getWhatsappNumber(),
				provider.isWhatsappEnabled(),
				provider.isActive(),
				provider.getTherapists().stream().map(MassageTherapistDto::from).toList(),
				provider.getCreatedAt(),
				provider.getUpdatedAt()
		);
	}

	public static MassageProviderDto from(
			MassageProviderListItemView provider,
			List<MassageTherapistDto> therapists
	) {
		return new MassageProviderDto(
				provider.getId(),
				provider.getName(),
				provider.getSpecialty(),
				provider.getContact(),
				provider.getWhatsappNumber(),
				Boolean.TRUE.equals(provider.getWhatsappEnabled()),
				Boolean.TRUE.equals(provider.getActive()),
				therapists == null ? List.of() : List.copyOf(therapists),
				provider.getCreatedAt(),
				provider.getUpdatedAt()
		);
	}
}
