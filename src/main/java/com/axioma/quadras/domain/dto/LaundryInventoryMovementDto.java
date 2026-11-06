package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryStockDocumentType;
import com.axioma.quadras.domain.model.LaundryStockMovement;
import com.axioma.quadras.domain.model.LaundryStockMovementType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LaundryInventoryMovementDto(
		Long id,
		OffsetDateTime effectiveAt,
		Long itemId,
		String itemName,
		BigDecimal quantity,
		String movementType,
		String notes,
		OffsetDateTime createdAt,
		String createdBy
) {
	public static LaundryInventoryMovementDto from(LaundryStockMovement movement) {
		return new LaundryInventoryMovementDto(
				movement.getId(),
				movement.getEffectiveAt(),
				movement.getItem().getId(),
				movement.getItem().getName(),
				movement.getQuantity(),
				apiType(movement),
				movement.getReason(),
				movement.getCreatedAt(),
				movement.getActorUsername()
		);
	}

	private static String apiType(LaundryStockMovement movement) {
		if (movement.getDocumentType() == LaundryStockDocumentType.ITEM_RETIREMENT) {
			return "ITEM_RETIREMENT";
		}
		if (movement.getMovementType() == LaundryStockMovementType.OPENING) {
			return "OPENING_STOCK";
		}
		if (movement.getFromBucket() != null && movement.getToBucket() == null) {
			return "WITHDRAWAL";
		}
		return "ADJUSTMENT";
	}
}
