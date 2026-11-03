package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryBillableEvent;
import com.axioma.quadras.domain.model.LaundryBillableExportStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LaundryBillableEventDto(Long id, Long providerId, String providerName, Long itemId,
		String itemName, Long returnId, Long obligationId, BigDecimal quantity,
		BigDecimal unitPrice, BigDecimal amount, OffsetDateTime acceptedAt, String acceptedBy,
		LaundryBillableExportStatus exportStatus) {
	public static LaundryBillableEventDto from(LaundryBillableEvent event) {
		return new LaundryBillableEventDto(event.getId(), event.getProvider().getId(),
				event.getProvider().getName(), event.getItem().getId(), event.getItem().getName(),
				event.getManifest().getId(), event.getObligation().getId(), event.getQuantity(),
				event.getUnitPrice(), event.getAmount(), event.getAcceptedAt(), event.getAcceptedBy(),
				event.getExportStatus());
	}
}
