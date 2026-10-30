package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.PushSubscriptionDto;
import com.axioma.quadras.domain.dto.RegisterPushSubscriptionDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.PushSubscription;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.PushSubscriptionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushSubscriptionService {

	private final PushSubscriptionRepository subscriptionRepository;
	private final AppUserRepository userRepository;
	private final PushTokenProtector tokenProtector;
	private final Clock operationalClock;

	public PushSubscriptionService(
			PushSubscriptionRepository subscriptionRepository,
			AppUserRepository userRepository,
			PushTokenProtector tokenProtector,
			@Qualifier("maintenanceOperationalClock") Clock operationalClock
	) {
		this.subscriptionRepository = subscriptionRepository;
		this.userRepository = userRepository;
		this.tokenProtector = tokenProtector;
		this.operationalClock = operationalClock;
	}

	@Transactional
	public PushSubscriptionDto register(String username, RegisterPushSubscriptionDto input) {
		final AppUser user = userRepository.findByUsernameForUpdate(username)
				.orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
		final OffsetDateTime now = nowUtc();
		final String tokenHash = tokenProtector.hash(input.token());
		final String encryptedToken = tokenProtector.encrypt(input.token());
		subscriptionRepository.deactivateConflicts(
				user.getUsername(), input.installationId(), tokenHash, now
		);
		final PushSubscription subscription = subscriptionRepository
				.findByUsernameAndInstallationId(user.getUsername(), input.installationId())
				.orElseGet(() -> PushSubscription.create(
						user.getUsername(),
						input.installationId(),
						input.platform(),
						input.provider(),
						encryptedToken,
						tokenHash,
						now
				));
		if (subscription.getId() != null) {
			subscription.refresh(input.platform(), input.provider(), encryptedToken, tokenHash, now);
		}
		return toDto(subscriptionRepository.save(subscription));
	}

	@Transactional
	public void heartbeat(String username, String installationId) {
		final PushSubscription subscription = ownedActive(username, installationId);
		subscription.heartbeat(nowUtc());
	}

	@Transactional
	public void unregister(String username, String installationId) {
		final PushSubscription subscription = subscriptionRepository
				.findByUsernameAndInstallationId(username, installationId)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Push subscription not found"));
		if (subscription.isActive()) {
			subscription.deactivate(nowUtc());
		}
	}

	private PushSubscription ownedActive(String username, String installationId) {
		return subscriptionRepository.findByUsernameAndInstallationId(username, installationId)
				.filter(PushSubscription::isActive)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Push subscription not found"));
	}

	private OffsetDateTime nowUtc() {
		return OffsetDateTime.ofInstant(operationalClock.instant(), ZoneOffset.UTC);
	}

	private PushSubscriptionDto toDto(PushSubscription subscription) {
		return new PushSubscriptionDto(
				subscription.getInstallationId(),
				subscription.getPlatform(),
				subscription.getProvider(),
				subscription.isActive(),
				subscription.getLastSeenAt()
		);
	}
}
