package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "massage_bookings")
public class MassageBooking {

	private static final int MAX_CLIENT_NAME_LENGTH = 120;
	private static final int MAX_GUEST_REFERENCE_LENGTH = 120;
	private static final int MAX_TREATMENT_LENGTH = 120;
	private static final int MAX_PAYMENT_NOTES_LENGTH = 500;
	private static final int MAX_CANCELLATION_NOTES_LENGTH = 500;
	private static final int MAX_USERNAME_LENGTH = 80;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "booking_date", nullable = false)
	private LocalDate bookingDate;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "client_name", nullable = false, length = MAX_CLIENT_NAME_LENGTH)
	private String clientName;

	@Column(name = "guest_reference", nullable = false, length = MAX_GUEST_REFERENCE_LENGTH)
	private String guestReference;

	@Column(name = "treatment", nullable = false, length = MAX_TREATMENT_LENGTH)
	private String treatment;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private MassageProvider provider;

	@Column(name = "paid", nullable = false)
	private boolean paid;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 20)
	private MassagePaymentMethod paymentMethod;

	@Column(name = "payment_date")
	private LocalDate paymentDate;

	@Column(name = "payment_notes", length = MAX_PAYMENT_NOTES_LENGTH)
	private String paymentNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private MassageBookingStatus status;

	@Column(name = "cancellation_notes", length = MAX_CANCELLATION_NOTES_LENGTH)
	private String cancellationNotes;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "cancelled_at")
	private OffsetDateTime cancelledAt;

	@Column(name = "created_by", nullable = false, updatable = false, length = MAX_USERNAME_LENGTH)
	private String createdBy;

	@Column(name = "updated_by", nullable = false, length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	@Column(name = "cancelled_by", length = MAX_USERNAME_LENGTH)
	private String cancelledBy;

	protected MassageBooking() {
	}

	private MassageBooking(
			LocalDate bookingDate,
			LocalTime startTime,
			String clientName,
			String guestReference,
			String treatment,
			BigDecimal amount,
			MassageProvider provider,
			boolean paid,
			MassagePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		this.bookingDate = requireDate(bookingDate);
		this.startTime = requireTime(startTime);
		this.clientName = normalize(clientName, "clientName", MAX_CLIENT_NAME_LENGTH);
		this.guestReference = normalize(
				guestReference,
				"guestReference",
				MAX_GUEST_REFERENCE_LENGTH
		);
		this.treatment = normalize(treatment, "treatment", MAX_TREATMENT_LENGTH);
		this.amount = requireAmount(amount);
		this.provider = requireProvider(provider);
		this.status = MassageBookingStatus.SCHEDULED;
		this.createdBy = normalizeUsername(actorUsername, "createdBy");
		this.updatedBy = this.createdBy;
		applyPaymentStatus(paid, paymentMethod, paymentDate, paymentNotes);
	}

	public static MassageBooking schedule(
			LocalDate bookingDate,
			LocalTime startTime,
			String clientName,
			String guestReference,
			String treatment,
			BigDecimal amount,
			MassageProvider provider,
			boolean paid,
			MassagePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		return new MassageBooking(
				bookingDate,
				startTime,
				clientName,
				guestReference,
				treatment,
				amount,
				provider,
				paid,
				paymentMethod,
				paymentDate,
				paymentNotes,
				actorUsername
		);
	}

	public Long getId() {
		return id;
	}

	public LocalDate getBookingDate() {
		return bookingDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public String getClientName() {
		return clientName;
	}

	public String getGuestReference() {
		return guestReference;
	}

	public String getTreatment() {
		return treatment;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public MassageProvider getProvider() {
		return provider;
	}

	public boolean isPaid() {
		return paid;
	}

	public MassagePaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public String getPaymentNotes() {
		return paymentNotes;
	}

	public MassageBookingStatus getStatus() {
		return status;
	}

	public String getCancellationNotes() {
		return cancellationNotes;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public OffsetDateTime getCancelledAt() {
		return cancelledAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public String getCancelledBy() {
		return cancelledBy;
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

	public void updateBooking(
			LocalDate bookingDate,
			LocalTime startTime,
			String clientName,
			String guestReference,
			String treatment,
			BigDecimal amount,
			MassageProvider provider,
			boolean paid,
			MassagePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		this.bookingDate = requireDate(bookingDate);
		this.startTime = requireTime(startTime);
		this.clientName = normalize(clientName, "clientName", MAX_CLIENT_NAME_LENGTH);
		this.guestReference = normalize(
				guestReference,
				"guestReference",
				MAX_GUEST_REFERENCE_LENGTH
		);
		this.treatment = normalize(treatment, "treatment", MAX_TREATMENT_LENGTH);
		this.amount = requireAmount(amount);
		this.provider = requireProvider(provider);
		this.updatedBy = normalizeUsername(actorUsername, "updatedBy");
		applyPaymentStatus(paid, paymentMethod, paymentDate, paymentNotes);
	}

	public void markPayment(
			MassagePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		this.updatedBy = normalizeUsername(actorUsername, "updatedBy");
		applyPaymentStatus(true, paymentMethod, paymentDate, paymentNotes);
	}

	public void markCancelled(String cancellationNotes, String actorUsername) {
		if (status == MassageBookingStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled massage bookings cannot be cancelled again.");
		}
		final String normalizedActor = normalizeUsername(actorUsername, "cancelledBy");
		this.status = MassageBookingStatus.CANCELLED;
		this.cancellationNotes = normalize(
				cancellationNotes,
				"cancellationNotes",
				MAX_CANCELLATION_NOTES_LENGTH
		);
		this.cancelledAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.cancelledBy = normalizedActor;
		this.updatedBy = normalizedActor;
	}

	private static String normalize(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " must be <= " + maxLength + " chars");
		}
		return normalized;
	}

	private static String normalizeOptional(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " must be <= " + maxLength + " chars");
		}
		return normalized;
	}

	private static String normalizeUsername(String value, String fieldName) {
		return normalize(value, fieldName, MAX_USERNAME_LENGTH).toLowerCase();
	}

	private static LocalDate requireDate(LocalDate value) {
		if (value == null) {
			throw new IllegalArgumentException("bookingDate is required");
		}
		return value;
	}

	private static LocalTime requireTime(LocalTime value) {
		if (value == null) {
			throw new IllegalArgumentException("startTime is required");
		}
		return value;
	}

	private static BigDecimal requireAmount(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			throw new IllegalArgumentException("amount must be greater than zero");
		}
		return amount;
	}

	private static MassageProvider requireProvider(MassageProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider is required");
		}
		return provider;
	}

	private void applyPaymentStatus(
			boolean paid,
			MassagePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes
	) {
		if (!paid) {
			if (paymentMethod != null || paymentDate != null || (paymentNotes != null && !paymentNotes.isBlank())) {
				throw new IllegalArgumentException(
						"paymentMethod, paymentDate and paymentNotes require paid=true"
				);
			}
			this.paid = false;
			this.paymentMethod = null;
			this.paymentDate = null;
			this.paymentNotes = null;
			return;
		}

		if (paymentMethod == null) {
			throw new IllegalArgumentException("paymentMethod is required when paid is true");
		}
		if (paymentDate == null) {
			throw new IllegalArgumentException("paymentDate is required when paid is true");
		}

		this.paid = true;
		this.paymentMethod = paymentMethod;
		this.paymentDate = paymentDate;
		this.paymentNotes = normalizeOptional(
				paymentNotes,
				"paymentNotes",
				MAX_PAYMENT_NOTES_LENGTH
		);
	}
}
