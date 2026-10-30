package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import com.axioma.quadras.repository.NotificationOutboxDispatchItem;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

class MaintenanceNotificationOutboxDispatcherTest {

	private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-28T15:00:00Z");

	@Test
	void shouldClaimAndDeliverPendingMessage() throws Exception {
		final NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
		final MaintenanceNotificationOutboxWorker worker = mock(MaintenanceNotificationOutboxWorker.class);
		final PushProvider provider = mock(PushProvider.class);
		final NotificationOutboxDispatchItem item = item(1);
		when(repository.findDispatchableIds(any(), any(Pageable.class))).thenReturn(List.of(11L));
		when(worker.claim(11L, NOW)).thenReturn(Optional.of(item));
		final MaintenanceNotificationOutboxDispatcher dispatcher = dispatcher(repository, worker, provider, 8);

		assertThat(dispatcher.dispatch()).isEqualTo(1);

		verify(worker).recoverExpiredClaims(NOW.minusMinutes(2), NOW);
		final ArgumentCaptor<PushMessage> message = ArgumentCaptor.forClass(PushMessage.class);
		verify(provider).deliver(message.capture());
		assertThat(message.getValue().idempotencyKey()).isEqualTo("maintenance:11");
		assertThat(message.getValue().orderId()).isEqualTo(21L);
		verify(worker).markDelivered(11L, NOW);
	}

	@Test
	void shouldScheduleExponentialRetryWithoutPersistingProviderDetails() throws Exception {
		final NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
		final MaintenanceNotificationOutboxWorker worker = mock(MaintenanceNotificationOutboxWorker.class);
		final PushProvider provider = mock(PushProvider.class);
		when(repository.findDispatchableIds(any(), any(Pageable.class))).thenReturn(List.of(11L));
		when(worker.claim(11L, NOW)).thenReturn(Optional.of(item(3)));
		doThrow(new IllegalStateException("provider\nsecret unavailable")).when(provider).deliver(any());
		final MaintenanceNotificationOutboxDispatcher dispatcher = dispatcher(repository, worker, provider, 8);

		assertThat(dispatcher.dispatch()).isZero();

		verify(worker).scheduleRetry(
				11L,
				NOW.plusMinutes(2),
				"IllegalStateException"
		);
		verify(worker, never()).abandon(any(), any(), any());
	}

	@Test
	void shouldAbandonAfterMaximumAttempts() throws Exception {
		final NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
		final MaintenanceNotificationOutboxWorker worker = mock(MaintenanceNotificationOutboxWorker.class);
		final PushProvider provider = mock(PushProvider.class);
		when(repository.findDispatchableIds(any(), any(Pageable.class))).thenReturn(List.of(11L));
		when(worker.claim(11L, NOW)).thenReturn(Optional.of(item(8)));
		doThrow(new IllegalStateException("unavailable")).when(provider).deliver(any());
		final MaintenanceNotificationOutboxDispatcher dispatcher = dispatcher(repository, worker, provider, 8);

		dispatcher.dispatch();

		verify(worker).abandon(11L, NOW, "IllegalStateException");
		verify(worker, never()).scheduleRetry(any(), any(), any());
	}

	private MaintenanceNotificationOutboxDispatcher dispatcher(
			NotificationOutboxRepository repository,
			MaintenanceNotificationOutboxWorker worker,
			PushProvider provider,
			int maxAttempts
	) {
		final Clock clock = Clock.fixed(Instant.parse("2026-10-28T15:00:00Z"), ZoneOffset.UTC);
		return new MaintenanceNotificationOutboxDispatcher(
				repository,
				worker,
				provider,
				properties(maxAttempts),
				clock
		);
	}

	private NotificationOutboxDispatchItem item(int attemptCount) {
		return new NotificationOutboxDispatchItem(
				11L,
				"operator",
				"maintenance:11",
				attemptCount,
				21L,
				"Maintenance overdue",
				"Order 21 is overdue",
				NOW.minusHours(1)
		);
	}

	private MaintenanceAlertProperties properties(int maxAttempts) {
		return new MaintenanceAlertProperties(
				ZoneId.of("America/Sao_Paulo"),
				Duration.ofMinutes(1),
				Duration.ofMinutes(60),
				Duration.ofSeconds(10),
				Duration.ofMinutes(2),
				Duration.ofSeconds(30),
				Duration.ofMinutes(30),
				LocalTime.of(6, 0),
				LocalTime.of(23, 0),
				100,
				50,
				maxAttempts,
				true,
				true,
				false,
				true
		);
	}
}
