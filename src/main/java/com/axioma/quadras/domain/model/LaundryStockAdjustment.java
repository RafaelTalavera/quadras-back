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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_stock_adjustments")
public class LaundryStockAdjustment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LaundryStockOperation operation;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_bucket", length = 30)
	private LaundryStockBucket fromBucket;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_bucket", length = 30)
	private LaundryStockBucket toBucket;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private LaundryProvider provider;

	@Column(name = "provider_scope", nullable = false)
	private long providerScope;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	@Column(nullable = false, length = 500)
	private String reason;

	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;

	@Column(name = "actor_username", nullable = false, length = 120)
	private String actorUsername;

	@Column(name = "actor_role", nullable = false, length = 40)
	private String actorRole;

	@Column(name = "correlation_id", nullable = false, length = 64)
	private String correlationId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected LaundryStockAdjustment() {
	}

	public static LaundryStockAdjustment create(
			LaundryItem item,
			LaundryStockOperation operation,
			LaundryStockBucket fromBucket,
			LaundryStockBucket toBucket,
			LaundryProvider provider,
			BigDecimal quantity,
			String reason,
			OffsetDateTime effectiveAt,
			String actorUsername,
			String actorRole,
			String correlationId
	) {
		if (item == null) {
			throw new IllegalArgumentException("item is required");
		}
		if (operation == null) {
			throw new IllegalArgumentException("operation is required");
		}
		if (quantity == null || quantity.signum() <= 0) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("reason is required");
		}
		if (effectiveAt == null) {
			throw new IllegalArgumentException("effectiveAt is required");
		}
		final LaundryStockAdjustment adjustment = new LaundryStockAdjustment();
		adjustment.item = item;
		adjustment.operation = operation;
		adjustment.fromBucket = fromBucket;
		adjustment.toBucket = toBucket;
		adjustment.provider = provider;
		adjustment.providerScope = provider == null ? 0L : provider.getId();
		adjustment.quantity = LaundryStockBalance.normalize(quantity).setScale(3, RoundingMode.HALF_UP);
		adjustment.reason = reason;
		adjustment.effectiveAt = effectiveAt;
		adjustment.actorUsername = actorUsername;
		adjustment.actorRole = actorRole;
		adjustment.correlationId = correlationId;
		adjustment.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return adjustment;
	}

	public Long getId() {
		return id;
	}

	public LaundryItem getItem() {
		return item;
	}

	public LaundryStockOperation getOperation() {
		return operation;
	}

	public LaundryStockBucket getFromBucket() {
		return fromBucket;
	}

	public LaundryStockBucket getToBucket() {
		return toBucket;
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

	public String getReason() {
		return reason;
	}

	public OffsetDateTime getEffectiveAt() {
		return effectiveAt;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public String getActorRole() {
		return actorRole;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
