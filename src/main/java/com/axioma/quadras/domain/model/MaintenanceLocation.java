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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "maintenance_locations")
public class MaintenanceLocation {

	private static final int MAX_CODE_LENGTH = 60;
	private static final int MAX_LABEL_LENGTH = 160;
	private static final int MAX_FLOOR_LENGTH = 40;
	private static final int MAX_DESCRIPTION_LENGTH = 500;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "location_type", nullable = false, length = 20)
	private MaintenanceLocationType locationType;

	@Column(name = "code", nullable = false, length = MAX_CODE_LENGTH)
	private String code;

	@Column(name = "label", nullable = false, length = MAX_LABEL_LENGTH)
	private String label;

	@Column(name = "floor", length = MAX_FLOOR_LENGTH)
	private String floor;

	@Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
	private String description;

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

	protected MaintenanceLocation() {
	}

	public static MaintenanceLocation create(
			MaintenanceLocationType locationType,
			String code,
			String label,
			String floor,
			String description,
			boolean active,
			String actorUsername
	) {
		final MaintenanceLocation location = new MaintenanceLocation();
		location.locationType = requireType(locationType);
		location.code = normalize(code, "code", MAX_CODE_LENGTH);
		location.label = normalize(label, "label", MAX_LABEL_LENGTH);
		location.floor = normalizeOptional(floor, "floor", MAX_FLOOR_LENGTH);
		location.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		location.active = active;
		location.createdBy = normalizeActor(actorUsername, "createdBy");
		location.updatedBy = location.createdBy;
		return location;
	}

	public void update(
			MaintenanceLocationType locationType,
			String code,
			String label,
			String floor,
			String description,
			boolean active,
			String actorUsername
	) {
		this.locationType = requireType(locationType);
		this.code = normalize(code, "code", MAX_CODE_LENGTH);
		this.label = normalize(label, "label", MAX_LABEL_LENGTH);
		this.floor = normalizeOptional(floor, "floor", MAX_FLOOR_LENGTH);
		this.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
	}

	public Long getId() {
		return id;
	}

	public MaintenanceLocationType getLocationType() {
		return locationType;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public String getFloor() {
		return floor;
	}

	public String getDescription() {
		return description;
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

	private static MaintenanceLocationType requireType(MaintenanceLocationType value) {
		if (value == null) {
			throw new IllegalArgumentException("locationType is required");
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
