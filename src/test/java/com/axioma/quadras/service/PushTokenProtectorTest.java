package com.axioma.quadras.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.axioma.quadras.config.PushSubscriptionProperties;
import org.junit.jupiter.api.Test;

class PushTokenProtectorTest {

	@Test
	void shouldEncryptDecryptAndHashWithoutExposingToken() {
		final PushTokenProtector protector = new PushTokenProtector(
				new PushSubscriptionProperties("costanorte-test-push-encryption-key-2026")
		);
		final String token = "fcm-registration-token-value-000000000001";

		final String encrypted = protector.encrypt(token);

		assertThat(encrypted).doesNotContain(token);
		assertThat(protector.decrypt(encrypted)).isEqualTo(token);
		assertThat(protector.hash(token)).hasSize(64);
		assertThat(protector.encrypt(token)).isNotEqualTo(encrypted);
	}
}
