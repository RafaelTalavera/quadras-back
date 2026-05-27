package com.axioma.quadras.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {

	@Test
	void shouldAcceptPasswordWithMinimumLength() {
		assertDoesNotThrow(() -> PasswordPolicy.validatePlaintext("abc123"));
	}

	@Test
	void shouldAcceptPasswordWithMaximumLength() {
		assertDoesNotThrow(() -> PasswordPolicy.validatePlaintext("ab12!@cd34#$xyz"));
	}

	@Test
	void shouldRejectPasswordShorterThanSixCharacters() {
		final IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> PasswordPolicy.validatePlaintext("12345")
		);

		assertEquals("password must be between 6 and 15 characters", exception.getMessage());
	}

	@Test
	void shouldRejectPasswordLongerThanFifteenCharacters() {
		final IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> PasswordPolicy.validatePlaintext("abc123!@#xyz9pqR")
		);

		assertEquals("password must be between 6 and 15 characters", exception.getMessage());
	}

	@Test
	void shouldAcceptLettersNumbersAndSymbols() {
		assertDoesNotThrow(() -> PasswordPolicy.validatePlaintext("Ab1!23"));
	}
}
