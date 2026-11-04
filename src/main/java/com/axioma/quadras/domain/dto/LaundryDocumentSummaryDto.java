package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocument;
import com.axioma.quadras.domain.model.LaundryDocumentStatus;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import java.time.OffsetDateTime;

public record LaundryDocumentSummaryDto(Long id, String reference,
		LaundryDocumentType documentType, LaundryDocumentStatus status,
		Long providerId, String providerName, OffsetDateTime effectiveAt,
		OffsetDateTime submittedAt, long version) {
	public static LaundryDocumentSummaryDto from(LaundryDocument document) {
		return new LaundryDocumentSummaryDto(document.getId(), document.getReference(),
				document.getDocumentType(), document.getStatus(), document.getProvider().getId(),
				document.getProvider().getName(), document.getEffectiveAt(),
				document.getSubmittedAt(), document.getVersion());
	}
}
