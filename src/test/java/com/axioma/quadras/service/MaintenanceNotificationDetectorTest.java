package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class MaintenanceNotificationDetectorTest {

	private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

	@Test
	void shouldDetectBothNotificationTypesInsideOperationalWindow() {
		final MaintenanceOrderRepository orderRepository = mock(MaintenanceOrderRepository.class);
		final MaintenanceNotificationService notificationService = mock(MaintenanceNotificationService.class);
		final ScheduleLockService lockService = mock(ScheduleLockService.class);
		final MaintenanceOrder dueOrder = mock(MaintenanceOrder.class);
		final MaintenanceOrder overdueOrder = mock(MaintenanceOrder.class);
		when(orderRepository.findDueToStartNotificationCandidates(any(), any(), any(Pageable.class)))
				.thenReturn(List.of(dueOrder));
		when(orderRepository.findOverdueNotificationCandidates(any(), any(), any(), any(Pageable.class)))
				.thenReturn(List.of(overdueOrder));
		when(notificationService.createNotifications(
				List.of(dueOrder), MaintenanceNotificationType.DUE_TO_START, java.time.LocalDateTime.of(2026, 10, 28, 9, 0)))
				.thenReturn(1);
		when(notificationService.createNotifications(
				List.of(overdueOrder), MaintenanceNotificationType.OVERDUE, java.time.LocalDateTime.of(2026, 10, 28, 9, 0)))
				.thenReturn(2);
		final MaintenanceNotificationDetector detector = new MaintenanceNotificationDetector(
				orderRepository,
				notificationService,
				lockService,
				properties(true, true),
				Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZONE)
		);

		assertThat(detector.detect()).isEqualTo(3);
		verify(lockService).acquireMaintenanceNotificationDate(any());
	}

	@Test
	void shouldNotQueryOrdersOutsideOperationalWindow() {
		final MaintenanceOrderRepository orderRepository = mock(MaintenanceOrderRepository.class);
		final MaintenanceNotificationDetector detector = new MaintenanceNotificationDetector(
				orderRepository,
				mock(MaintenanceNotificationService.class),
				mock(ScheduleLockService.class),
				properties(true, true),
				Clock.fixed(Instant.parse("2026-10-29T02:00:00Z"), ZONE)
		);

		assertThat(detector.detect()).isZero();
		verify(orderRepository, never()).findDueToStartNotificationCandidates(any(), any(), any());
	}

	@Test
	void shouldRemainInactiveWhenInAppFlagIsDisabled() {
		final MaintenanceOrderRepository orderRepository = mock(MaintenanceOrderRepository.class);
		final MaintenanceNotificationDetector detector = new MaintenanceNotificationDetector(
				orderRepository,
				mock(MaintenanceNotificationService.class),
				mock(ScheduleLockService.class),
				properties(false, true),
				Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZONE)
		);

		assertThat(detector.detect()).isZero();
		verify(orderRepository, never()).findDueToStartNotificationCandidates(any(), any(), any());
	}

	private MaintenanceAlertProperties properties(boolean inApp, boolean push) {
		return new MaintenanceAlertProperties(
				ZONE,
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
				inApp,
				true,
				false,
				push
		);
	}
}
