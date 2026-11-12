package com.axioma.quadras.domain.dto;

public record CurrentUserDto(
		String username,
		String role,
		ProviderAccessDto providerAccess,
		Long laundryProviderId
) {
}
