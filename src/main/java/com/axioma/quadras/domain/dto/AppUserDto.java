package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.AppUser;
import java.time.OffsetDateTime;

public record AppUserDto(
		Long id,
		String username,
		String role,
		boolean enabled,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	public static AppUserDto from(AppUser appUser) {
		return new AppUserDto(
				appUser.getId(),
				appUser.getUsername(),
				appUser.getRole().name(),
				appUser.isEnabled(),
				appUser.getCreatedAt(),
				appUser.getUpdatedAt()
		);
	}
}
