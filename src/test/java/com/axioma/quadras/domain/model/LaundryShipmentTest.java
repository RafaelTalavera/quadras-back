package com.axioma.quadras.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LaundryShipmentTest {
	private static final OffsetDateTime SENT_AT = OffsetDateTime.of(2026, 10, 30, 12, 0, 0, 0, ZoneOffset.UTC);

	@Test
	void shouldReconcilePartialAndCompleteReceipts() {
		LaundryShipmentLine line = LaundryShipmentLine.standard(item(), new BigDecimal("10"));
		LaundryShipment shipment = shipment(line);

		line.classify(new BigDecimal("4"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		shipment.refreshAfterReceipt(SENT_AT.plusDays(1), "operador.demo");

		assertEquals(LaundryShipmentStatus.PARTIALLY_RECEIVED, shipment.getStatus());
		assertEquals(new BigDecimal("6.000"), line.getPendingQuantity());

		line.classify(new BigDecimal("4"), new BigDecimal("1"), new BigDecimal("1"), BigDecimal.ZERO);
		shipment.refreshAfterReceipt(SENT_AT.plusDays(2), "operador.demo");

		assertEquals(LaundryShipmentStatus.RECEIVED, shipment.getStatus());
		assertEquals(0, line.getPendingQuantity().signum());
	}

	@Test
	void shouldRejectClassificationAbovePendingQuantity() {
		LaundryShipmentLine line = LaundryShipmentLine.standard(item(), new BigDecimal("5"));

		IllegalStateException error = assertThrows(
				IllegalStateException.class,
				() -> line.classify(new BigDecimal("6"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
		);

		assertEquals("Received classification exceeds pending quantity", error.getMessage());
	}

	@Test
	void shouldRejectNegativeClassification() {
		LaundryShipmentLine line = LaundryShipmentLine.standard(item(), new BigDecimal("5"));

		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> line.classify(new BigDecimal("-1"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
		);

		assertEquals("acceptedQuantity must be >= 0", error.getMessage());
	}

	@Test
	void shouldCreateNoChargeRewashLinkedToOrigin() {
		LaundryShipmentLine origin = LaundryShipmentLine.standard(item(), new BigDecimal("8"));
		LaundryShipmentLine rewash = LaundryShipmentLine.rewash(
				origin,
				new BigDecimal("2"),
				LaundryRewashReason.INCOMPLETE_WASH
		);

		assertEquals(origin, rewash.getOriginLine());
		assertEquals(LaundryRewashReason.INCOMPLETE_WASH, rewash.getRewashReason());
		assertEquals(new BigDecimal("0.00"), rewash.getUnitPriceSnapshot());
		assertEquals(new BigDecimal("0.00"), rewash.getBillableAmount());
	}

	private LaundryShipment shipment(LaundryShipmentLine line) {
		LaundryShipment shipment = LaundryShipment.create(
				"LAV-TEST",
				LaundryProvider.create("Lavanderia", null, true),
				LaundryShipmentKind.STANDARD,
				SENT_AT,
				null,
				"operador.demo"
		);
		shipment.addLine(line);
		return shipment;
	}

	private LaundryItem item() {
		return LaundryItem.create("Toalla", LaundryUnitType.PIECE, new BigDecimal("3.50"), true);
	}
}
