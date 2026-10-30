package com.axioma.quadras.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "laundry_shipment_lines")
public class LaundryShipmentLine {
	private static final BigDecimal ZERO_QUANTITY = new BigDecimal("0.000");

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shipment_id", nullable = false)
	private LaundryShipment shipment;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id", nullable = false)
	private LaundryItem item;
	@Column(name = "item_name_snapshot", nullable = false, length = 120)
	private String itemNameSnapshot;
	@Enumerated(EnumType.STRING) @Column(name = "unit_type_snapshot", nullable = false, length = 20)
	private LaundryUnitType unitTypeSnapshot;
	@Column(name = "sent_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal sentQuantity;
	@Column(name = "accepted_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal acceptedQuantity;
	@Column(name = "rewash_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal rewashQuantity;
	@Column(name = "missing_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal missingQuantity;
	@Column(name = "damaged_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal damagedQuantity;
	@Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPriceSnapshot;
	@Column(name = "billable_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal billableAmount;
	@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "origin_line_id")
	private LaundryShipmentLine originLine;
	@Enumerated(EnumType.STRING) @Column(name = "rewash_reason", length = 30)
	private LaundryRewashReason rewashReason;

	protected LaundryShipmentLine() { }

	public static LaundryShipmentLine standard(LaundryItem item, BigDecimal quantity) {
		return create(item, quantity, item.getCurrentUnitPrice(), null, null);
	}

	public static LaundryShipmentLine rewash(
			LaundryShipmentLine originLine, BigDecimal quantity, LaundryRewashReason reason
	) {
		if (originLine == null) throw new IllegalArgumentException("originLine is required");
		if (reason == null) throw new IllegalArgumentException("rewashReason is required");
		return create(originLine.item, quantity, BigDecimal.ZERO, originLine, reason);
	}

	public static LaundryShipmentLine manualRewash(
			LaundryItem item, BigDecimal quantity, LaundryRewashReason reason
	) {
		return create(item, quantity, BigDecimal.ZERO, null, reason);
	}

	public static LaundryShipmentLine receipt(LaundryItem item, BigDecimal quantity) {
		LaundryShipmentLine value = create(item, quantity, BigDecimal.ZERO, null, null);
		value.acceptedQuantity = value.sentQuantity;
		return value;
	}

	private static LaundryShipmentLine create(
			LaundryItem item, BigDecimal quantity, BigDecimal price,
			LaundryShipmentLine originLine, LaundryRewashReason reason
	) {
		if (item == null) throw new IllegalArgumentException("item is required");
		BigDecimal normalizedQuantity = positive(quantity, "quantity");
		LaundryShipmentLine value = new LaundryShipmentLine();
		value.item = item; value.itemNameSnapshot = item.getName(); value.unitTypeSnapshot = item.getUnitType();
		value.sentQuantity = normalizedQuantity;
		value.acceptedQuantity = ZERO_QUANTITY; value.rewashQuantity = ZERO_QUANTITY;
		value.missingQuantity = ZERO_QUANTITY; value.damagedQuantity = ZERO_QUANTITY;
		value.unitPriceSnapshot = price.setScale(2, RoundingMode.HALF_UP);
		value.billableAmount = value.unitPriceSnapshot.multiply(normalizedQuantity).setScale(2, RoundingMode.HALF_UP);
		value.originLine = originLine; value.rewashReason = reason;
		return value;
	}

	public void classify(BigDecimal accepted, BigDecimal rewash, BigDecimal missing, BigDecimal damaged) {
		accepted = nonNegative(accepted, "acceptedQuantity");
		rewash = nonNegative(rewash, "rewashQuantity");
		missing = nonNegative(missing, "missingQuantity");
		damaged = nonNegative(damaged, "damagedQuantity");
		BigDecimal classified = accepted.add(rewash).add(missing).add(damaged);
		if (classified.signum() == 0) throw new IllegalArgumentException("At least one received quantity is required");
		if (classified.compareTo(getPendingQuantity()) > 0) {
			throw new IllegalStateException("Received classification exceeds pending quantity");
		}
		acceptedQuantity = acceptedQuantity.add(accepted);
		rewashQuantity = rewashQuantity.add(rewash);
		missingQuantity = missingQuantity.add(missing);
		damagedQuantity = damagedQuantity.add(damaged);
	}

	void attachTo(LaundryShipment shipment) { this.shipment = shipment; }
	public BigDecimal getPendingQuantity() {
		return sentQuantity.subtract(acceptedQuantity).subtract(rewashQuantity)
				.subtract(missingQuantity).subtract(damagedQuantity);
	}
	public Long getId() { return id; }
	public LaundryItem getItem() { return item; }
	public String getItemNameSnapshot() { return itemNameSnapshot; }
	public LaundryUnitType getUnitTypeSnapshot() { return unitTypeSnapshot; }
	public BigDecimal getSentQuantity() { return sentQuantity; }
	public BigDecimal getAcceptedQuantity() { return acceptedQuantity; }
	public BigDecimal getRewashQuantity() { return rewashQuantity; }
	public BigDecimal getMissingQuantity() { return missingQuantity; }
	public BigDecimal getDamagedQuantity() { return damagedQuantity; }
	public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
	public BigDecimal getBillableAmount() { return billableAmount; }
	public LaundryShipmentLine getOriginLine() { return originLine; }
	public Long getOriginShipmentId() {
		return originLine == null || originLine.shipment == null ? null : originLine.shipment.getId();
	}
	public LaundryRewashReason getRewashReason() { return rewashReason; }

	private static BigDecimal positive(BigDecimal value, String field) {
		BigDecimal normalized = nonNegative(value, field);
		if (normalized.signum() == 0) throw new IllegalArgumentException(field + " must be greater than zero");
		return normalized;
	}
	private static BigDecimal nonNegative(BigDecimal value, String field) {
		if (value == null || value.signum() < 0) throw new IllegalArgumentException(field + " must be >= 0");
		if (value.scale() > 3) throw new IllegalArgumentException(field + " supports at most 3 decimal places");
		return value.setScale(3, RoundingMode.UNNECESSARY);
	}
}
