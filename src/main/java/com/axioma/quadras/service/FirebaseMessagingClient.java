package com.axioma.quadras.service;

import com.axioma.quadras.config.FirebasePushProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

public class FirebaseMessagingClient {

	private final FirebaseServiceAccount account;
	private final FirebasePushProperties properties;
	private final FirebaseAccessTokenProvider accessTokenProvider;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	public FirebaseMessagingClient(
			FirebaseServiceAccount account,
			FirebasePushProperties properties,
			FirebaseAccessTokenProvider accessTokenProvider,
			ObjectMapper objectMapper,
			HttpClient httpClient
	) {
		this.account = account;
		this.properties = properties;
		this.accessTokenProvider = accessTokenProvider;
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
	}

	public void send(String token, PushMessage message) throws Exception {
		final String collapseId = collapseId(message.idempotencyKey());
		final Map<String, Object> payload = Map.of(
				"message", Map.of(
						"token", token,
						"notification", Map.of(
								"title", "Manutencao pendente",
								"body", "Abra a aplicacao para consultar a ordem."
						),
						"data", Map.of(
								"module", "maintenance",
								"orderId", String.valueOf(message.orderId()),
								"dueAt", message.dueAt().toString(),
								"idempotencyKey", message.idempotencyKey()
						),
						"android", Map.of("collapse_key", collapseId),
						"apns", Map.of("headers", Map.of("apns-collapse-id", collapseId)),
						"webpush", Map.of("headers", Map.of("Topic", collapseId))
				)
		);
		final HttpRequest request = HttpRequest.newBuilder(URI.create(
				"https://fcm.googleapis.com/v1/projects/" + account.projectId() + "/messages:send"
		))
				.timeout(properties.requestTimeout())
				.header("Authorization", "Bearer " + accessTokenProvider.accessToken())
				.header("Content-Type", "application/json; charset=UTF-8")
				.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
				.build();
		final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() / 100 == 2) {
			return;
		}
		final String status = errorStatus(response.body());
		final boolean invalidToken = response.statusCode() == 404 && "UNREGISTERED".equals(status)
				|| response.statusCode() == 400 && "INVALID_ARGUMENT".equals(status);
		throw new FirebaseDeliveryException(response.statusCode(), invalidToken);
	}

	private String errorStatus(String body) {
		try {
			final JsonNode root = objectMapper.readTree(body);
			return root.path("error").path("status").asText();
		} catch (Exception ignored) {
			return "";
		}
	}

	private String collapseId(String idempotencyKey) throws Exception {
		final byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(idempotencyKey.getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(digest).substring(0, 32);
	}
}
