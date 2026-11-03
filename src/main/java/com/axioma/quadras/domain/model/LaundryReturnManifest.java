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
@Table(name = "laundry_return_manifests")
public class LaundryReturnManifest {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 36)
	private String reference;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "provider_id", nullable = false)
	private LaundryProvider provider;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
	private LaundryReturnStatus status;
	@Column(name = "effective_at", nullable = false)
	private OffsetDateTime effectiveAt;
	@Column(length = 500) private String notes;
	@Column(name = "created_by", nullable = false, length = 120) private String createdBy;
	@Column(name = "submitted_by", length = 120) private String submittedBy;
	@Column(name = "submitted_at") private OffsetDateTime submittedAt;
	@Column(name = "confirmed_by", length = 120) private String confirmedBy;
	@Column(name = "confirmed_at") private OffsetDateTime confirmedAt;
	@Column(name = "correction_notes", length = 500) private String correctionNotes;
	@Column(name = "correlation_id", nullable = false, length = 64) private String correlationId;
	@Version @Column(nullable = false) private long version;
	@Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
	@OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LaundryReturnManifestLine> lines = new ArrayList<>();

	protected LaundryReturnManifest() { }

	public static LaundryReturnManifest draft(String reference, LaundryProvider provider,
			OffsetDateTime effectiveAt, String notes, String actor, String correlationId) {
		LaundryReturnManifest value = new LaundryReturnManifest();
		value.reference = reference;
		value.provider = provider;
		value.status = LaundryReturnStatus.DRAFT;
		value.effectiveAt = effectiveAt;
		value.notes = optional(notes);
		value.createdBy = actor;
		value.correlationId = correlationId;
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		value.updatedAt = value.createdAt;
		return value;
	}

	public void addLine(LaundryItem item, BigDecimal clean, BigDecimal retained) {
		lines.add(LaundryReturnManifestLine.create(this, item, clean, retained));
	}

	public void updateDraft(OffsetDateTime effectiveAt, String notes) {
		if (status != LaundryReturnStatus.DRAFT)
			throw new IllegalStateException("Only DRAFT returns can be updated");
		this.effectiveAt = effectiveAt;
		this.notes = optional(notes);
		this.lines.clear();
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	public void submit(String actor) {
		if (status != LaundryReturnStatus.DRAFT) throw new IllegalStateException("Only DRAFT returns can be submitted");
		status = LaundryReturnStatus.SUBMITTED;
		submittedBy = actor;
		submittedAt = OffsetDateTime.now(ZoneOffset.UTC);
		updatedAt = submittedAt;
	}

	public void confirm(String actor, String corrections, boolean manual) {
		if ((!manual && status != LaundryReturnStatus.SUBMITTED)
				|| (manual && status != LaundryReturnStatus.DRAFT)) {
			throw new IllegalStateException("Return manifest is not confirmable");
		}
		status = manual ? LaundryReturnStatus.CONFIRMED_BY_HOTEL : LaundryReturnStatus.CONFIRMED;
		confirmedBy = actor;
		confirmedAt = OffsetDateTime.now(ZoneOffset.UTC);
		correctionNotes = optional(corrections);
		updatedAt = confirmedAt;
	}

	private static String optional(String value) { return value == null || value.isBlank() ? null : value.trim(); }
	public Long getId() { return id; }
	public String getReference() { return reference; }
	public LaundryProvider getProvider() { return provider; }
	public LaundryReturnStatus getStatus() { return status; }
	public OffsetDateTime getEffectiveAt() { return effectiveAt; }
	public String getNotes() { return notes; }
	public String getCreatedBy() { return createdBy; }
	public String getSubmittedBy() { return submittedBy; }
	public OffsetDateTime getSubmittedAt() { return submittedAt; }
	public String getConfirmedBy() { return confirmedBy; }
	public OffsetDateTime getConfirmedAt() { return confirmedAt; }
	public String getCorrectionNotes() { return correctionNotes; }
	public String getCorrelationId() { return correlationId; }
	public long getVersion() { return version; }
	public List<LaundryReturnManifestLine> getLines() { return List.copyOf(lines); }
}
