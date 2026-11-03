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
@Table(name = "laundry_document_lines")
public class LaundryDocumentLine {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "document_id", nullable = false)
	private LaundryDocument document;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;
	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;
	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected LaundryDocumentLine() { }

	static LaundryDocumentLine create(LaundryDocument document, LaundryItem item,
			BigDecimal quantity, BigDecimal unitPrice) {
		LaundryDocumentLine value = new LaundryDocumentLine();
		value.document = document;
		value.item = item;
		value.quantity = LaundryStockBalance.normalize(quantity);
		value.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return value;
	}

	public Long getId() { return id; }
	public LaundryItem getItem() { return item; }
	public BigDecimal getQuantity() { return quantity; }
	public BigDecimal getUnitPrice() { return unitPrice; }
}
