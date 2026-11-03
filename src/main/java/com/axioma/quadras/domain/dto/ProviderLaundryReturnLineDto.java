package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryReturnManifestLine;
import java.math.BigDecimal;

public record ProviderLaundryReturnLineDto(Long id, Long itemId, String itemName,
		BigDecimal declaredClean, BigDecimal declaredRetained,
		BigDecimal acceptedClean, BigDecimal returnedDirty, BigDecimal retained,
		BigDecimal missing, BigDecimal damaged) {
	public static ProviderLaundryReturnLineDto from(LaundryReturnManifestLine line) {
		return new ProviderLaundryReturnLineDto(line.getId(), line.getItem().getId(),
				line.getItem().getName(), line.getDeclaredCleanQuantity(),
				line.getDeclaredRetainedQuantity(), line.getAcceptedCleanQuantity(),
				line.getReturnedDirtyQuantity(), line.getRetainedQuantity(),
				line.getMissingQuantity(), line.getDamagedQuantity());
	}
}
