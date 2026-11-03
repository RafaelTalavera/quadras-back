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
@Table(name = "laundry_obligations")
public class LaundryObligation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private LaundryProvider provider;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_document_type", nullable = false, length = 30)
	private LaundryStockDocumentType sourceDocumentType;

	@Column(name = "source_document_id", nullable = false)
	private Long sourceDocumentId;

	@Column(name = "source_line_id", nullable = false)
	private Long sourceLineId;

	@Column(name = "original_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal originalQuantity;

	@Column(name = "open_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal openQuantity;

	@Column(name = "in_rewash_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal inRewashQuantity;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LaundryObligationStatus status;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected LaundryObligation() {
	}

	public static LaundryObligation create(
			LaundryProvider provider,
			LaundryItem item,
			LaundryStockDocumentType sourceDocumentType,
			Long sourceDocumentId,
			Long sourceLineId,
			BigDecimal quantity,
			BigDecimal unitPrice,
			OffsetDateTime effectiveAt
	) {
		if (provider == null || item == null) {
			throw new IllegalArgumentException("provider and item are required");
		}
		if (sourceDocumentType == null || sourceDocumentId == null || sourceLineId == null) {
			throw new IllegalArgumentException("source document is required");
		}
		if (unitPrice == null || unitPrice.signum() < 0) {
			throw new IllegalArgumentException("unitPrice cannot be negative");
		}
		final BigDecimal normalized = LaundryStockBalance.normalize(quantity);
		if (normalized.signum() <= 0) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		final LaundryObligation obligation = new LaundryObligation();
		obligation.provider = provider;
		obligation.item = item;
		obligation.sourceDocumentType = sourceDocumentType;
		obligation.sourceDocumentId = sourceDocumentId;
		obligation.sourceLineId = sourceLineId;
		obligation.originalQuantity = normalized;
		obligation.openQuantity = normalized;
		obligation.inRewashQuantity = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		obligation.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
		obligation.effectiveAt = effectiveAt;
		obligation.status = LaundryObligationStatus.OPEN;
		obligation.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		obligation.updatedAt = obligation.createdAt;
		return obligation;
	}

	public void closeIfSettled() {
		if (openQuantity.signum() == 0) {
			status = LaundryObligationStatus.CLOSED;
			updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
		}
	}

	public void markInRewash(BigDecimal quantity) {
		BigDecimal normalized = LaundryStockBalance.normalize(quantity);
		if (normalized.signum() <= 0 || inRewashQuantity.add(normalized).compareTo(openQuantity) > 0) {
			throw new IllegalArgumentException("Rewash quantity exceeds open obligation quantity");
		}
		inRewashQuantity = inRewashQuantity.add(normalized);
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public Long getId() {
		return id;
	}

	public LaundryProvider getProvider() {
		return provider;
	}

	public LaundryItem getItem() {
		return item;
	}

	public LaundryStockDocumentType getSourceDocumentType() {
		return sourceDocumentType;
	}

	public Long getSourceDocumentId() {
		return sourceDocumentId;
	}

	public Long getSourceLineId() {
		return sourceLineId;
	}

	public BigDecimal getOriginalQuantity() {
		return originalQuantity;
	}

	public BigDecimal getOpenQuantity() {
		return openQuantity;
	}

	public BigDecimal getInRewashQuantity() {
		return inRewashQuantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public OffsetDateTime getEffectiveAt() {
		return effectiveAt;
	}

	public LaundryObligationStatus getStatus() {
		return status;
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
