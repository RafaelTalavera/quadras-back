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
@Table(name = "laundry_billable_events")
public class LaundryBillableEvent {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "provider_id", nullable = false) private LaundryProvider provider;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id", nullable = false) private LaundryItem item;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "return_manifest_id", nullable = false) private LaundryReturnManifest manifest;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "return_line_id", nullable = false) private LaundryReturnManifestLine line;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "allocation_id", nullable = false, unique = true) private LaundryReturnAllocation allocation;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "obligation_id", nullable = false) private LaundryObligation obligation;
	@Column(nullable = false, precision = 12, scale = 3) private BigDecimal quantity;
	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;
	@Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
	@Column(name = "accepted_at", nullable = false) private OffsetDateTime acceptedAt;
	@Column(name = "accepted_by", nullable = false, length = 120) private String acceptedBy;
	@Enumerated(EnumType.STRING) @Column(name = "export_status", nullable = false, length = 20) private LaundryBillableExportStatus exportStatus;
	@Column(name = "correlation_id", nullable = false, length = 64) private String correlationId;
	@Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

	protected LaundryBillableEvent() { }
	public static LaundryBillableEvent create(LaundryReturnManifest manifest,
			LaundryReturnAllocation allocation, String actor) {
		LaundryBillableEvent value = new LaundryBillableEvent();
		value.provider = manifest.getProvider();
		value.item = allocation.getManifestLine().getItem();
		value.manifest = manifest;
		value.line = allocation.getManifestLine();
		value.allocation = allocation;
		value.obligation = allocation.getObligation();
		value.quantity = allocation.getQuantity();
		value.unitPrice = allocation.getUnitPrice();
		value.amount = value.quantity.multiply(value.unitPrice).setScale(2, RoundingMode.HALF_UP);
		value.acceptedAt = OffsetDateTime.now(ZoneOffset.UTC);
		value.acceptedBy = actor;
		value.exportStatus = LaundryBillableExportStatus.PENDING_EXPORT;
		value.correlationId = manifest.getCorrelationId();
		value.createdAt = value.acceptedAt;
		return value;
	}
	public Long getId() { return id; }
	public LaundryProvider getProvider() { return provider; }
	public LaundryItem getItem() { return item; }
	public LaundryReturnManifest getManifest() { return manifest; }
	public LaundryObligation getObligation() { return obligation; }
	public BigDecimal getQuantity() { return quantity; }
	public BigDecimal getUnitPrice() { return unitPrice; }
	public BigDecimal getAmount() { return amount; }
	public OffsetDateTime getAcceptedAt() { return acceptedAt; }
	public String getAcceptedBy() { return acceptedBy; }
	public LaundryBillableExportStatus getExportStatus() { return exportStatus; }
}
