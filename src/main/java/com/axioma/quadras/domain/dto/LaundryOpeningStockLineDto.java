package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryOpeningStockLine;
import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;

public record LaundryOpeningStockLineDto(
		Long itemId,
		String itemName,
		LaundryUnitType unitType,
		BigDecimal quantity
) {
	public static LaundryOpeningStockLineDto from(LaundryOpeningStockLine line) {
		return new LaundryOpeningStockLineDto(
				line.getItem().getId(),
				line.getItem().getName(),
				line.getItem().getUnitType(),
				line.getQuantity()
		);
	}
}
