package com.axioma.quadras.domain.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "laundry_documents")
public class LaundryDocument {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 36)
	private String reference;
	@Enumerated(EnumType.STRING) @Column(name = "document_type", nullable = false, length = 30)
	private LaundryDocumentType documentType;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
	private LaundryDocumentStatus status;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "provider_id", nullable = false)
	private LaundryProvider provider;
	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;
	@Column(length = 500)
	private String notes;
	@Column(name = "created_by", nullable = false, length = 120)
	private String createdBy;
	@Column(name = "submitted_by", length = 120)
	private String submittedBy;
	@Column(name = "submitted_at")
	private OffsetDateTime submittedAt;
	@Column(name = "correlation_id", nullable = false, length = 64)
	private String correlationId;
	@Version @Column(nullable = false)
	private long version;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LaundryDocumentLine> lines = new ArrayList<>();

	protected LaundryDocument() { }

	public static LaundryDocument draft(String reference, LaundryDocumentType type,
			LaundryProvider provider, OffsetDateTime effectiveAt, String notes,
			String actor, String correlationId) {
		LaundryDocument value = new LaundryDocument();
		value.reference = reference;
		value.documentType = type;
		value.status = LaundryDocumentStatus.DRAFT;
		value.provider = provider;
		value.effectiveAt = effectiveAt;
		value.notes = notes == null || notes.isBlank() ? null : notes.trim();
		value.createdBy = actor;
		value.correlationId = correlationId;
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		value.updatedAt = value.createdAt;
		return value;
	}

	public void addLine(LaundryItem item, BigDecimal quantity, BigDecimal unitPrice) {
		lines.add(LaundryDocumentLine.create(this, item, quantity, unitPrice));
	}

	public void submit(String actor) {
		if (status != LaundryDocumentStatus.DRAFT) throw new IllegalStateException("Only DRAFT documents can be submitted");
		status = LaundryDocumentStatus.SUBMITTED;
		submittedBy = actor;
		submittedAt = OffsetDateTime.now(ZoneOffset.UTC);
		updatedAt = submittedAt;
	}

	public Long getId() { return id; }
	public String getReference() { return reference; }
	public LaundryDocumentType getDocumentType() { return documentType; }
	public LaundryDocumentStatus getStatus() { return status; }
	public LaundryProvider getProvider() { return provider; }
	public OffsetDateTime getEffectiveAt() { return effectiveAt; }
	public String getNotes() { return notes; }
	public String getCreatedBy() { return createdBy; }
	public String getSubmittedBy() { return submittedBy; }
	public OffsetDateTime getSubmittedAt() { return submittedAt; }
	public String getCorrelationId() { return correlationId; }
	public long getVersion() { return version; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public List<LaundryDocumentLine> getLines() { return List.copyOf(lines); }
}
