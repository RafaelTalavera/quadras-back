package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocument;
import com.axioma.quadras.domain.model.LaundryDocumentStatus;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import java.time.OffsetDateTime;
import java.util.List;

public record LaundryDocumentDto(Long id, String reference, LaundryDocumentType documentType,
		LaundryDocumentStatus status, Long providerId, String providerName,
		OffsetDateTime effectiveAt, String notes, String createdBy, String submittedBy,
		OffsetDateTime submittedAt, String correlationId, long version,
		List<LaundryDocumentLineDto> lines) {
	public static LaundryDocumentDto from(LaundryDocument document) {
		return new LaundryDocumentDto(document.getId(), document.getReference(),
				document.getDocumentType(), document.getStatus(), document.getProvider().getId(),
				document.getProvider().getName(), document.getEffectiveAt(), document.getNotes(),
				document.getCreatedBy(), document.getSubmittedBy(), document.getSubmittedAt(),
				document.getCorrelationId(), document.getVersion(),
				document.getLines().stream().map(LaundryDocumentLineDto::from).toList());
	}
}
