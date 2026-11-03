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

@Entity
@Table(name = "laundry_opening_stock_lines")
public class LaundryOpeningStockLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "opening_stock_id", nullable = false)
	private LaundryOpeningStock openingStock;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	protected LaundryOpeningStockLine() {
	}

	public static LaundryOpeningStockLine create(
			LaundryOpeningStock openingStock,
			LaundryItem item,
			BigDecimal quantity
	) {
		if (openingStock == null || item == null) {
			throw new IllegalArgumentException("openingStock and item are required");
		}
		final BigDecimal normalized = LaundryStockBalance.normalize(quantity);
		if (normalized.signum() < 0) {
			throw new IllegalArgumentException("quantity cannot be negative");
		}
		final LaundryOpeningStockLine line = new LaundryOpeningStockLine();
		line.openingStock = openingStock;
		line.item = item;
		line.quantity = normalized.setScale(3, RoundingMode.HALF_UP);
		return line;
	}

	public Long getId() {
		return id;
	}

	public LaundryOpeningStock getOpeningStock() {
		return openingStock;
	}

	public LaundryItem getItem() {
		return item;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}
}
