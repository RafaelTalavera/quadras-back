package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocument;
import com.axioma.quadras.domain.model.LaundryDocumentStatus;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import java.time.OffsetDateTime;

public record ProviderLaundryDocumentSummaryDto(Long id, String reference,
		LaundryDocumentType documentType, LaundryDocumentStatus status,
		OffsetDateTime effectiveAt, OffsetDateTime submittedAt, long version) {
	public static ProviderLaundryDocumentSummaryDto from(LaundryDocument document) {
		return new ProviderLaundryDocumentSummaryDto(document.getId(), document.getReference(),
				document.getDocumentType(), document.getStatus(), document.getEffectiveAt(),
				document.getSubmittedAt(), document.getVersion());
	}
}
