package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryProvider;

public record LaundryProviderDto(Long id, String name, String contact, boolean active) {
	public static LaundryProviderDto from(LaundryProvider value) {
		return new LaundryProviderDto(value.getId(), value.getName(), value.getContact(), value.isActive());
	}
}
