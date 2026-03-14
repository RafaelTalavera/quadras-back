package com.axioma.quadras.service;

import com.axioma.quadras.config.DemoUserProperties;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.repository.AppUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(
		prefix = "costanorte.security.demo-user",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class DemoUserInitializer implements org.springframework.beans.factory.InitializingBean {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final DemoUserProperties demoUserProperties;
	private final TransactionTemplate transactionTemplate;

	public DemoUserInitializer(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			DemoUserProperties demoUserProperties,
			TransactionTemplate transactionTemplate
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.demoUserProperties = demoUserProperties;
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public void afterPropertiesSet() {
		transactionTemplate.executeWithoutResult(status -> {
			final String username = AppUser.normalizeUsername(demoUserProperties.username());
			final String passwordHash = passwordEncoder.encode(demoUserProperties.password());
			final AppUser demoUser = appUserRepository.findByUsername(username)
					.orElseGet(() -> AppUser.create(
							username,
							passwordHash,
							demoUserProperties.role(),
							true
					));

			demoUser.updateSecurity(passwordHash, demoUserProperties.role(), true);
			appUserRepository.save(demoUser);
		});
	}
}
