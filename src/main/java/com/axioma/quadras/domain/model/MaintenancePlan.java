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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "maintenance_plans")
public class MaintenancePlan {

	private static final int MAX_TITLE_LENGTH = 160;
	private static final int MAX_DESCRIPTION_LENGTH = 1500;
	private static final int MAX_USERNAME_LENGTH = 120;
	private static final int MAX_RECURRENCE_INTERVAL = 60;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "location_id", nullable = false)
	private MaintenanceLocation location;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private MaintenanceProvider provider;

	@Column(name = "title", nullable = false, length = MAX_TITLE_LENGTH)
	private String title;

	@Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "recurrence_unit", nullable = false, length = 20)
	private MaintenancePlanRecurrenceUnit recurrenceUnit;

	@Column(name = "recurrence_interval", nullable = false)
	private Integer recurrenceInterval;

	@Column(name = "next_due_date", nullable = false)
	private LocalDate nextDueDate;

	@Column(name = "last_generated_on")
	private LocalDate lastGeneratedOn;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "created_by", length = MAX_USERNAME_LENGTH, updatable = false)
	private String createdBy;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected MaintenancePlan() {
	}

	public static MaintenancePlan create(
			MaintenanceLocation location,
			MaintenanceProvider provider,
			String title,
			String description,
			MaintenancePlanRecurrenceUnit recurrenceUnit,
			Integer recurrenceInterval,
			LocalDate nextDueDate,
			boolean active,
			String actorUsername
	) {
		final MaintenancePlan plan = new MaintenancePlan();
		plan.location = requireLocation(location);
		plan.provider = provider;
		plan.title = normalize(title, "title", MAX_TITLE_LENGTH);
		plan.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		plan.recurrenceUnit = requireRecurrenceUnit(recurrenceUnit);
		plan.recurrenceInterval = normalizeRecurrenceInterval(recurrenceInterval);
		plan.nextDueDate = requireDate(nextDueDate, "nextDueDate");
		plan.active = active;
		plan.createdBy = normalizeActor(actorUsername, "createdBy");
		plan.updatedBy = plan.createdBy;
		return plan;
	}

	public void update(
			MaintenanceLocation location,
			MaintenanceProvider provider,
			String title,
			String description,
			MaintenancePlanRecurrenceUnit recurrenceUnit,
			Integer recurrenceInterval,
			LocalDate nextDueDate,
			boolean active,
			String actorUsername
	) {
		this.location = requireLocation(location);
		this.provider = provider;
		this.title = normalize(title, "title", MAX_TITLE_LENGTH);
		this.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		this.recurrenceUnit = requireRecurrenceUnit(recurrenceUnit);
		this.recurrenceInterval = normalizeRecurrenceInterval(recurrenceInterval);
		this.nextDueDate = requireDate(nextDueDate, "nextDueDate");
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
	}

	public void markGenerated(LocalDate generationDate) {
		this.lastGeneratedOn = requireDate(generationDate, "generationDate");
		this.nextDueDate = advance(generationDate);
	}

	public LocalDate advance(LocalDate anchorDate) {
		final LocalDate baseDate = requireDate(anchorDate, "anchorDate");
		return switch (recurrenceUnit) {
			case MONTHLY -> baseDate.plusMonths(recurrenceInterval.longValue());
			case YEARLY -> baseDate.plusYears(recurrenceInterval.longValue());
		};
	}

	public Long getId() {
		return id;
	}

	public MaintenanceLocation getLocation() {
		return location;
	}

	public MaintenanceProvider getProvider() {
		return provider;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public MaintenancePlanRecurrenceUnit getRecurrenceUnit() {
		return recurrenceUnit;
	}

	public Integer getRecurrenceInterval() {
		return recurrenceInterval;
	}

	public LocalDate getNextDueDate() {
		return nextDueDate;
	}

	public LocalDate getLastGeneratedOn() {
		return lastGeneratedOn;
	}

	public boolean isActive() {
		return active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
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

	private static MaintenanceLocation requireLocation(MaintenanceLocation value) {
		if (value == null) {
			throw new IllegalArgumentException("location is required");
		}
		return value;
	}

	private static MaintenancePlanRecurrenceUnit requireRecurrenceUnit(MaintenancePlanRecurrenceUnit value) {
		if (value == null) {
			throw new IllegalArgumentException("recurrenceUnit is required");
		}
		return value;
	}

	private static Integer normalizeRecurrenceInterval(Integer value) {
		if (value == null) {
			throw new IllegalArgumentException("recurrenceInterval is required");
		}
		if (value < 1 || value > MAX_RECURRENCE_INTERVAL) {
			throw new IllegalArgumentException(
					"recurrenceInterval must be between 1 and " + MAX_RECURRENCE_INTERVAL
			);
		}
		return value;
	}

	private static LocalDate requireDate(LocalDate value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " is required");
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
