package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocumentLine;
import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;

public record LaundryDocumentLineDto(Long id, Long itemId, String itemName,
		LaundryUnitType unitType, BigDecimal quantity, BigDecimal unitPrice) {
	public static LaundryDocumentLineDto from(LaundryDocumentLine line) {
		return new LaundryDocumentLineDto(line.getId(), line.getItem().getId(),
				line.getItem().getName(), line.getItem().getUnitType(), line.getQuantity(), line.getUnitPrice());
	}
}
