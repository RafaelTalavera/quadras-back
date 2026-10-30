package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.PushPlatform;
import com.axioma.quadras.domain.model.PushProviderType;
import java.time.OffsetDateTime;

public record PushSubscriptionDto(
		String installationId,
		PushPlatform platform,
		PushProviderType provider,
		boolean active,
		OffsetDateTime lastSeenAt
) {
}
