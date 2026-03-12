package com.axioma.quadras.domain.model;

import java.time.LocalTime;
import java.util.Set;

public final class ReservationRules {

	public static final LocalTime OPENING_TIME = LocalTime.of(7, 0);
	public static final LocalTime CLOSING_TIME = LocalTime.of(23, 0);
	public static final Set<Long> ALLOWED_DURATIONS_MINUTES = Set.of(60L, 90L, 120L);

	private ReservationRules() {
	}

	public static boolean isWithinOperatingHours(LocalTime startTime, LocalTime endTime) {
		return !startTime.isBefore(OPENING_TIME) && !endTime.isAfter(CLOSING_TIME);
	}

	public static boolean isAllowedDuration(long durationMinutes) {
		return ALLOWED_DURATIONS_MINUTES.contains(durationMinutes);
	}
}
