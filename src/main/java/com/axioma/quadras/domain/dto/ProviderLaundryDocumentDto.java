package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocument;
import com.axioma.quadras.domain.model.LaundryDocumentConfirmation;
import com.axioma.quadras.domain.model.LaundryDocumentStatus;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderLaundryDocumentDto(Long id, String reference,
		LaundryDocumentType documentType, LaundryDocumentStatus status,
		Long providerId, String providerName, OffsetDateTime effectiveAt,
		String notes, OffsetDateTime submittedAt, long version,
		List<ProviderLaundryDocumentLineDto> lines,
		List<ProviderLaundryConfirmationDto> confirmations) {
	public static ProviderLaundryDocumentDto from(LaundryDocument document,
			List<LaundryDocumentConfirmation> confirmations) {
		return new ProviderLaundryDocumentDto(document.getId(), document.getReference(),
				document.getDocumentType(), document.getStatus(), document.getProvider().getId(),
				document.getProvider().getName(), document.getEffectiveAt(), document.getNotes(),
				document.getSubmittedAt(), document.getVersion(), document.getLines().stream()
				.map(ProviderLaundryDocumentLineDto::from).toList(), confirmations.stream()
				.map(ProviderLaundryConfirmationDto::from).toList());
	}
}
