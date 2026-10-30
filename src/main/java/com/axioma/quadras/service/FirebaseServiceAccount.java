package com.axioma.quadras.service;

import com.axioma.quadras.config.FirebasePushProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public record FirebaseServiceAccount(
		String clientEmail,
		String tokenUri,
		String projectId,
		PrivateKey privateKey
) {
	public static FirebaseServiceAccount load(FirebasePushProperties properties, ObjectMapper objectMapper) {
		if (properties.credentialsPath() == null || properties.credentialsPath().isBlank()) {
			throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS is required when Firebase is enabled");
		}
		try {
			final JsonNode json = objectMapper.readTree(Files.readString(Path.of(properties.credentialsPath())));
			final String configuredProject = properties.projectId();
			final String projectId = configuredProject == null || configuredProject.isBlank()
					? required(json, "project_id")
					: configuredProject.trim();
			return new FirebaseServiceAccount(
					required(json, "client_email"),
					required(json, "token_uri"),
					projectId,
					parsePrivateKey(required(json, "private_key"))
			);
		} catch (IOException | GeneralSecurityException exception) {
			throw new IllegalStateException("Firebase service account credentials are invalid", exception);
		}
	}

	private static PrivateKey parsePrivateKey(String pem) throws GeneralSecurityException {
		final String normalized = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		return KeyFactory.getInstance("RSA").generatePrivate(
				new PKCS8EncodedKeySpec(Base64.getDecoder().decode(normalized))
		);
	}

	private static String required(JsonNode json, String field) {
		final String value = json.path(field).asText();
		if (value.isBlank()) {
			throw new IllegalStateException("Firebase credential field is missing: " + field);
		}
		return value;
	}
}
