package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.PushPlatform;
import com.axioma.quadras.domain.model.PushProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPushSubscriptionDto(
		@NotBlank
		@Pattern(regexp = "[A-Za-z0-9._:-]{8,120}")
		String installationId,
		@NotNull PushPlatform platform,
		@NotNull PushProviderType provider,
		@NotBlank @Size(min = 20, max = 4096) String token
) {
}
