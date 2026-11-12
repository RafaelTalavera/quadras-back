package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axioma.quadras.domain.model.AuditEvent;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditTrailServiceTest {

	@Test
	void shouldPersistProviderActorContextWithExternalAction() {
		final AuditEventRepository repository = mock(AuditEventRepository.class);
		final CurrentActorService currentActorService = mock(CurrentActorService.class);
		when(currentActorService.currentUsername()).thenReturn("profesor.joao");
		when(currentActorService.currentRole()).thenReturn("PROVIDER");
		when(currentActorService.currentProviderAccess()).thenReturn(
				new ProviderAccess(ProviderAccessType.COURTS, 17L, "Joao Silva"));
		final AuditTrailService service = new AuditTrailService(
				repository, currentActorService, new ObjectMapper());

		service.record("courts", "court-booking", 99L, "CONFIRMED",
				"Reserva confirmada por proveedor", null, null, null);

		final ArgumentCaptor<AuditEvent> event = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(event.capture());
		assertThat(event.getValue().getActorUsername()).isEqualTo("profesor.joao");
		assertThat(event.getValue().getActorRole()).isEqualTo("PROVIDER");
		assertThat(event.getValue().getActorProviderType()).isEqualTo(ProviderAccessType.COURTS);
		assertThat(event.getValue().getActorProviderResourceId()).isEqualTo(17L);
		assertThat(event.getValue().getActionName()).isEqualTo("CONFIRMED");
	}
}
