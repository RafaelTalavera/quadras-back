package com.axioma.quadras.service;

import com.axioma.quadras.config.PushSubscriptionProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.axioma.quadras.domain.exception.ApplicationException;

@Service
public class PushTokenProtector {

	private static final int IV_LENGTH = 12;
	private static final int GCM_TAG_BITS = 128;
	private final PushSubscriptionProperties properties;
	private final SecureRandom secureRandom = new SecureRandom();

	public PushTokenProtector(PushSubscriptionProperties properties) {
		this.properties = properties;
	}

	public String encrypt(String token) {
		try {
			final byte[] iv = new byte[IV_LENGTH];
			secureRandom.nextBytes(iv);
			final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
			final byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(
					ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array()
			);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Push token encryption failed", exception);
		}
	}

	public String hash(String token) {
		try {
			return java.util.HexFormat.of().formatHex(
					MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))
			);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	public String decrypt(String protectedToken) {
		try {
			final byte[] payload = Base64.getDecoder().decode(protectedToken);
			if (payload.length <= IV_LENGTH) {
				throw new IllegalArgumentException("Protected push token is invalid");
			}
			final ByteBuffer buffer = ByteBuffer.wrap(payload);
			final byte[] iv = new byte[IV_LENGTH];
			buffer.get(iv);
			final byte[] encrypted = new byte[buffer.remaining()];
			buffer.get(encrypted);
			final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException exception) {
			throw new IllegalStateException("Push token decryption failed", exception);
		}
	}

	private SecretKeySpec encryptionKey() throws NoSuchAlgorithmException {
		final String configuredKey = properties.encryptionKey();
		if (configuredKey == null || configuredKey.length() < 32) {
			throw new ApplicationException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Push subscription encryption is not configured"
			);
		}
		final byte[] key = MessageDigest.getInstance("SHA-256")
				.digest(configuredKey.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(key, "AES");
	}
}
