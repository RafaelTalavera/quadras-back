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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "tour_bookings")
public class TourBooking {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_REFERENCE_LENGTH = 120;
	private static final int MAX_DESCRIPTION_LENGTH = 1000;
	private static final int MAX_PAYMENT_NOTES_LENGTH = 500;
	private static final int MAX_CANCELLATION_NOTES_LENGTH = 500;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "service_type", nullable = false, length = 20)
	private TourServiceType serviceType;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Column(name = "client_name", nullable = false, length = MAX_NAME_LENGTH)
	private String clientName;

	@Column(name = "guest_reference", nullable = false, length = MAX_REFERENCE_LENGTH)
	private String guestReference;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private TourProvider provider;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "commission_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal commissionPercent;

	@Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal commissionAmount;

	@Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
	private String description;

	@Column(name = "paid", nullable = false)
	private boolean paid;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 30)
	private TourPaymentMethod paymentMethod;

	@Column(name = "payment_date")
	private LocalDate paymentDate;

	@Column(name = "payment_notes", length = MAX_PAYMENT_NOTES_LENGTH)
	private String paymentNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private TourBookingStatus status;

	@Column(name = "cancellation_notes", length = MAX_CANCELLATION_NOTES_LENGTH)
	private String cancellationNotes;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "cancelled_at")
	private OffsetDateTime cancelledAt;

	@Column(name = "created_by", length = MAX_USERNAME_LENGTH)
	private String createdBy;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	@Column(name = "cancelled_by", length = MAX_USERNAME_LENGTH)
	private String cancelledBy;

	protected TourBooking() {
	}

	public static TourBooking schedule(
			TourServiceType serviceType,
			LocalDateTime startAt,
			LocalDateTime endAt,
			String clientName,
			String guestReference,
			TourProvider provider,
			BigDecimal amount,
			BigDecimal commissionPercent,
			String description,
			boolean paid,
			TourPaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		final TourBooking booking = new TourBooking();
		booking.applyCoreState(
				serviceType,
				startAt,
				endAt,
				clientName,
				guestReference,
				provider,
				amount,
				commissionPercent,
				description
		);
		booking.status = TourBookingStatus.SCHEDULED;
		booking.createdBy = normalizeActor(actorUsername, "createdBy");
		booking.updatedBy = booking.createdBy;
		booking.applyPaymentStatus(paid, paymentMethod, paymentDate, paymentNotes);
		return booking;
	}

	public void updateBooking(
			TourServiceType serviceType,
			LocalDateTime startAt,
			LocalDateTime endAt,
			String clientName,
			String guestReference,
			TourProvider provider,
			BigDecimal amount,
			BigDecimal commissionPercent,
			String description,
			boolean paid,
			TourPaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		applyCoreState(
				serviceType,
				startAt,
				endAt,
				clientName,
				guestReference,
				provider,
				amount,
				commissionPercent,
				description
		);
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyPaymentStatus(paid, paymentMethod, paymentDate, paymentNotes);
	}

	public void markPayment(
			TourPaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		if (status == TourBookingStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled tour bookings cannot be paid.");
		}
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyPaymentStatus(true, paymentMethod, paymentDate, paymentNotes);
	}

	public void markCancelled(String cancellationNotes, String actorUsername) {
		if (status == TourBookingStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled tour bookings cannot be cancelled again.");
		}
		final String actor = normalizeActor(actorUsername, "cancelledBy");
		this.status = TourBookingStatus.CANCELLED;
		this.cancellationNotes = normalizeOptional(
				cancellationNotes,
				"cancellationNotes",
				MAX_CANCELLATION_NOTES_LENGTH
		);
		this.cancelledAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.cancelledBy = actor;
		this.updatedBy = actor;
	}

	public Long getId() {
		return id;
	}

	public TourServiceType getServiceType() {
		return serviceType;
	}

	public LocalDateTime getStartAt() {
		return startAt;
	}

	public LocalDateTime getEndAt() {
		return endAt;
	}

	public String getClientName() {
		return clientName;
	}

	public String getGuestReference() {
		return guestReference;
	}

	public TourProvider getProvider() {
		return provider;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getCommissionPercent() {
		return commissionPercent;
	}

	public BigDecimal getCommissionAmount() {
		return commissionAmount;
	}

	public String getDescription() {
		return description;
	}

	public boolean isPaid() {
		return paid;
	}

	public TourPaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public String getPaymentNotes() {
		return paymentNotes;
	}

	public TourBookingStatus getStatus() {
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

	private void applyCoreState(
			TourServiceType serviceType,
			LocalDateTime startAt,
			LocalDateTime endAt,
			String clientName,
			String guestReference,
			TourProvider provider,
			BigDecimal amount,
			BigDecimal commissionPercent,
			String description
	) {
		if (serviceType == null) {
			throw new IllegalArgumentException("serviceType is required");
		}
		this.serviceType = serviceType;
		this.startAt = requireDateTime(startAt, "startAt");
		this.endAt = requireDateTime(endAt, "endAt");
		if (!this.startAt.isBefore(this.endAt)) {
			throw new IllegalArgumentException("endAt must be after startAt");
		}
		this.clientName = normalize(clientName, "clientName", MAX_NAME_LENGTH);
		this.guestReference = normalize(guestReference, "guestReference", MAX_REFERENCE_LENGTH);
		if (provider == null) {
			throw new IllegalArgumentException("provider is required");
		}
		this.provider = provider;
		this.amount = requireNonNegativeAmount(amount, "amount");
		this.commissionPercent = requireCommissionPercent(commissionPercent, "commissionPercent");
		this.commissionAmount = calculateCommission(this.amount, this.commissionPercent);
		this.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
	}

	private void applyPaymentStatus(
			boolean paid,
			TourPaymentMethod paymentMethod,
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
		this.paymentNotes = normalizeOptional(paymentNotes, "paymentNotes", MAX_PAYMENT_NOTES_LENGTH);
	}

	private static BigDecimal calculateCommission(BigDecimal amount, BigDecimal percent) {
		return amount.multiply(percent)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}

	private static LocalDateTime requireDateTime(LocalDateTime value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value;
	}

	private static BigDecimal requireNonNegativeAmount(BigDecimal value, String fieldName) {
		if (value == null || value.signum() < 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than or equal to zero");
		}
		return value;
	}

	private static BigDecimal requireCommissionPercent(BigDecimal value, String fieldName) {
		if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
			throw new IllegalArgumentException(fieldName + " must be between 0 and 100");
		}
		return value;
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

	private static String normalizeActor(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		final String normalized = value.trim().toLowerCase();
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException(fieldName + " must be <= " + MAX_USERNAME_LENGTH + " chars");
		}
		return normalized;
	}
}
