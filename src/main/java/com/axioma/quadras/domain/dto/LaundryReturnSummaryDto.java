package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryReturnManifest;
import com.axioma.quadras.domain.model.LaundryReturnStatus;
import java.time.OffsetDateTime;

public record LaundryReturnSummaryDto(Long id, String reference, LaundryReturnStatus status,
		Long providerId, String providerName, OffsetDateTime effectiveAt,
		OffsetDateTime submittedAt, OffsetDateTime confirmedAt, long version) {
	public static LaundryReturnSummaryDto from(LaundryReturnManifest manifest) {
		return new LaundryReturnSummaryDto(manifest.getId(), manifest.getReference(),
				manifest.getStatus(), manifest.getProvider().getId(), manifest.getProvider().getName(),
				manifest.getEffectiveAt(), manifest.getSubmittedAt(), manifest.getConfirmedAt(),
				manifest.getVersion());
	}
}
