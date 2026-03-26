package com.axioma.quadras.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "court_bookings")
public class CourtBooking {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_REFERENCE_LENGTH = 120;
	private static final int MAX_PAYMENT_NOTES_LENGTH = 500;
	private static final int MAX_CANCELLATION_NOTES_LENGTH = 500;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "booking_date", nullable = false)
	private LocalDate bookingDate;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes;

	@Column(name = "customer_name", nullable = false, length = MAX_NAME_LENGTH)
	private String customerName;

	@Column(name = "customer_reference", nullable = false, length = MAX_REFERENCE_LENGTH)
	private String customerReference;

	@Enumerated(EnumType.STRING)
	@Column(name = "customer_type", nullable = false, length = 40)
	private CourtCustomerType customerType;

	@Enumerated(EnumType.STRING)
	@Column(name = "pricing_period", nullable = false, length = 20)
	private CourtPricingPeriod pricingPeriod;

	@Column(name = "sunrise_estimate", nullable = false)
	private LocalTime sunriseEstimate;

	@Column(name = "sunset_estimate", nullable = false)
	private LocalTime sunsetEstimate;

	@Column(name = "court_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal courtAmount;

	@Column(name = "materials_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal materialsAmount;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "paid", nullable = false)
	private boolean paid;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 30)
	private CourtPaymentMethod paymentMethod;

	@Column(name = "payment_date")
	private LocalDate paymentDate;

	@Column(name = "payment_notes", length = MAX_PAYMENT_NOTES_LENGTH)
	private String paymentNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private CourtBookingStatus status;

	@Column(name = "cancellation_notes", length = MAX_CANCELLATION_NOTES_LENGTH)
	private String cancellationNotes;

	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private final List<CourtBookingMaterial> materials = new ArrayList<>();

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

	protected CourtBooking() {
	}

	public static CourtBooking schedule(
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			String customerName,
			String customerReference,
			CourtCustomerType customerType,
			CourtPricingPeriod pricingPeriod,
			LocalTime sunriseEstimate,
			LocalTime sunsetEstimate,
			BigDecimal courtAmount,
			BigDecimal materialsAmount,
			BigDecimal totalAmount,
			boolean paid,
			CourtPaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			List<CourtBookingMaterial> materials,
			String actorUsername
	) {
		final CourtBooking booking = new CourtBooking();
		booking.applyCoreState(
				bookingDate,
				startTime,
				endTime,
				customerName,
				customerReference,
				customerType,
				pricingPeriod,
				sunriseEstimate,
				sunsetEstimate,
				courtAmount,
				materialsAmount,
				totalAmount,
				materials
		);
		booking.status = CourtBookingStatus.SCHEDULED;
		booking.createdBy = normalizeActor(actorUsername, "createdBy");
		booking.updatedBy = booking.createdBy;
		booking.applyPaymentStatus(paid, paymentMethod, paymentDate, paymentNotes);
		return booking;
	}

	public void updateBooking(
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			String customerName,
			String customerReference,
			CourtCustomerType customerType,
			CourtPricingPeriod pricingPeriod,
			LocalTime sunriseEstimate,
			LocalTime sunsetEstimate,
			BigDecimal courtAmount,
			BigDecimal materialsAmount,
			BigDecimal totalAmount,
			boolean paid,
			CourtPaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			List<CourtBookingMaterial> materials,
			String actorUsername
	) {
		applyCoreState(
				bookingDate,
				startTime,
				endTime,
				customerName,
				customerReference,
				customerType,
				pricingPeriod,
				sunriseEstimate,
				sunsetEstimate,
				courtAmount,
				materialsAmount,
				totalAmount,
				materials
		);
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyPaymentStatus(paid, paymentMethod, paymentDate, paymentNotes);
	}

	public void markPayment(
			CourtPaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyPaymentStatus(true, paymentMethod, paymentDate, paymentNotes);
	}

	public void markCancelled(String cancellationNotes, String actorUsername) {
		if (status == CourtBookingStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled court bookings cannot be cancelled again.");
		}
		final String actor = normalizeActor(actorUsername, "cancelledBy");
		this.status = CourtBookingStatus.CANCELLED;
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

	public LocalDate getBookingDate() {
		return bookingDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getCustomerReference() {
		return customerReference;
	}

	public CourtCustomerType getCustomerType() {
		return customerType;
	}

	public CourtPricingPeriod getPricingPeriod() {
		return pricingPeriod;
	}

	public LocalTime getSunriseEstimate() {
		return sunriseEstimate;
	}

	public LocalTime getSunsetEstimate() {
		return sunsetEstimate;
	}

	public BigDecimal getCourtAmount() {
		return courtAmount;
	}

	public BigDecimal getMaterialsAmount() {
		return materialsAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public boolean isPaid() {
		return paid;
	}

	public CourtPaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public String getPaymentNotes() {
		return paymentNotes;
	}

	public CourtBookingStatus getStatus() {
		return status;
	}

	public String getCancellationNotes() {
		return cancellationNotes;
	}

	public List<CourtBookingMaterial> getMaterials() {
		return List.copyOf(materials);
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
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			String customerName,
			String customerReference,
			CourtCustomerType customerType,
			CourtPricingPeriod pricingPeriod,
			LocalTime sunriseEstimate,
			LocalTime sunsetEstimate,
			BigDecimal courtAmount,
			BigDecimal materialsAmount,
			BigDecimal totalAmount,
			List<CourtBookingMaterial> materials
	) {
		this.bookingDate = requireDate(bookingDate, "bookingDate");
		this.startTime = requireTime(startTime, "startTime");
		this.endTime = requireTime(endTime, "endTime");
		if (!this.startTime.isBefore(this.endTime)) {
			throw new IllegalArgumentException("startTime must be before endTime");
		}
		this.durationMinutes = (int) Duration.between(this.startTime, this.endTime).toMinutes();
		this.customerName = normalize(customerName, "customerName", MAX_NAME_LENGTH);
		this.customerReference = normalize(customerReference, "customerReference", MAX_REFERENCE_LENGTH);
		if (customerType == null) {
			throw new IllegalArgumentException("customerType is required");
		}
		if (pricingPeriod == null) {
			throw new IllegalArgumentException("pricingPeriod is required");
		}
		this.customerType = customerType;
		this.pricingPeriod = pricingPeriod;
		this.sunriseEstimate = requireTime(sunriseEstimate, "sunriseEstimate");
		this.sunsetEstimate = requireTime(sunsetEstimate, "sunsetEstimate");
		this.courtAmount = requireAmount(courtAmount, "courtAmount");
		this.materialsAmount = requireAmount(materialsAmount, "materialsAmount");
		this.totalAmount = requireAmount(totalAmount, "totalAmount");
		replaceMaterials(materials);
	}

	private void replaceMaterials(List<CourtBookingMaterial> items) {
		this.materials.clear();
		if (items == null) {
			return;
		}
		for (final CourtBookingMaterial item : items) {
			item.attachTo(this);
			this.materials.add(item);
		}
	}

	private void applyPaymentStatus(
			boolean paid,
			CourtPaymentMethod paymentMethod,
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

	private static LocalDate requireDate(LocalDate value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value;
	}

	private static LocalTime requireTime(LocalTime value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value;
	}

	private static BigDecimal requireAmount(BigDecimal value, String fieldName) {
		if (value == null || value.signum() < 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than or equal to zero");
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
