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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "court_rates")
public class CourtRate {

	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "customer_type", nullable = false, length = 40)
	private CourtCustomerType customerType;

	@Enumerated(EnumType.STRING)
	@Column(name = "pricing_period", nullable = false, length = 20)
	private CourtPricingPeriod pricingPeriod;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected CourtRate() {
	}

	public Long getId() {
		return id;
	}

	public CourtCustomerType getCustomerType() {
		return customerType;
	}

	public CourtPricingPeriod getPricingPeriod() {
		return pricingPeriod;
	}

	public BigDecimal getAmount() {
		return amount;
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

	public void update(BigDecimal amount, boolean active, String actorUsername) {
		if (amount == null || amount.signum() < 0) {
			throw new IllegalArgumentException("amount must be greater than or equal to zero");
		}
		this.amount = amount;
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername);
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
