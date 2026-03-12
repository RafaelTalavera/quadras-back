package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "reservations")
public class Reservation {

	private static final int MAX_GUEST_NAME_LENGTH = 120;
	private static final int MAX_NOTES_LENGTH = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "guest_name", nullable = false, length = MAX_GUEST_NAME_LENGTH)
	private String guestName;

	@Column(name = "reservation_date", nullable = false)
	private LocalDate reservationDate;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ReservationStatus status;

	@Column(name = "notes", length = MAX_NOTES_LENGTH)
	private String notes;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected Reservation() {
	}

	private Reservation(
			String guestName,
			LocalDate reservationDate,
			LocalTime startTime,
			LocalTime endTime,
			String notes,
			ReservationStatus status
	) {
		this.guestName = normalizeGuestName(guestName);
		this.reservationDate = requireNonNullDate(reservationDate);
		this.startTime = requireNonNullTime(startTime, "startTime");
		this.endTime = requireNonNullTime(endTime, "endTime");
		validateTimeWindow(this.startTime, this.endTime);
		this.notes = normalizeNotes(notes);
		this.status = requireStatus(status);
	}

	public static Reservation schedule(
			String guestName,
			LocalDate reservationDate,
			LocalTime startTime,
			LocalTime endTime,
			String notes
	) {
		return new Reservation(
				guestName,
				reservationDate,
				startTime,
				endTime,
				notes,
				ReservationStatus.SCHEDULED
		);
	}

	public Long getId() {
		return id;
	}

	public String getGuestName() {
		return guestName;
	}

	public LocalDate getReservationDate() {
		return reservationDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public long durationInMinutes() {
		return Duration.between(startTime, endTime).toMinutes();
	}

	public void markCancelled() {
		this.status = ReservationStatus.CANCELLED;
	}

	public void markCompleted() {
		this.status = ReservationStatus.COMPLETED;
	}

	@PrePersist
	void onCreate() {
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	private static String normalizeGuestName(String guestName) {
		if (guestName == null || guestName.isBlank()) {
			throw new IllegalArgumentException("guestName is required");
		}
		final String normalized = guestName.trim();
		if (normalized.length() > MAX_GUEST_NAME_LENGTH) {
			throw new IllegalArgumentException(
					"guestName must be <= " + MAX_GUEST_NAME_LENGTH + " chars"
			);
		}
		return normalized;
	}

	private static LocalDate requireNonNullDate(LocalDate reservationDate) {
		if (reservationDate == null) {
			throw new IllegalArgumentException("reservationDate is required");
		}
		return reservationDate;
	}

	private static LocalTime requireNonNullTime(LocalTime value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value;
	}

	private static void validateTimeWindow(LocalTime start, LocalTime end) {
		if (!start.isBefore(end)) {
			throw new IllegalArgumentException("startTime must be before endTime");
		}
	}

	private static ReservationStatus requireStatus(ReservationStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("status is required");
		}
		return status;
	}

	private static String normalizeNotes(String notes) {
		if (notes == null || notes.isBlank()) {
			return null;
		}
		final String normalized = notes.trim();
		if (normalized.length() > MAX_NOTES_LENGTH) {
			throw new IllegalArgumentException(
					"notes must be <= " + MAX_NOTES_LENGTH + " chars"
			);
		}
		return normalized;
	}
}
