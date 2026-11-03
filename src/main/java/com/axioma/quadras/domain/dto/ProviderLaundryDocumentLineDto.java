package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryDocumentLine;
import java.math.BigDecimal;

public record ProviderLaundryDocumentLineDto(Long id, Long itemId,
		String itemName, BigDecimal quantity) {
	public static ProviderLaundryDocumentLineDto from(LaundryDocumentLine line) {
		return new ProviderLaundryDocumentLineDto(line.getId(), line.getItem().getId(),
				line.getItem().getName(), line.getQuantity());
	}
}
