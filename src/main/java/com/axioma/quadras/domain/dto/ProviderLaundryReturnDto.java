package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryReturnManifest;
import com.axioma.quadras.domain.model.LaundryReturnStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderLaundryReturnDto(Long id, String reference, LaundryReturnStatus status,
		Long providerId, String providerName, OffsetDateTime effectiveAt, String notes,
		OffsetDateTime submittedAt, OffsetDateTime confirmedAt, String correctionNotes,
		long version, List<ProviderLaundryReturnLineDto> lines) {
	public static ProviderLaundryReturnDto from(LaundryReturnManifest manifest) {
		return new ProviderLaundryReturnDto(manifest.getId(), manifest.getReference(),
				manifest.getStatus(), manifest.getProvider().getId(), manifest.getProvider().getName(),
				manifest.getEffectiveAt(), manifest.getNotes(), manifest.getSubmittedAt(),
				manifest.getConfirmedAt(), manifest.getCorrectionNotes(), manifest.getVersion(),
				manifest.getLines().stream().map(ProviderLaundryReturnLineDto::from).toList());
	}
}
