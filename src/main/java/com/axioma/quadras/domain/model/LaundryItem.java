package com.axioma.quadras.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_items")
public class LaundryItem {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 120)
	private String name;
	@Enumerated(EnumType.STRING) @Column(name = "unit_type", nullable = false, length = 20)
	private LaundryUnitType unitType;
	@Column(name = "current_unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal currentUnitPrice;
	@Column(nullable = false)
	private boolean active;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected LaundryItem() { }

	public static LaundryItem create(String name, LaundryUnitType unitType, BigDecimal price, boolean active) {
		LaundryItem value = new LaundryItem();
		value.update(name, unitType, price, active);
		return value;
	}

	public void update(String name, LaundryUnitType unitType, BigDecimal price, boolean active) {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
		if (name.trim().length() > 120) throw new IllegalArgumentException("name must be <= 120 chars");
		if (unitType == null) throw new IllegalArgumentException("unitType is required");
		if (price == null || price.signum() < 0) throw new IllegalArgumentException("currentUnitPrice must be >= 0");
		this.name = name.trim();
		this.unitType = unitType;
		this.currentUnitPrice = price.setScale(2, RoundingMode.HALF_UP);
		this.active = active;
	}

	public void retire() {
		this.active = false;
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public LaundryUnitType getUnitType() { return unitType; }
	public BigDecimal getCurrentUnitPrice() { return currentUnitPrice; }
	public boolean isActive() { return active; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	@PrePersist void onCreate() { createdAt = OffsetDateTime.now(ZoneOffset.UTC); updatedAt = createdAt; }
	@PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
}
