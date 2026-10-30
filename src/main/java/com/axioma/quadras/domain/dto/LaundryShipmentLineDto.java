package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.*;
import java.math.BigDecimal;

public record LaundryShipmentLineDto(
		Long id, Long itemId, String itemName, LaundryUnitType unitType,
		BigDecimal sentQuantity, BigDecimal acceptedQuantity, BigDecimal rewashQuantity,
		BigDecimal missingQuantity, BigDecimal damagedQuantity, BigDecimal pendingQuantity,
		BigDecimal unitPrice, BigDecimal billableAmount, Long originLineId, LaundryRewashReason rewashReason
) {
	public static LaundryShipmentLineDto from(LaundryShipmentLine value) {
		return new LaundryShipmentLineDto(
				value.getId(), value.getItem().getId(), value.getItemNameSnapshot(), value.getUnitTypeSnapshot(),
				value.getSentQuantity(), value.getAcceptedQuantity(), value.getRewashQuantity(),
				value.getMissingQuantity(), value.getDamagedQuantity(), value.getPendingQuantity(),
				value.getUnitPriceSnapshot(), value.getBillableAmount(),
				value.getOriginLine() == null ? null : value.getOriginLine().getId(), value.getRewashReason()
		);
	}
}
