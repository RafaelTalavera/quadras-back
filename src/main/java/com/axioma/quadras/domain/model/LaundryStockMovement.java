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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_stock_movements")
public class LaundryStockMovement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private LaundryProvider provider;

	@Column(name = "provider_scope", nullable = false)
	private long providerScope;

	@Enumerated(EnumType.STRING)
	@Column(name = "movement_type", nullable = false, length = 40)
	private LaundryStockMovementType movementType;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_bucket", length = 30)
	private LaundryStockBucket fromBucket;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_bucket", length = 30)
	private LaundryStockBucket toBucket;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "document_type", length = 30)
	private LaundryStockDocumentType documentType;

	@Column(name = "document_id")
	private Long documentId;

	@Column(name = "document_line_id")
	private Long documentLineId;

	@Column(name = "actor_username", nullable = false, length = 120)
	private String actorUsername;

	@Column(name = "actor_role", nullable = false, length = 40)
	private String actorRole;

	@Column(length = 500)
	private String reason;

	@Column(name = "correlation_id", nullable = false, length = 64)
	private String correlationId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected LaundryStockMovement() {
	}

	public static LaundryStockMovement create(
			LaundryItem item,
			LaundryProvider provider,
			long providerScope,
			LaundryStockMovementType movementType,
			LaundryStockBucket fromBucket,
			LaundryStockBucket toBucket,
			BigDecimal quantity,
			OffsetDateTime effectiveAt,
			LaundryStockDocumentType documentType,
			Long documentId,
			Long documentLineId,
			String actorUsername,
			String actorRole,
			String reason,
			String correlationId
	) {
		if (item == null) {
			throw new IllegalArgumentException("item is required");
		}
		if (movementType == null) {
			throw new IllegalArgumentException("movementType is required");
		}
		if (fromBucket == null && toBucket == null) {
			throw new IllegalArgumentException("at least one bucket is required");
		}
		if (fromBucket != null && fromBucket == toBucket) {
			throw new IllegalArgumentException("fromBucket and toBucket must differ");
		}
		final LaundryStockMovement movement = new LaundryStockMovement();
		movement.item = item;
		movement.provider = provider;
		movement.providerScope = providerScope;
		movement.movementType = movementType;
		movement.fromBucket = fromBucket;
		movement.toBucket = toBucket;
		movement.quantity = LaundryStockBalance.normalize(quantity);
		movement.effectiveAt = effectiveAt;
		movement.documentType = documentType;
		movement.documentId = documentId;
		movement.documentLineId = documentLineId;
		movement.actorUsername = actorUsername;
		movement.actorRole = actorRole;
		movement.reason = reason;
		movement.correlationId = correlationId;
		movement.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return movement;
	}

	public Long getId() {
		return id;
	}

	public LaundryItem getItem() {
		return item;
	}

	public LaundryProvider getProvider() {
		return provider;
	}

	public long getProviderScope() {
		return providerScope;
	}

	public LaundryStockMovementType getMovementType() {
		return movementType;
	}

	public LaundryStockBucket getFromBucket() {
		return fromBucket;
	}

	public LaundryStockBucket getToBucket() {
		return toBucket;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public OffsetDateTime getEffectiveAt() {
		return effectiveAt;
	}

	public LaundryStockDocumentType getDocumentType() {
		return documentType;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public Long getDocumentLineId() {
		return documentLineId;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public String getActorRole() {
		return actorRole;
	}

	public String getReason() {
		return reason;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
