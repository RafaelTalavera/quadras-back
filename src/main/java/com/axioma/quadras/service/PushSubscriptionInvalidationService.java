package com.axioma.quadras.service;

import com.axioma.quadras.repository.PushSubscriptionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushSubscriptionInvalidationService {

	private final PushSubscriptionRepository repository;
	private final Clock operationalClock;

	public PushSubscriptionInvalidationService(
			PushSubscriptionRepository repository,
			@Qualifier("maintenanceOperationalClock") Clock operationalClock
	) {
		this.repository = repository;
		this.operationalClock = operationalClock;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void invalidate(Long subscriptionId) {
		repository.deactivateInvalidToken(
				subscriptionId,
				OffsetDateTime.ofInstant(operationalClock.instant(), ZoneOffset.UTC)
		);
	}
}
