package com.axioma.quadras.service;

public enum ScheduleSyncDomain {
	RESERVATIONS("reservations"),
	MASSAGES("massages"),
	COURTS("courts"),
	TOURS("tours"),
	MAINTENANCE("maintenance");

	private final String apiValue;

	ScheduleSyncDomain(String apiValue) {
		this.apiValue = apiValue;
	}

	public String apiValue() {
		return apiValue;
	}
}
