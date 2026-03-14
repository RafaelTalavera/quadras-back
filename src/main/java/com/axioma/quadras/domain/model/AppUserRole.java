package com.axioma.quadras.domain.model;

import java.util.Locale;

public enum AppUserRole {
	OPERATOR;

	public String authority() {
		return "ROLE_" + name();
	}

	public static AppUserRole fromClaim(String claimValue) {
		if (claimValue == null || claimValue.isBlank()) {
			throw new IllegalArgumentException("JWT role claim is required.");
		}
		return AppUserRole.valueOf(claimValue.trim().toUpperCase(Locale.ROOT));
	}
}
