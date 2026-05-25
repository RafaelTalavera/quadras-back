package com.axioma.quadras.service;

import java.nio.charset.StandardCharsets;

final class PasswordPolicy {

	private static final int MIN_CODE_POINTS = 12;
	private static final int MAX_UTF8_BYTES = 72;

	private PasswordPolicy() {
	}

	static void validatePlaintext(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("password is required");
		}
		final int codePointCount = password.codePointCount(0, password.length());
		if (codePointCount < MIN_CODE_POINTS) {
			throw new IllegalArgumentException("password must be at least 12 characters long");
		}
		if (password.getBytes(StandardCharsets.UTF_8).length > MAX_UTF8_BYTES) {
			throw new IllegalArgumentException("password must be <= 72 UTF-8 bytes");
		}
	}
}
