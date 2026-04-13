package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "schedule_locks")
public class ScheduleLock {

	private static final int MAX_SCOPE_KEY_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "lock_type", nullable = false, length = 40)
	private ScheduleLockType lockType;

	@Column(name = "scope_key", nullable = false, length = MAX_SCOPE_KEY_LENGTH)
	private String scopeKey;

	@Column(name = "booking_date", nullable = false)
	private LocalDate bookingDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected ScheduleLock() {
	}

	public static ScheduleLock create(
			ScheduleLockType lockType,
			String scopeKey,
			LocalDate bookingDate
	) {
		final ScheduleLock lock = new ScheduleLock();
		lock.lockType = requireLockType(lockType);
		lock.scopeKey = normalizeScopeKey(scopeKey);
		lock.bookingDate = requireBookingDate(bookingDate);
		return lock;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	private static ScheduleLockType requireLockType(ScheduleLockType value) {
		if (value == null) {
			throw new IllegalArgumentException("lockType is required");
		}
		return value;
	}

	private static String normalizeScopeKey(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("scopeKey is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > MAX_SCOPE_KEY_LENGTH) {
			throw new IllegalArgumentException("scopeKey must be <= " + MAX_SCOPE_KEY_LENGTH + " chars");
		}
		return normalized;
	}

	private static LocalDate requireBookingDate(LocalDate value) {
		if (value == null) {
			throw new IllegalArgumentException("bookingDate is required");
		}
		return value;
	}
}
