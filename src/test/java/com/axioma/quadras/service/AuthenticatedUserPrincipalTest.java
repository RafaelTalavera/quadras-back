package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import org.junit.jupiter.api.Test;

class AuthenticatedUserPrincipalTest {

	@Test
	void shouldKeepSupervisorOperatorInheritanceWithoutGrantingItToProvider() {
		final AuthenticatedUserPrincipal supervisor = new AuthenticatedUserPrincipal(
				AppUser.create("supervisor", "hash", AppUserRole.SUPERVISOR, true));
		final AppUser providerUser = mock(AppUser.class);
		when(providerUser.getRole()).thenReturn(AppUserRole.PROVIDER);
		final AuthenticatedUserPrincipal provider = new AuthenticatedUserPrincipal(providerUser);

		assertThat(supervisor.getAuthorities()).extracting("authority")
				.containsExactlyInAnyOrder("ROLE_SUPERVISOR", "ROLE_OPERATOR");
		assertThat(provider.getAuthorities()).extracting("authority")
				.containsExactly("ROLE_PROVIDER")
				.doesNotContain("ROLE_OPERATOR", "ROLE_SUPERVISOR");
	}
}
