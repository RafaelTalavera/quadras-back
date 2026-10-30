package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axioma.quadras.repository.MaintenanceNotificationRepository;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class MaintenanceNotificationLifecycleServiceTest {

	@Test
	void shouldAbandonPendingDeliveriesBeforeResolvingActiveNotifications() {
		final MaintenanceNotificationRepository notificationRepository =
				mock(MaintenanceNotificationRepository.class);
		final NotificationOutboxRepository outboxRepository = mock(NotificationOutboxRepository.class);
		final Clock clock = Clock.fixed(
				Instant.parse("2026-10-28T15:30:00Z"),
				ZoneId.of("America/Sao_Paulo")
		);
		final MaintenanceNotificationEventPublisher eventPublisher =
				mock(MaintenanceNotificationEventPublisher.class);
		when(notificationRepository.resolveActiveByOrderId(eq(42L), org.mockito.ArgumentMatchers.any()))
				.thenReturn(2);
		final MaintenanceNotificationLifecycleService service = new MaintenanceNotificationLifecycleService(
				notificationRepository,
				outboxRepository,
				clock,
				eventPublisher
		);

		assertThat(service.resolveActiveForOrder(42L)).isEqualTo(2);

		final ArgumentCaptor<OffsetDateTime> abandonedAt = ArgumentCaptor.forClass(OffsetDateTime.class);
		final ArgumentCaptor<OffsetDateTime> resolvedAt = ArgumentCaptor.forClass(OffsetDateTime.class);
		final InOrder calls = inOrder(outboxRepository, notificationRepository);
		calls.verify(outboxRepository).abandonPendingForActiveNotificationsByOrderId(
				eq(42L),
				abandonedAt.capture(),
				eq("Maintenance order lifecycle changed")
		);
		calls.verify(notificationRepository).resolveActiveByOrderId(eq(42L), resolvedAt.capture());
		assertThat(abandonedAt.getValue()).isEqualTo(OffsetDateTime.parse("2026-10-28T15:30:00Z"));
		assertThat(resolvedAt.getValue()).isEqualTo(abandonedAt.getValue());
		org.mockito.Mockito.verify(eventPublisher).publishChanged();
	}
}
