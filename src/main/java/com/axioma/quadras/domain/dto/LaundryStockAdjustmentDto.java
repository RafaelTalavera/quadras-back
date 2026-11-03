package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryStockAdjustment;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockOperation;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LaundryStockAdjustmentDto(
		Long id,
		Long itemId,
		String itemName,
		LaundryStockOperation operation,
		LaundryStockBucket fromBucket,
		LaundryStockBucket toBucket,
		Long providerId,
		BigDecimal quantity,
		String reason,
		OffsetDateTime effectiveAt,
		String actorUsername,
		String actorRole,
		String correlationId,
		OffsetDateTime createdAt
) {
	public static LaundryStockAdjustmentDto from(LaundryStockAdjustment adjustment) {
		return new LaundryStockAdjustmentDto(
				adjustment.getId(),
				adjustment.getItem().getId(),
				adjustment.getItem().getName(),
				adjustment.getOperation(),
				adjustment.getFromBucket(),
				adjustment.getToBucket(),
				adjustment.getProvider() == null ? null : adjustment.getProvider().getId(),
				adjustment.getQuantity(),
				adjustment.getReason(),
				adjustment.getEffectiveAt(),
				adjustment.getActorUsername(),
				adjustment.getActorRole(),
				adjustment.getCorrelationId(),
				adjustment.getCreatedAt()
		);
	}
}
