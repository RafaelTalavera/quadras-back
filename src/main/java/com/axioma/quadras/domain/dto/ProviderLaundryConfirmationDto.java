package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocumentConfirmation;
import com.axioma.quadras.domain.model.LaundryDocumentConfirmationAction;
import java.time.OffsetDateTime;

public record ProviderLaundryConfirmationDto(Long id,
		LaundryDocumentConfirmationAction action, String notes,
		String actorRole, OffsetDateTime createdAt) {
	public static ProviderLaundryConfirmationDto from(LaundryDocumentConfirmation value) {
		return new ProviderLaundryConfirmationDto(value.getId(), value.getActionType(),
				value.getNotes(), value.getActorRole(), value.getCreatedAt());
	}
}
