package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryReturnManifest;
import com.axioma.quadras.domain.model.LaundryReturnStatus;
import java.time.OffsetDateTime;

public record ProviderLaundryReturnSummaryDto(Long id, String reference,
		LaundryReturnStatus status, OffsetDateTime effectiveAt, OffsetDateTime submittedAt,
		OffsetDateTime confirmedAt, long version) {
	public static ProviderLaundryReturnSummaryDto from(LaundryReturnManifest manifest) {
		return new ProviderLaundryReturnSummaryDto(manifest.getId(), manifest.getReference(),
				manifest.getStatus(), manifest.getEffectiveAt(), manifest.getSubmittedAt(),
				manifest.getConfirmedAt(), manifest.getVersion());
	}
}
