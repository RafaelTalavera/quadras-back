package com.axioma.quadras.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "costanorte.firebase")
public record FirebasePushProperties(
		boolean enabled,
		String credentialsPath,
		String projectId,
		@NotNull Duration connectTimeout,
		@NotNull Duration requestTimeout
) {
	public FirebasePushProperties {
		if (connectTimeout != null && connectTimeout.compareTo(Duration.ofSeconds(1)) < 0) {
			throw new IllegalArgumentException("connectTimeout must be >= 1 second");
		}
		if (requestTimeout != null && requestTimeout.compareTo(Duration.ofSeconds(1)) < 0) {
			throw new IllegalArgumentException("requestTimeout must be >= 1 second");
		}
	}
}
