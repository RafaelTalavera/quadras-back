package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryOpeningStock;
import com.axioma.quadras.domain.model.LaundryOpeningStockStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record LaundryOpeningStockDto(
		Long id,
		String reference,
		LaundryOpeningStockStatus status,
		String approvedBy,
		OffsetDateTime approvedAt,
		OffsetDateTime effectiveAt,
		String notes,
		String correlationId,
		List<LaundryOpeningStockLineDto> lines
) {
	public static LaundryOpeningStockDto from(LaundryOpeningStock opening) {
		return new LaundryOpeningStockDto(
				opening.getId(),
				opening.getReference(),
				opening.getStatus(),
				opening.getApprovedBy(),
				opening.getApprovedAt(),
				opening.getEffectiveAt(),
				opening.getNotes(),
				opening.getCorrelationId(),
				opening.getLines().stream().map(LaundryOpeningStockLineDto::from).toList()
		);
	}
}
