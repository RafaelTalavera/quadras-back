package com.axioma.quadras.config;

import com.axioma.quadras.domain.model.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "costanorte.security.demo-user")
public record DemoUserProperties(
		boolean enabled,
		@NotBlank String username,
		@NotBlank String password,
		@NotNull AppUserRole role
) {
}
