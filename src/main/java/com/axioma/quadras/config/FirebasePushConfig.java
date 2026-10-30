package com.axioma.quadras.config;

import com.axioma.quadras.repository.PushSubscriptionRepository;
import com.axioma.quadras.service.FirebaseAccessTokenProvider;
import com.axioma.quadras.service.FirebaseMessagingClient;
import com.axioma.quadras.service.FirebasePushProvider;
import com.axioma.quadras.service.FirebaseServiceAccount;
import com.axioma.quadras.service.PushProvider;
import com.axioma.quadras.service.PushSubscriptionInvalidationService;
import com.axioma.quadras.service.PushTokenProtector;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "costanorte.firebase", name = "enabled", havingValue = "true")
public class FirebasePushConfig {

	@Bean
	HttpClient firebaseHttpClient(FirebasePushProperties properties) {
		return HttpClient.newBuilder()
				.connectTimeout(properties.connectTimeout())
				.build();
	}

	@Bean
	FirebaseServiceAccount firebaseServiceAccount(
			FirebasePushProperties properties,
			ObjectMapper objectMapper
	) {
		return FirebaseServiceAccount.load(properties, objectMapper);
	}

	@Bean
	FirebaseAccessTokenProvider firebaseAccessTokenProvider(
			FirebaseServiceAccount account,
			FirebasePushProperties properties,
			ObjectMapper objectMapper,
			HttpClient firebaseHttpClient,
			@Qualifier("maintenanceOperationalClock") Clock clock
	) {
		return new FirebaseAccessTokenProvider(
				account, properties, objectMapper, firebaseHttpClient, clock
		);
	}

	@Bean
	FirebaseMessagingClient firebaseMessagingClient(
			FirebaseServiceAccount account,
			FirebasePushProperties properties,
			FirebaseAccessTokenProvider accessTokenProvider,
			ObjectMapper objectMapper,
			HttpClient firebaseHttpClient
	) {
		return new FirebaseMessagingClient(
				account, properties, accessTokenProvider, objectMapper, firebaseHttpClient
		);
	}

	@Bean
	PushProvider firebasePushProvider(
			PushSubscriptionRepository subscriptionRepository,
			PushSubscriptionInvalidationService invalidationService,
			PushTokenProtector tokenProtector,
			FirebaseMessagingClient messagingClient
	) {
		return new FirebasePushProvider(
				subscriptionRepository, invalidationService, tokenProtector, messagingClient
		);
	}
}
