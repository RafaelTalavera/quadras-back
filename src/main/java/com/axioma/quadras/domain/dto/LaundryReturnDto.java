package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryReturnManifest;
import com.axioma.quadras.domain.model.LaundryReturnStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record LaundryReturnDto(Long id, String reference, LaundryReturnStatus status,
		Long providerId, String providerName, OffsetDateTime effectiveAt, String notes,
		String createdBy, String submittedBy, OffsetDateTime submittedAt, String confirmedBy,
		OffsetDateTime confirmedAt, String correctionNotes, String correlationId, long version,
		List<LaundryReturnLineDto> lines) {
	public static LaundryReturnDto from(LaundryReturnManifest manifest) {
		return new LaundryReturnDto(manifest.getId(), manifest.getReference(), manifest.getStatus(),
				manifest.getProvider().getId(), manifest.getProvider().getName(), manifest.getEffectiveAt(),
				manifest.getNotes(), manifest.getCreatedBy(), manifest.getSubmittedBy(),
				manifest.getSubmittedAt(), manifest.getConfirmedBy(), manifest.getConfirmedAt(),
				manifest.getCorrectionNotes(), manifest.getCorrelationId(), manifest.getVersion(),
				manifest.getLines().stream().map(LaundryReturnLineDto::from).toList());
	}
}
