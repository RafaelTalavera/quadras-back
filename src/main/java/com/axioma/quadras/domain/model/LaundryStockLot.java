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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_stock_lots")
public class LaundryStockLot {

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

	@Column(name = "remaining_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal remainingQuantity;

	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;

	@Column(name = "source_movement_id")
	private Long sourceMovementId;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected LaundryStockLot() {
	}

	public static LaundryStockLot create(
			LaundryItem item,
			LaundryStockBucket bucket,
			LaundryProvider provider,
			BigDecimal quantity,
			OffsetDateTime effectiveAt,
			Long sourceMovementId
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
		if (effectiveAt == null) {
			throw new IllegalArgumentException("effectiveAt is required");
		}
		final BigDecimal normalized = LaundryStockBalance.normalize(quantity);
		if (normalized.signum() <= 0) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		final LaundryStockLot lot = new LaundryStockLot();
		lot.item = item;
		lot.bucket = bucket;
		lot.provider = provider;
		lot.providerScope = provider == null ? 0L : provider.getId();
		lot.remainingQuantity = normalized;
		lot.effectiveAt = effectiveAt;
		lot.sourceMovementId = sourceMovementId;
		lot.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return lot;
	}

	public void consume(BigDecimal quantity) {
		final BigDecimal normalized = LaundryStockBalance.normalize(quantity);
		if (normalized.signum() <= 0) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		if (remainingQuantity.compareTo(normalized) < 0) {
			throw new IllegalStateException("insufficient lot quantity");
		}
		remainingQuantity = remainingQuantity.subtract(normalized).setScale(3, RoundingMode.HALF_UP);
	}

	public boolean hasRemaining() {
		return remainingQuantity.signum() > 0;
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

	public BigDecimal getRemainingQuantity() {
		return remainingQuantity;
	}

	public OffsetDateTime getEffectiveAt() {
		return effectiveAt;
	}

	public Long getSourceMovementId() {
		return sourceMovementId;
	}

	public long getVersion() {
		return version;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
