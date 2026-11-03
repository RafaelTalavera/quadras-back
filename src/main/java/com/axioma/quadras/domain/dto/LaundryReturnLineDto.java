package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryReturnManifestLine;
import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;

public record LaundryReturnLineDto(Long id, Long itemId, String itemName, LaundryUnitType unitType,
		BigDecimal declaredClean, BigDecimal declaredRetained, BigDecimal acceptedClean,
		BigDecimal returnedDirty, BigDecimal retained, BigDecimal missing, BigDecimal damaged) {
	public static LaundryReturnLineDto from(LaundryReturnManifestLine line) {
		return new LaundryReturnLineDto(line.getId(), line.getItem().getId(), line.getItem().getName(),
				line.getItem().getUnitType(), line.getDeclaredCleanQuantity(),
				line.getDeclaredRetainedQuantity(), line.getAcceptedCleanQuantity(),
				line.getReturnedDirtyQuantity(), line.getRetainedQuantity(), line.getMissingQuantity(),
				line.getDamagedQuantity());
	}
}
