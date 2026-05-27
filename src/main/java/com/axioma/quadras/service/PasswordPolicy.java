package com.axioma.quadras.service;

final class PasswordPolicy {

	private static final int MIN_LENGTH = 6;
	private static final int MAX_LENGTH = 15;

	private PasswordPolicy() {
	}

	static void validatePlaintext(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("password is required");
		}
		if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("password must be between 6 and 15 characters");
		}
	}
}
