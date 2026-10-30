package com.axioma.quadras.service;

import com.axioma.quadras.config.FirebasePushProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class FirebaseAccessTokenProvider {

	private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
	private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	private final FirebaseServiceAccount account;
	private final FirebasePushProperties properties;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final Clock clock;
	private String cachedToken;
	private Instant refreshAt = Instant.EPOCH;

	public FirebaseAccessTokenProvider(
			FirebaseServiceAccount account,
			FirebasePushProperties properties,
			ObjectMapper objectMapper,
			HttpClient httpClient,
			Clock clock
	) {
		this.account = account;
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
		this.clock = clock;
	}

	public synchronized String accessToken() throws Exception {
		if (cachedToken != null && clock.instant().isBefore(refreshAt)) {
			return cachedToken;
		}
		final String assertion = signedAssertion();
		final String form = "grant_type=" + URLEncoder.encode(GRANT_TYPE, StandardCharsets.UTF_8)
				+ "&assertion=" + URLEncoder.encode(assertion, StandardCharsets.UTF_8);
		final HttpRequest request = HttpRequest.newBuilder(URI.create(account.tokenUri()))
				.timeout(properties.requestTimeout())
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(form))
				.build();
		final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() / 100 != 2) {
			throw new IllegalStateException("Firebase OAuth request failed with status " + response.statusCode());
		}
		final JsonNode json = objectMapper.readTree(response.body());
		cachedToken = json.path("access_token").asText();
		final long expiresIn = json.path("expires_in").asLong(3600);
		if (cachedToken.isBlank()) {
			throw new IllegalStateException("Firebase OAuth response did not contain an access token");
		}
		refreshAt = clock.instant().plusSeconds(Math.max(1, expiresIn - 60));
		return cachedToken;
	}

	private String signedAssertion() throws Exception {
		final long issuedAt = clock.instant().getEpochSecond();
		final String header = objectMapper.writeValueAsString(Map.of("alg", "RS256", "typ", "JWT"));
		final String claims = objectMapper.writeValueAsString(Map.of(
				"iss", account.clientEmail(),
				"scope", SCOPE,
				"aud", account.tokenUri(),
				"iat", issuedAt,
				"exp", issuedAt + 3600
		));
		final String unsigned = base64Url(header) + "." + base64Url(claims);
		return unsigned + "." + sign(unsigned);
	}

	private String sign(String value) throws GeneralSecurityException {
		final Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(account.privateKey());
		signature.update(value.getBytes(StandardCharsets.US_ASCII));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
	}

	private String base64Url(String value) {
		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}
}
