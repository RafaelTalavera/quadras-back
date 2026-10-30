package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axioma.quadras.domain.model.PushProviderType;
import com.axioma.quadras.domain.model.PushSubscription;
import com.axioma.quadras.repository.PushSubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FirebasePushProviderTest {

	@Test
	void shouldDeliverToEveryActiveFirebaseSubscription() throws Exception {
		final Fixture fixture = fixture();

		fixture.provider.deliver(message());

		verify(fixture.client).send("token-one", message());
		verify(fixture.client).send("token-two", message());
	}

	@Test
	void shouldInvalidateRejectedTokenAndContinue() throws Exception {
		final Fixture fixture = fixture();
		doThrow(new FirebaseDeliveryException(404, true))
				.when(fixture.client).send("token-one", message());

		fixture.provider.deliver(message());

		verify(fixture.invalidationService).invalidate(1L);
		verify(fixture.client).send("token-two", message());
	}

	@Test
	void shouldPropagateTransientFailureForOutboxRetry() throws Exception {
		final Fixture fixture = fixture();
		doThrow(new FirebaseDeliveryException(503, false))
				.when(fixture.client).send("token-one", message());

		assertThatThrownBy(() -> fixture.provider.deliver(message()))
				.isInstanceOf(FirebaseDeliveryException.class);
	}

	private Fixture fixture() {
		final PushSubscriptionRepository repository = mock(PushSubscriptionRepository.class);
		final PushSubscriptionInvalidationService invalidationService = mock(PushSubscriptionInvalidationService.class);
		final PushTokenProtector protector = mock(PushTokenProtector.class);
		final FirebaseMessagingClient client = mock(FirebaseMessagingClient.class);
		final PushSubscription first = subscription(1L, "encrypted-one");
		final PushSubscription second = subscription(2L, "encrypted-two");
		when(repository.findAllByUsernameAndProviderAndActiveTrueOrderById("operator", PushProviderType.FIREBASE))
				.thenReturn(List.of(first, second));
		when(protector.decrypt("encrypted-one")).thenReturn("token-one");
		when(protector.decrypt("encrypted-two")).thenReturn("token-two");
		return new Fixture(
				new FirebasePushProvider(repository, invalidationService, protector, client),
				invalidationService,
				client
		);
	}

	private PushSubscription subscription(Long id, String encryptedToken) {
		final PushSubscription subscription = mock(PushSubscription.class);
		when(subscription.getId()).thenReturn(id);
		when(subscription.getEncryptedToken()).thenReturn(encryptedToken);
		return subscription;
	}

	private PushMessage message() {
		return new PushMessage(
				"operator",
				"maintenance:1:OVERDUE",
				1L,
				"ignored sensitive title",
				"ignored sensitive body",
				OffsetDateTime.parse("2026-10-28T15:00:00Z")
		);
	}

	private record Fixture(
			FirebasePushProvider provider,
			PushSubscriptionInvalidationService invalidationService,
			FirebaseMessagingClient client
	) {
	}
}
