package com.axioma.quadras.service;

public class FirebaseDeliveryException extends Exception {

	private final boolean invalidToken;

	public FirebaseDeliveryException(int statusCode, boolean invalidToken) {
		super("Firebase delivery failed with status " + statusCode);
		this.invalidToken = invalidToken;
	}

	public boolean isInvalidToken() {
		return invalidToken;
	}
}
