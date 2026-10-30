package com.axioma.quadras.service;

import com.axioma.quadras.repository.NotificationOutboxDispatchItem;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceNotificationOutboxWorker {

	private final NotificationOutboxRepository outboxRepository;

	public MaintenanceNotificationOutboxWorker(NotificationOutboxRepository outboxRepository) {
		this.outboxRepository = outboxRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int recoverExpiredClaims(OffsetDateTime expiredBefore, OffsetDateTime now) {
		return outboxRepository.recoverExpiredClaims(expiredBefore, now);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<NotificationOutboxDispatchItem> claim(Long outboxId, OffsetDateTime claimedAt) {
		if (outboxRepository.claim(outboxId, claimedAt) == 0) {
			return Optional.empty();
		}
		return outboxRepository.findClaimedItem(outboxId);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markDelivered(Long outboxId, OffsetDateTime deliveredAt) {
		outboxRepository.markDelivered(outboxId, deliveredAt);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void scheduleRetry(Long outboxId, OffsetDateTime nextAttemptAt, String error) {
		outboxRepository.scheduleRetry(outboxId, nextAttemptAt, error);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void abandon(Long outboxId, OffsetDateTime abandonedAt, String error) {
		outboxRepository.abandonClaim(outboxId, abandonedAt, error);
	}
}
