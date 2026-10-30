package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axioma.quadras.config.FirebasePushProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FirebaseMessagingClientTest {

	@Test
	@SuppressWarnings("unchecked")
	void shouldSendAuthorizedRequestToProjectEndpoint() throws Exception {
		final HttpClient httpClient = mock(HttpClient.class);
		final FirebaseAccessTokenProvider tokens = mock(FirebaseAccessTokenProvider.class);
		final HttpResponse<String> response = mock(HttpResponse.class);
		when(tokens.accessToken()).thenReturn("oauth-token");
		when(response.statusCode()).thenReturn(200);
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
		final FirebaseMessagingClient client = client(httpClient, tokens);

		client.send("registration-token", message());

		final ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
		verify(httpClient).send(request.capture(), any(HttpResponse.BodyHandler.class));
		assertThat(request.getValue().uri().toString())
				.isEqualTo("https://fcm.googleapis.com/v1/projects/test-project/messages:send");
		assertThat(request.getValue().headers().firstValue("Authorization"))
				.contains("Bearer oauth-token");
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldClassifyUnregisteredTokenAsInvalid() throws Exception {
		final HttpClient httpClient = mock(HttpClient.class);
		final FirebaseAccessTokenProvider tokens = mock(FirebaseAccessTokenProvider.class);
		final HttpResponse<String> response = mock(HttpResponse.class);
		when(tokens.accessToken()).thenReturn("oauth-token");
		when(response.statusCode()).thenReturn(404);
		when(response.body()).thenReturn("{\"error\":{\"status\":\"UNREGISTERED\"}}");
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

		assertThatThrownBy(() -> client(httpClient, tokens).send("expired-token", message()))
				.isInstanceOfSatisfying(
						FirebaseDeliveryException.class,
						exception -> assertThat(exception.isInvalidToken()).isTrue()
				);
	}

	private FirebaseMessagingClient client(
			HttpClient httpClient,
			FirebaseAccessTokenProvider tokens
	) {
		final FirebaseServiceAccount account = new FirebaseServiceAccount(
				"firebase@test-project.iam.gserviceaccount.com",
				"https://oauth2.googleapis.com/token",
				"test-project",
				null
		);
		return new FirebaseMessagingClient(
				account,
				new FirebasePushProperties(false, "", "", Duration.ofSeconds(5), Duration.ofSeconds(10)),
				tokens,
				new ObjectMapper(),
				httpClient
		);
	}

	private PushMessage message() {
		return new PushMessage(
				"operator",
				"maintenance:1:OVERDUE",
				1L,
				"Guest name must not be sent",
				"Room and guest details must not be sent",
				OffsetDateTime.parse("2026-10-28T15:00:00Z")
		);
	}
}
