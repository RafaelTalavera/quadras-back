package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "laundry_return_manifest_lines")
public class LaundryReturnManifestLine {
	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "manifest_id", nullable = false)
	private LaundryReturnManifest manifest;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;
	@Column(name = "declared_clean_quantity", nullable = false, precision = 12, scale = 3) private BigDecimal declaredCleanQuantity;
	@Column(name = "declared_retained_quantity", nullable = false, precision = 12, scale = 3) private BigDecimal declaredRetainedQuantity;
	@Column(name = "accepted_clean_quantity", precision = 12, scale = 3) private BigDecimal acceptedCleanQuantity;
	@Column(name = "returned_dirty_quantity", precision = 12, scale = 3) private BigDecimal returnedDirtyQuantity;
	@Column(name = "retained_quantity", precision = 12, scale = 3) private BigDecimal retainedQuantity;
	@Column(name = "missing_quantity", precision = 12, scale = 3) private BigDecimal missingQuantity;
	@Column(name = "damaged_quantity", precision = 12, scale = 3) private BigDecimal damagedQuantity;
	@Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

	protected LaundryReturnManifestLine() { }
	static LaundryReturnManifestLine create(LaundryReturnManifest manifest, LaundryItem item,
			BigDecimal clean, BigDecimal retained) {
		LaundryReturnManifestLine value = new LaundryReturnManifestLine();
		value.manifest = manifest;
		value.item = item;
		value.declaredCleanQuantity = normalize(clean);
		value.declaredRetainedQuantity = normalize(retained);
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return value;
	}
	public void decide(BigDecimal clean, BigDecimal dirty, BigDecimal retained,
			BigDecimal missing, BigDecimal damaged) {
		acceptedCleanQuantity = normalize(clean);
		returnedDirtyQuantity = normalize(dirty);
		retainedQuantity = normalize(retained);
		missingQuantity = normalize(missing);
		damagedQuantity = normalize(damaged);
	}
	private static BigDecimal normalize(BigDecimal value) {
		return value == null ? ZERO : LaundryStockBalance.normalize(value);
	}
	public Long getId() { return id; }
	public LaundryItem getItem() { return item; }
	public BigDecimal getDeclaredCleanQuantity() { return declaredCleanQuantity; }
	public BigDecimal getDeclaredRetainedQuantity() { return declaredRetainedQuantity; }
	public BigDecimal getAcceptedCleanQuantity() { return acceptedCleanQuantity; }
	public BigDecimal getReturnedDirtyQuantity() { return returnedDirtyQuantity; }
	public BigDecimal getRetainedQuantity() { return retainedQuantity; }
	public BigDecimal getMissingQuantity() { return missingQuantity; }
	public BigDecimal getDamagedQuantity() { return damagedQuantity; }
}
