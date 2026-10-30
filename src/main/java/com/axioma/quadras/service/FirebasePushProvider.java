package com.axioma.quadras.service;

import com.axioma.quadras.domain.model.PushProviderType;
import com.axioma.quadras.repository.PushSubscriptionRepository;

public class FirebasePushProvider implements PushProvider {

	private final PushSubscriptionRepository subscriptionRepository;
	private final PushSubscriptionInvalidationService invalidationService;
	private final PushTokenProtector tokenProtector;
	private final FirebaseMessagingClient messagingClient;

	public FirebasePushProvider(
			PushSubscriptionRepository subscriptionRepository,
			PushSubscriptionInvalidationService invalidationService,
			PushTokenProtector tokenProtector,
			FirebaseMessagingClient messagingClient
	) {
		this.subscriptionRepository = subscriptionRepository;
		this.invalidationService = invalidationService;
		this.tokenProtector = tokenProtector;
		this.messagingClient = messagingClient;
	}

	@Override
	public void deliver(PushMessage message) throws Exception {
		final var subscriptions = subscriptionRepository
				.findAllByUsernameAndProviderAndActiveTrueOrderById(
						message.logicalDestination(), PushProviderType.FIREBASE
				);
		for (var subscription : subscriptions) {
			try {
				messagingClient.send(tokenProtector.decrypt(subscription.getEncryptedToken()), message);
			} catch (FirebaseDeliveryException exception) {
				if (exception.isInvalidToken()) {
					invalidationService.invalidate(subscription.getId());
					continue;
				}
				throw exception;
			}
		}
	}
}
