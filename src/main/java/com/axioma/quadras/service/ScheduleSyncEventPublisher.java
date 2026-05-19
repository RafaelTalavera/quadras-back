package com.axioma.quadras.service;

import java.time.Instant;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ScheduleSyncEventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	public ScheduleSyncEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publish(
			ScheduleSyncDomain domain,
			String action,
			Long entityId,
			LocalDate dateFrom,
			LocalDate dateTo
	) {
		applicationEventPublisher.publishEvent(new ScheduleSyncChangeRequestedEvent(
				domain,
				action,
				entityId,
				dateFrom,
				dateTo,
				Instant.now()
		));
	}
}
