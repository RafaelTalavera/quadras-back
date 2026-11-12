package com.axioma.quadras.domain.model;

public record ProviderAccess(
		ProviderAccessType type,
		Long resourceId,
		String displayName
) {
	public ProviderAccess {
		if (type == null) {
			throw new IllegalArgumentException("provider access type is required");
		}
		if (resourceId == null) {
			throw new IllegalArgumentException("provider resource id is required");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("provider display name is required");
		}
		displayName = displayName.trim();
	}
}
