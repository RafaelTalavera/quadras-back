package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtBookingMaterial;
import com.axioma.quadras.domain.model.CourtMaterialCode;
import com.axioma.quadras.repository.CourtBookingMaterialListItemView;
import java.math.BigDecimal;

public record CourtBookingMaterialDto(
		CourtMaterialCode materialCode,
		String materialLabel,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal totalPrice
) {
	public static CourtBookingMaterialDto from(CourtBookingMaterial item) {
		return new CourtBookingMaterialDto(
				item.getMaterialCode(),
				item.getMaterialLabel(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getTotalPrice()
		);
	}

	public static CourtBookingMaterialDto from(CourtBookingMaterialListItemView item) {
		return new CourtBookingMaterialDto(
				item.getMaterialCode(),
				item.getMaterialLabel(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getTotalPrice()
		);
	}
}
