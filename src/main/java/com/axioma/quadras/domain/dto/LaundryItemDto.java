package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;

public record LaundryItemDto(Long id, String name, LaundryUnitType unitType, BigDecimal currentUnitPrice, boolean active) {
	public static LaundryItemDto from(LaundryItem value) {
		return new LaundryItemDto(value.getId(), value.getName(), value.getUnitType(), value.getCurrentUnitPrice(), value.isActive());
	}
}
