package com.axioma.quadras.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "costanorte.security.bootstrap-supervisor")
public record BootstrapSupervisorProperties(
		boolean enabled,
		@NotBlank String username,
		@NotBlank String password
) {
}
