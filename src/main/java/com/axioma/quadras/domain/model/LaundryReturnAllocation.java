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
@Table(name = "laundry_return_allocations")
public class LaundryReturnAllocation {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "manifest_line_id", nullable = false)
	private LaundryReturnManifestLine manifestLine;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "obligation_id", nullable = false)
	private LaundryObligation obligation;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private LaundryReturnOutcome outcome;
	@Enumerated(EnumType.STRING) @Column(name = "source_bucket", nullable = false, length = 30) private LaundryStockBucket sourceBucket;
	@Column(nullable = false, precision = 12, scale = 3) private BigDecimal quantity;
	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;
	@Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

	protected LaundryReturnAllocation() { }
	public static LaundryReturnAllocation create(LaundryReturnManifestLine line,
			LaundryObligation obligation, LaundryReturnOutcome outcome,
			LaundryStockBucket sourceBucket, BigDecimal quantity) {
		LaundryReturnAllocation value = new LaundryReturnAllocation();
		value.manifestLine = line;
		value.obligation = obligation;
		value.outcome = outcome;
		value.sourceBucket = sourceBucket;
		value.quantity = LaundryStockBalance.normalize(quantity);
		value.unitPrice = obligation.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return value;
	}
	public Long getId() { return id; }
	public LaundryReturnManifestLine getManifestLine() { return manifestLine; }
	public LaundryObligation getObligation() { return obligation; }
	public LaundryReturnOutcome getOutcome() { return outcome; }
	public LaundryStockBucket getSourceBucket() { return sourceBucket; }
	public BigDecimal getQuantity() { return quantity; }
	public BigDecimal getUnitPrice() { return unitPrice; }
}
