package com.axioma.quadras.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.axioma.quadras.config.MaintenanceAlertProperties;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class MaintenanceNotificationEventPublisherTest {

	@Test
	void shouldPublishCompatibleMaintenanceInvalidationWhenSseIsEnabled() {
		final ScheduleSyncEventPublisher schedulePublisher = mock(ScheduleSyncEventPublisher.class);
		final MaintenanceNotificationEventPublisher publisher =
				new MaintenanceNotificationEventPublisher(schedulePublisher, properties(true));

		publisher.publishChanged();

		verify(schedulePublisher).publish(
				ScheduleSyncDomain.MAINTENANCE,
				MaintenanceNotificationEventPublisher.NOTIFICATIONS_CHANGED_ACTION,
				null,
				null,
				null
		);
	}

	@Test
	void shouldNotPublishWhenSseIsDisabled() {
		final ScheduleSyncEventPublisher schedulePublisher = mock(ScheduleSyncEventPublisher.class);
		final MaintenanceNotificationEventPublisher publisher =
				new MaintenanceNotificationEventPublisher(schedulePublisher, properties(false));

		publisher.publishChanged();

		verifyNoInteractions(schedulePublisher);
	}

	private MaintenanceAlertProperties properties(boolean sseEnabled) {
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
				sseEnabled,
				false
		);
	}
}
