package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "court_material_settings")
public class CourtMaterialSetting {

	private static final int MAX_LABEL_LENGTH = 120;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "code", nullable = false, length = 30)
	private CourtMaterialCode code;

	@Column(name = "label", nullable = false, length = MAX_LABEL_LENGTH)
	private String label;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "charge_guest", nullable = false)
	private boolean chargeGuest;

	@Column(name = "charge_vip", nullable = false)
	private boolean chargeVip;

	@Column(name = "charge_external", nullable = false)
	private boolean chargeExternal;

	@Column(name = "charge_partner_coach", nullable = false)
	private boolean chargePartnerCoach;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected CourtMaterialSetting() {
	}

	public Long getId() {
		return id;
	}

	public CourtMaterialCode getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public boolean isChargeGuest() {
		return chargeGuest;
	}

	public boolean isChargeVip() {
		return chargeVip;
	}

	public boolean isChargeExternal() {
		return chargeExternal;
	}

	public boolean isChargePartnerCoach() {
		return chargePartnerCoach;
	}

	public boolean isActive() {
		return active;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void update(
			String label,
			BigDecimal unitPrice,
			boolean chargeGuest,
			boolean chargeVip,
			boolean chargeExternal,
			boolean chargePartnerCoach,
			boolean active,
			String actorUsername
	) {
		this.label = normalizeLabel(label);
		if (unitPrice == null || unitPrice.signum() < 0) {
			throw new IllegalArgumentException("unitPrice must be greater than or equal to zero");
		}
		this.unitPrice = unitPrice;
		this.chargeGuest = chargeGuest;
		this.chargeVip = chargeVip;
		this.chargeExternal = chargeExternal;
		this.chargePartnerCoach = chargePartnerCoach;
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername);
	}

	public boolean charges(CourtCustomerType customerType) {
		return switch (customerType) {
			case GUEST -> chargeGuest;
			case VIP -> chargeVip;
			case EXTERNAL -> chargeExternal;
			case PARTNER_COACH -> chargePartnerCoach;
		};
	}

	@PrePersist
	void onCreate() {
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	private static String normalizeLabel(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("label is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > MAX_LABEL_LENGTH) {
			throw new IllegalArgumentException("label must be <= " + MAX_LABEL_LENGTH + " chars");
		}
		return normalized;
	}

	private static String normalizeActor(String actorUsername) {
		if (actorUsername == null || actorUsername.isBlank()) {
			return null;
		}
		final String normalized = actorUsername.trim().toLowerCase();
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException("updatedBy must be <= " + MAX_USERNAME_LENGTH + " chars");
		}
		return normalized;
	}
}
