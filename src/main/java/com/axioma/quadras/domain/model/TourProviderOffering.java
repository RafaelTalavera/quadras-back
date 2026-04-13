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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "tour_provider_offerings")
public class TourProviderOffering {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_DESCRIPTION_LENGTH = 1000;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private TourProvider provider;

	@Enumerated(EnumType.STRING)
	@Column(name = "service_type", nullable = false, length = 20)
	private TourServiceType serviceType;

	@Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
	private String description;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected TourProviderOffering() {
	}

	public static TourProviderOffering create(
			TourProvider provider,
			TourServiceType serviceType,
			String name,
			BigDecimal amount,
			String description,
			boolean active,
			String actorUsername
	) {
		final TourProviderOffering offering = new TourProviderOffering();
		offering.provider = requireProvider(provider);
		offering.serviceType = requireServiceType(serviceType);
		offering.name = normalize(name, "name", MAX_NAME_LENGTH);
		offering.amount = requireNonNegativeAmount(amount, "amount");
		offering.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		offering.active = active;
		offering.updatedBy = normalizeActor(actorUsername);
		return offering;
	}

	public void update(
			TourServiceType serviceType,
			String name,
			BigDecimal amount,
			String description,
			boolean active,
			String actorUsername
	) {
		this.serviceType = requireServiceType(serviceType);
		this.name = normalize(name, "name", MAX_NAME_LENGTH);
		this.amount = requireNonNegativeAmount(amount, "amount");
		this.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername);
	}

	public Long getId() {
		return id;
	}

	public TourProvider getProvider() {
		return provider;
	}

	public TourServiceType getServiceType() {
		return serviceType;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getDescription() {
		return description;
	}

	public boolean isActive() {
		return active;
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

	private static TourProvider requireProvider(TourProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider is required");
		}
		return provider;
	}

	private static TourServiceType requireServiceType(TourServiceType serviceType) {
		if (serviceType == null) {
			throw new IllegalArgumentException("serviceType is required");
		}
		return serviceType;
	}

	private static BigDecimal requireNonNegativeAmount(BigDecimal value, String fieldName) {
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
