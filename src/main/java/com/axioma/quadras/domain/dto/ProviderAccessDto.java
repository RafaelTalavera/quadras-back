package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;

public record ProviderAccessDto(
		ProviderAccessType type,
		Long resourceId,
		String displayName
) {
	public static ProviderAccessDto from(ProviderAccess access) {
		return access == null ? null : new ProviderAccessDto(
				access.type(), access.resourceId(), access.displayName());
	}
}
