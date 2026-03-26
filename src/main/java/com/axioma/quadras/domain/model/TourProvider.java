package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "tour_providers")
public class TourProvider {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_CONTACT_LENGTH = 160;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(name = "contact", nullable = false, length = MAX_CONTACT_LENGTH)
	private String contact;

	@Column(name = "default_commission_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal defaultCommissionPercent;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected TourProvider() {
	}

	public static TourProvider create(
			String name,
			String contact,
			BigDecimal defaultCommissionPercent,
			String actorUsername
	) {
		final TourProvider provider = new TourProvider();
		provider.name = normalize(name, "name", MAX_NAME_LENGTH);
		provider.contact = normalize(contact, "contact", MAX_CONTACT_LENGTH);
		provider.defaultCommissionPercent = requireCommissionPercent(defaultCommissionPercent, "defaultCommissionPercent");
		provider.active = true;
		provider.updatedBy = normalizeActor(actorUsername);
		return provider;
	}

	public void update(
			String name,
			String contact,
			BigDecimal defaultCommissionPercent,
			boolean active,
			String actorUsername
	) {
		this.name = normalize(name, "name", MAX_NAME_LENGTH);
		this.contact = normalize(contact, "contact", MAX_CONTACT_LENGTH);
		this.defaultCommissionPercent = requireCommissionPercent(defaultCommissionPercent, "defaultCommissionPercent");
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getContact() {
		return contact;
	}

	public BigDecimal getDefaultCommissionPercent() {
		return defaultCommissionPercent;
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

	private static String normalizeActor(String actorUsername) {
		if (actorUsername == null || actorUsername.isBlank()) {
			return null;
		}
		final String normalized = actorUsername.trim().toLowerCase();
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException("updatedBy must be <= " + MAX_USERNAME_LENGTH + " chars");
		}
		return normalized;
	}
}
