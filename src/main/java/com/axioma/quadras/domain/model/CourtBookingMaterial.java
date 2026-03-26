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

@Entity
@Table(name = "court_booking_materials")
public class CourtBookingMaterial {

	private static final int MAX_LABEL_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "court_booking_id", nullable = false)
	private CourtBooking booking;

	@Enumerated(EnumType.STRING)
	@Column(name = "material_code", nullable = false, length = 30)
	private CourtMaterialCode materialCode;

	@Column(name = "material_label", nullable = false, length = MAX_LABEL_LENGTH)
	private String materialLabel;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "total_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalPrice;

	protected CourtBookingMaterial() {
	}

	private CourtBookingMaterial(
			CourtMaterialCode materialCode,
			String materialLabel,
			int quantity,
			BigDecimal unitPrice,
			BigDecimal totalPrice
	) {
		this.materialCode = materialCode;
		this.materialLabel = materialLabel;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalPrice = totalPrice;
	}

	public static CourtBookingMaterial of(
			CourtMaterialCode materialCode,
			String materialLabel,
			int quantity,
			BigDecimal unitPrice,
			BigDecimal totalPrice
	) {
		if (materialCode == null) {
			throw new IllegalArgumentException("materialCode is required");
		}
		if (materialLabel == null || materialLabel.isBlank()) {
			throw new IllegalArgumentException("materialLabel is required");
		}
		if (materialLabel.trim().length() > MAX_LABEL_LENGTH) {
			throw new IllegalArgumentException("materialLabel must be <= " + MAX_LABEL_LENGTH + " chars");
		}
		if (quantity < 0) {
			throw new IllegalArgumentException("quantity must be greater than or equal to zero");
		}
		if (unitPrice == null || unitPrice.signum() < 0) {
			throw new IllegalArgumentException("unitPrice must be greater than or equal to zero");
		}
		if (totalPrice == null || totalPrice.signum() < 0) {
			throw new IllegalArgumentException("totalPrice must be greater than or equal to zero");
		}
		return new CourtBookingMaterial(materialCode, materialLabel.trim(), quantity, unitPrice, totalPrice);
	}

	void attachTo(CourtBooking booking) {
		this.booking = booking;
	}

	public CourtMaterialCode getMaterialCode() {
		return materialCode;
	}

	public String getMaterialLabel() {
		return materialLabel;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}
}
