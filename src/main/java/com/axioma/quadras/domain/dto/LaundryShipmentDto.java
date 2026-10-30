package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record LaundryShipmentDto(
		Long id, String shipmentNumber, Long providerId, String providerName,
		LaundryShipmentKind kind, LaundryShipmentStatus status, OffsetDateTime sentAt,
		OffsetDateTime receivedAt, String notes, String cancellationNotes, long version,
		BigDecimal totalQuantity, BigDecimal billableAmount, Long originShipmentId,
		List<LaundryShipmentLineDto> lines,
		String createdBy, String updatedBy
) {
	public static LaundryShipmentDto from(LaundryShipment value) {
		List<LaundryShipmentLineDto> lines = value.getLines().stream().map(LaundryShipmentLineDto::from).toList();
		Long originShipmentId = value.getLines().stream()
				.map(LaundryShipmentLine::getOriginShipmentId)
				.filter(java.util.Objects::nonNull)
				.findFirst()
				.orElse(null);
		return new LaundryShipmentDto(
				value.getId(), value.getShipmentNumber(), value.getProvider().getId(), value.getProviderNameSnapshot(),
				value.getKind(), value.getStatus(), value.getSentAt(), value.getReceivedAt(), value.getNotes(),
				value.getCancellationNotes(), value.getVersion(),
				value.getLines().stream().map(LaundryShipmentLine::getSentQuantity).reduce(BigDecimal.ZERO, BigDecimal::add),
				value.getLines().stream().map(LaundryShipmentLine::getBillableAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
				originShipmentId, lines, value.getCreatedBy(), value.getUpdatedBy()
		);
	}
}
