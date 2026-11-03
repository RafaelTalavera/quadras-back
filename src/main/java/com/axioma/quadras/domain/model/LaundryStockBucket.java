package com.axioma.quadras.domain.model;

public enum LaundryStockBucket {
	HOTEL_AVAILABLE,
	HOTEL_REWASH_QUEUE,
	PROVIDER_PROCESSING,
	PROVIDER_REWASH,
	MISSING,
	DAMAGED;

	public boolean requiresProvider() {
		return this != HOTEL_AVAILABLE;
	}
}
