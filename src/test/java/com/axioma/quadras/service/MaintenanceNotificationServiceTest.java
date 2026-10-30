package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.MaintenanceNotification;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.NotificationOutbox;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.MaintenanceNotificationRepository;
import com.axioma.quadras.repository.NotificationOutboxRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MaintenanceNotificationServiceTest {

	@Test
	void shouldCreateOneOverdueOccurrencePerConfiguredInterval() {
		final AppUserRepository userRepository = mock(AppUserRepository.class);
		final MaintenanceNotificationRepository notificationRepository =
				mock(MaintenanceNotificationRepository.class);
		final NotificationOutboxRepository outboxRepository = mock(NotificationOutboxRepository.class);
		when(userRepository.findAllByEnabledTrueOrderByUsernameAsc()).thenReturn(List.of(
				AppUser.create("operador", "hash", AppUserRole.OPERATOR, true)
		));
		when(notificationRepository.save(any(MaintenanceNotification.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		final MaintenanceOrder order = mock(MaintenanceOrder.class);
		when(order.getId()).thenReturn(45L);
		when(order.getScheduleVersion()).thenReturn(2L);
		when(order.getScheduledEndAt()).thenReturn(LocalDateTime.of(2026, 10, 28, 9, 0));
		when(order.getLocationLabelSnapshot()).thenReturn("Apartamento 102");
		when(order.getTitle()).thenReturn("Revisar elevador");
		final MaintenanceNotificationService service = new MaintenanceNotificationService(
				userRepository,
				notificationRepository,
				outboxRepository,
				properties(),
				mock(MaintenanceNotificationEventPublisher.class)
		);

		assertThat(service.createNotifications(
				List.of(order),
				MaintenanceNotificationType.OVERDUE,
				LocalDateTime.of(2026, 10, 28, 9, 1))).isEqualTo(1);
		assertThat(service.createNotifications(
				List.of(order),
				MaintenanceNotificationType.OVERDUE,
				LocalDateTime.of(2026, 10, 28, 10, 1))).isEqualTo(1);

		final ArgumentCaptor<MaintenanceNotification> notifications =
				ArgumentCaptor.forClass(MaintenanceNotification.class);
		verify(notificationRepository, org.mockito.Mockito.times(2)).save(notifications.capture());
		assertThat(notifications.getAllValues())
				.extracting(MaintenanceNotification::getOccurrenceKey)
				.containsExactly("OVERDUE-0", "OVERDUE-1");
		final ArgumentCaptor<NotificationOutbox> outbox =
				ArgumentCaptor.forClass(NotificationOutbox.class);
		verify(outboxRepository, org.mockito.Mockito.times(2)).save(outbox.capture());
		assertThat(outbox.getAllValues())
				.extracting(NotificationOutbox::getIdempotencyKey)
				.doesNotHaveDuplicates();
	}

	@Test
	void shouldDeduplicateAssignedUserAndCreatePushOutboxInSameFlow() {
		final AppUserRepository userRepository = mock(AppUserRepository.class);
		final MaintenanceNotificationRepository notificationRepository =
				mock(MaintenanceNotificationRepository.class);
		final NotificationOutboxRepository outboxRepository = mock(NotificationOutboxRepository.class);
		final MaintenanceNotificationEventPublisher eventPublisher =
				mock(MaintenanceNotificationEventPublisher.class);
		when(userRepository.findAllByEnabledTrueOrderByUsernameAsc()).thenReturn(List.of(
				AppUser.create("operador", "hash", AppUserRole.OPERATOR, true),
				AppUser.create("supervisor", "hash", AppUserRole.SUPERVISOR, true)
		));
		when(notificationRepository.save(any(MaintenanceNotification.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		final MaintenanceOrder order = mock(MaintenanceOrder.class);
		when(order.getId()).thenReturn(44L);
		when(order.getAssignedUsername()).thenReturn("operador");
		when(order.getScheduleVersion()).thenReturn(3L);
		when(order.getScheduledStartAt()).thenReturn(LocalDateTime.of(2026, 10, 28, 9, 0));
		when(order.getLocationLabelSnapshot()).thenReturn("Apartamento 101");
		when(order.getTitle()).thenReturn("Revisar ar condicionado");
		final MaintenanceNotificationService service = new MaintenanceNotificationService(
				userRepository,
				notificationRepository,
				outboxRepository,
				properties(),
				eventPublisher
		);

		assertThat(service.createNotifications(
				List.of(order),
				MaintenanceNotificationType.DUE_TO_START,
				LocalDateTime.of(2026, 10, 28, 9, 0)))
				.isEqualTo(2);
		final ArgumentCaptor<MaintenanceNotification> notificationCaptor =
				ArgumentCaptor.forClass(MaintenanceNotification.class);
		verify(notificationRepository, org.mockito.Mockito.times(2)).save(notificationCaptor.capture());
		assertThat(notificationCaptor.getAllValues())
				.extracting(MaintenanceNotification::getRecipientUsername)
				.containsExactly("operador", "supervisor");
		final ArgumentCaptor<NotificationOutbox> outboxCaptor =
				ArgumentCaptor.forClass(NotificationOutbox.class);
		verify(outboxRepository, org.mockito.Mockito.times(2)).save(outboxCaptor.capture());
		assertThat(outboxCaptor.getAllValues())
				.extracting(NotificationOutbox::getIdempotencyKey)
				.doesNotHaveDuplicates();
		verify(eventPublisher).publishChanged();
	}

	private MaintenanceAlertProperties properties() {
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
				8,
				true,
				true,
				false,
				true
		);
	}
}
