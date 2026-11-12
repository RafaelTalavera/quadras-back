package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.CourtPartnerCoach;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ProviderAccessGuardTest {

	private CurrentActorService currentActorService;
	private CourtPartnerCoachRepository courtPartnerCoachRepository;
	private ProviderAccessGuard guard;

	@BeforeEach
	void setUp() {
		currentActorService = mock(CurrentActorService.class);
		courtPartnerCoachRepository = mock(CourtPartnerCoachRepository.class);
		guard = new ProviderAccessGuard(
				currentActorService,
				courtPartnerCoachRepository,
				mock(MassageTherapistRepository.class),
				mock(MaintenanceProviderRepository.class),
				mock(LaundryProviderRepository.class)
		);
	}

	@Test
	void shouldAllowActiveProviderOfExpectedTypeAndOwner() {
		final ProviderAccess access = new ProviderAccess(ProviderAccessType.COURTS, 17L, "Joao");
		final AuthenticatedUserPrincipal principal = principal(access);
		final CourtPartnerCoach coach = mock(CourtPartnerCoach.class);
		when(coach.isActive()).thenReturn(true);
		when(courtPartnerCoachRepository.findById(17L)).thenReturn(Optional.of(coach));

		assertThat(guard.requireOwnership(ProviderAccessType.COURTS, 17L)).isEqualTo(access);
	}

	@Test
	void shouldRejectDifferentProviderTypeBeforeOwnershipCheck() {
		principal(new ProviderAccess(ProviderAccessType.COURTS, 17L, "Joao"));

		assertForbidden(() -> guard.requireType(ProviderAccessType.MASSAGE));
	}

	@Test
	void shouldRejectResourceOwnedByAnotherProvider() {
		principal(new ProviderAccess(ProviderAccessType.COURTS, 17L, "Joao"));
		final CourtPartnerCoach coach = mock(CourtPartnerCoach.class);
		when(coach.isActive()).thenReturn(true);
		when(courtPartnerCoachRepository.findById(17L)).thenReturn(Optional.of(coach));

		assertForbidden(() -> guard.requireOwnership(ProviderAccessType.COURTS, 18L));
	}

	@Test
	void shouldRejectProviderWhenBusinessResourceWasDeactivated() {
		principal(new ProviderAccess(ProviderAccessType.COURTS, 17L, "Joao"));
		final CourtPartnerCoach coach = mock(CourtPartnerCoach.class);
		when(coach.isActive()).thenReturn(false);
		when(courtPartnerCoachRepository.findById(17L)).thenReturn(Optional.of(coach));

		assertForbidden(() -> guard.requireType(ProviderAccessType.COURTS));
	}

	private AuthenticatedUserPrincipal principal(ProviderAccess access) {
		final AuthenticatedUserPrincipal principal = mock(AuthenticatedUserPrincipal.class);
		when(principal.getRole()).thenReturn(AppUserRole.PROVIDER);
		when(principal.getProviderAccess()).thenReturn(access);
		when(principal.getProviderType()).thenReturn(access.type());
		when(currentActorService.requirePrincipal()).thenReturn(principal);
		return principal;
	}

	private void assertForbidden(Runnable operation) {
		assertThatThrownBy(operation::run)
				.isInstanceOfSatisfying(ApplicationException.class,
						ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
	}
}
