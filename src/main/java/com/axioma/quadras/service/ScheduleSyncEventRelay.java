package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.ScheduleSyncEventDto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleSyncEventRelay {

	private final ScheduleSyncService scheduleSyncService;

	public ScheduleSyncEventRelay(ScheduleSyncService scheduleSyncService) {
		this.scheduleSyncService = scheduleSyncService;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onScheduleSyncChange(ScheduleSyncChangeRequestedEvent event) {
		scheduleSyncService.broadcast(new ScheduleSyncEventDto(
				event.domain().apiValue(),
				event.action(),
				event.entityId(),
				event.dateFrom(),
				event.dateTo(),
				event.occurredAt()
		));
	}
}
