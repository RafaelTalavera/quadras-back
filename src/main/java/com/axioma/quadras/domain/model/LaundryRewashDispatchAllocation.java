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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_rewash_dispatch_allocations")
public class LaundryRewashDispatchAllocation {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_line_id", nullable = false)
	private LaundryDocumentLine documentLine;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "obligation_id", nullable = false)
	private LaundryObligation obligation;
	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected LaundryRewashDispatchAllocation() { }

	public static LaundryRewashDispatchAllocation create(LaundryDocumentLine line,
			LaundryObligation obligation, BigDecimal quantity) {
		LaundryRewashDispatchAllocation value = new LaundryRewashDispatchAllocation();
		value.documentLine = line;
		value.obligation = obligation;
		value.quantity = LaundryStockBalance.normalize(quantity);
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return value;
	}

	public Long getId() { return id; }
	public LaundryObligation getObligation() { return obligation; }
	public BigDecimal getQuantity() { return quantity; }
}
