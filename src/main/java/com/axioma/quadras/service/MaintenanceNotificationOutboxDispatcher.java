package com.axioma.quadras.service;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import com.axioma.quadras.repository.NotificationOutboxDispatchItem;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(PushProvider.class)
public class MaintenanceNotificationOutboxDispatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceNotificationOutboxDispatcher.class);
	private static final int MAX_ERROR_LENGTH = 500;

	private final NotificationOutboxRepository outboxRepository;
	private final MaintenanceNotificationOutboxWorker worker;
	private final PushProvider pushProvider;
	private final MaintenanceAlertProperties properties;
	private final Clock operationalClock;

	public MaintenanceNotificationOutboxDispatcher(
			NotificationOutboxRepository outboxRepository,
			MaintenanceNotificationOutboxWorker worker,
			PushProvider pushProvider,
			MaintenanceAlertProperties properties,
			@Qualifier("maintenanceOperationalClock") Clock operationalClock
	) {
		this.outboxRepository = outboxRepository;
		this.worker = worker;
		this.pushProvider = pushProvider;
		this.properties = properties;
		this.operationalClock = operationalClock;
	}

	public int dispatch() {
		final OffsetDateTime now = OffsetDateTime.now(operationalClock);
		worker.recoverExpiredClaims(now.minus(properties.outboxClaimTimeout()), now);
		final List<Long> candidateIds = outboxRepository.findDispatchableIds(
				now,
				PageRequest.of(0, properties.outboxBatchSize())
		);
		int delivered = 0;
		for (Long candidateId : candidateIds) {
			final var claimed = worker.claim(candidateId, now);
			if (claimed.isEmpty()) {
				continue;
			}
			final NotificationOutboxDispatchItem item = claimed.orElseThrow();
			try {
				pushProvider.deliver(toMessage(item));
				worker.markDelivered(item.outboxId(), OffsetDateTime.now(operationalClock));
				delivered++;
			} catch (Exception exception) {
				handleFailure(item, exception, OffsetDateTime.now(operationalClock));
			}
		}
		return delivered;
	}

	private void handleFailure(
			NotificationOutboxDispatchItem item,
			Exception exception,
			OffsetDateTime failedAt
	) {
		final String error = sanitizedError(exception);
		if (item.attemptCount() >= properties.outboxMaxAttempts()) {
			worker.abandon(item.outboxId(), failedAt, error);
			LOGGER.warn("Push delivery abandoned after {} attempts for outbox {}: {}",
					item.attemptCount(), item.outboxId(), error);
			return;
		}
		worker.scheduleRetry(item.outboxId(), failedAt.plus(retryDelay(item.attemptCount())), error);
		LOGGER.warn("Push delivery failed for outbox {}; retry {} scheduled: {}",
				item.outboxId(), item.attemptCount() + 1, error);
	}

	private Duration retryDelay(int attemptCount) {
		Duration delay = properties.outboxRetryInitialDelay();
		for (int attempt = 1; attempt < attemptCount && delay.compareTo(properties.outboxRetryMaxDelay()) < 0; attempt++) {
			delay = delay.multipliedBy(2);
			if (delay.compareTo(properties.outboxRetryMaxDelay()) > 0) {
				return properties.outboxRetryMaxDelay();
			}
		}
		return delay;
	}

	private PushMessage toMessage(NotificationOutboxDispatchItem item) {
		return new PushMessage(
				item.logicalDestination(),
				item.idempotencyKey(),
				item.orderId(),
				item.title(),
				item.body(),
				item.dueAt()
		);
	}

	private String sanitizedError(Exception exception) {
		final String value = exception.getClass().getSimpleName();
		return value.substring(0, Math.min(value.length(), MAX_ERROR_LENGTH));
	}
}
