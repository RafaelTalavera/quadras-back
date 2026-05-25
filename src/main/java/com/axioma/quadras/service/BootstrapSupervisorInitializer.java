package com.axioma.quadras.service;

import com.axioma.quadras.config.BootstrapSupervisorProperties;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.repository.AppUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@ConditionalOnProperty(
		prefix = "costanorte.security.bootstrap-supervisor",
		name = "enabled",
		havingValue = "true"
)
public class BootstrapSupervisorInitializer implements org.springframework.beans.factory.InitializingBean {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final BootstrapSupervisorProperties bootstrapSupervisorProperties;
	private final TransactionTemplate transactionTemplate;

	public BootstrapSupervisorInitializer(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			BootstrapSupervisorProperties bootstrapSupervisorProperties,
			TransactionTemplate transactionTemplate
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.bootstrapSupervisorProperties = bootstrapSupervisorProperties;
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public void afterPropertiesSet() {
		transactionTemplate.executeWithoutResult(status -> {
			final String username = AppUser.normalizeUsername(bootstrapSupervisorProperties.username());
			final AppUser supervisor = appUserRepository.findByUsername(username)
					.orElseGet(() -> AppUser.create(
							username,
							passwordEncoder.encode(bootstrapSupervisorProperties.password()),
							AppUserRole.SUPERVISOR,
							true
					));

			supervisor.updateRoleAndStatus(AppUserRole.SUPERVISOR, true);
			if (!passwordEncoder.matches(
					bootstrapSupervisorProperties.password(),
					supervisor.getPasswordHash()
			)) {
				supervisor.changePasswordHash(
						passwordEncoder.encode(bootstrapSupervisorProperties.password())
				);
			}
			appUserRepository.save(supervisor);
		});
	}
}
