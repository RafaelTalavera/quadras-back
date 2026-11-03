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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_stock_balances")
public class LaundryStockBalance {

	private static final int QUANTITY_SCALE = 3;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private LaundryStockBucket bucket;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private LaundryProvider provider;

	@Column(name = "provider_scope", nullable = false)
	private long providerScope;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected LaundryStockBalance() {
	}

	public static LaundryStockBalance create(
			LaundryItem item,
			LaundryStockBucket bucket,
			LaundryProvider provider
	) {
		if (item == null) {
			throw new IllegalArgumentException("item is required");
		}
		if (bucket == null) {
			throw new IllegalArgumentException("bucket is required");
		}
		if (bucket.requiresProvider() != (provider != null)) {
			throw new IllegalArgumentException("provider does not match bucket");
		}
		final LaundryStockBalance balance = new LaundryStockBalance();
		balance.item = item;
		balance.bucket = bucket;
		balance.provider = provider;
		balance.providerScope = provider == null ? 0L : provider.getId();
		balance.quantity = zero();
		balance.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		balance.updatedAt = balance.createdAt;
		return balance;
	}

	public void add(BigDecimal delta) {
		final BigDecimal normalized = normalize(delta);
		if (normalized.signum() < 0) {
			throw new IllegalArgumentException("delta cannot be negative");
		}
		quantity = quantity.add(normalized).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public void subtract(BigDecimal delta) {
		final BigDecimal normalized = normalize(delta);
		if (normalized.signum() <= 0) {
			throw new IllegalArgumentException("delta must be positive");
		}
		requireAvailable(normalized);
		quantity = quantity.subtract(normalized).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public void requireAvailable(BigDecimal requested) {
		if (requested == null || requested.signum() <= 0) {
			throw new IllegalArgumentException("requested quantity must be positive");
		}
		if (quantity.compareTo(requested) < 0) {
			throw new IllegalStateException("insufficient stock");
		}
	}

	public boolean isEmpty() {
		return quantity.signum() == 0;
	}

	public static BigDecimal normalize(BigDecimal value) {
		if (value == null) {
			throw new IllegalArgumentException("quantity is required");
		}
		if (value.scale() > QUANTITY_SCALE) {
			throw new IllegalArgumentException("quantity supports at most 3 decimals");
		}
		return value.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
	}

	private static BigDecimal zero() {
		return BigDecimal.ZERO.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
	}

	@PreUpdate
	void touch() {
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public Long getId() {
		return id;
	}

	public LaundryItem getItem() {
		return item;
	}

	public LaundryStockBucket getBucket() {
		return bucket;
	}

	public LaundryProvider getProvider() {
		return provider;
	}

	public long getProviderScope() {
		return providerScope;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public long getVersion() {
		return version;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
