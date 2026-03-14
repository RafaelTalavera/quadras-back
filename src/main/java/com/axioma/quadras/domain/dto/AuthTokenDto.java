package com.axioma.quadras.domain.dto;

public record AuthTokenDto(
		String accessToken,
		String tokenType,
		long expiresInSeconds,
		String username,
		String role
) {
}
