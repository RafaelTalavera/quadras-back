package com.axioma.quadras.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "laundry_opening_stock")
public class LaundryOpeningStock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 36, unique = true)
	private String reference;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LaundryOpeningStockStatus status;

	@Column(name = "approved_by", nullable = false, length = 120)
	private String approvedBy;

	@Column(name = "approved_at", nullable = false)
	private OffsetDateTime approvedAt;

	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;

	@Column(length = 500)
	private String notes;

	@Column(name = "correlation_id", nullable = false, length = 64)
	private String correlationId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@OneToMany(mappedBy = "openingStock", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<LaundryOpeningStockLine> lines = new ArrayList<>();

	protected LaundryOpeningStock() {
	}

	public static LaundryOpeningStock create(
			String reference,
			String approvedBy,
			OffsetDateTime approvedAt,
			OffsetDateTime effectiveAt,
			String notes,
			String correlationId
	) {
		if (reference == null || reference.isBlank()) {
			throw new IllegalArgumentException("reference is required");
		}
		if (approvedBy == null || approvedBy.isBlank()) {
			throw new IllegalArgumentException("approvedBy is required");
		}
		if (approvedAt == null || effectiveAt == null) {
			throw new IllegalArgumentException("dates are required");
		}
		final LaundryOpeningStock opening = new LaundryOpeningStock();
		opening.reference = reference;
		opening.status = LaundryOpeningStockStatus.APPROVED;
		opening.approvedBy = approvedBy;
		opening.approvedAt = approvedAt;
		opening.effectiveAt = effectiveAt;
		opening.notes = notes;
		opening.correlationId = correlationId;
		opening.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return opening;
	}

	public void addLine(LaundryItem item, java.math.BigDecimal quantity) {
		lines.add(LaundryOpeningStockLine.create(this, item, quantity));
	}

	public Long getId() {
		return id;
	}

	public String getReference() {
		return reference;
	}

	public LaundryOpeningStockStatus getStatus() {
		return status;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public OffsetDateTime getApprovedAt() {
		return approvedAt;
	}

	public OffsetDateTime getEffectiveAt() {
		return effectiveAt;
	}

	public String getNotes() {
		return notes;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public List<LaundryOpeningStockLine> getLines() {
		return lines;
	}
}
