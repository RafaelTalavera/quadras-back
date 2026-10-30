package com.axioma.quadras.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "laundry_shipments")
public class LaundryShipment {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "shipment_number", nullable = false, length = 36, unique = true)
	private String shipmentNumber;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "provider_id", nullable = false)
	private LaundryProvider provider;
	@Column(name = "provider_name_snapshot", nullable = false, length = 120)
	private String providerNameSnapshot;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
	private LaundryShipmentKind kind;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
	private LaundryShipmentStatus status;
	@Column(name = "sent_at")
	private OffsetDateTime sentAt;
	@Column(name = "received_at")
	private OffsetDateTime receivedAt;
	@Column(length = 500)
	private String notes;
	@Column(name = "cancellation_notes", length = 500)
	private String cancellationNotes;
	@Version @Column(nullable = false)
	private long version;
	@OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<LaundryShipmentLine> lines = new ArrayList<>();
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	@Column(name = "created_by", nullable = false, length = 120)
	private String createdBy;
	@Column(name = "updated_by", nullable = false, length = 120)
	private String updatedBy;

	protected LaundryShipment() { }

	public static LaundryShipment create(
			String number, LaundryProvider provider, LaundryShipmentKind kind,
			OffsetDateTime sentAt, String notes, String actor
	) {
		if (number == null || number.isBlank()) throw new IllegalArgumentException("shipmentNumber is required");
		if (provider == null) throw new IllegalArgumentException("provider is required");
		if (kind == null) throw new IllegalArgumentException("kind is required");
		if (sentAt == null) throw new IllegalArgumentException("sentAt is required");
		LaundryShipment value = new LaundryShipment();
		value.shipmentNumber = number; value.provider = provider; value.providerNameSnapshot = provider.getName();
		value.kind = kind; value.status = LaundryShipmentStatus.SENT; value.sentAt = sentAt;
		value.notes = optional(notes, 500); value.createdBy = actor(actor); value.updatedBy = value.createdBy;
		return value;
	}

	public static LaundryShipment createPendingRewash(
			String number, LaundryProvider provider, String notes, String actor
	) {
		if (number == null || number.isBlank()) throw new IllegalArgumentException("shipmentNumber is required");
		if (provider == null) throw new IllegalArgumentException("provider is required");
		LaundryShipment value = new LaundryShipment();
		value.shipmentNumber = number; value.provider = provider; value.providerNameSnapshot = provider.getName();
		value.kind = LaundryShipmentKind.REWASH; value.status = LaundryShipmentStatus.PENDING_DISPATCH;
		value.sentAt = null; value.notes = optional(notes, 500);
		value.createdBy = actor(actor); value.updatedBy = value.createdBy;
		return value;
	}

	public static LaundryShipment createReceipt(
			String number, LaundryProvider provider, OffsetDateTime receivedAt, String notes, String actor
	) {
		if (number == null || number.isBlank()) throw new IllegalArgumentException("shipmentNumber is required");
		if (provider == null) throw new IllegalArgumentException("provider is required");
		if (receivedAt == null) throw new IllegalArgumentException("receivedAt is required");
		LaundryShipment value = new LaundryShipment();
		value.shipmentNumber = number; value.provider = provider; value.providerNameSnapshot = provider.getName();
		value.kind = LaundryShipmentKind.RECEIPT; value.status = LaundryShipmentStatus.RECEIVED;
		value.sentAt = null; value.receivedAt = receivedAt; value.notes = optional(notes, 500);
		value.createdBy = actor(actor); value.updatedBy = value.createdBy;
		return value;
	}

	public void addLine(LaundryShipmentLine line) {
		if (line == null) throw new IllegalArgumentException("line is required");
		if (lines.stream().anyMatch(existing -> existing.getItem().getId().equals(line.getItem().getId()))) {
			throw new IllegalArgumentException("Shipment cannot contain duplicate items");
		}
		line.attachTo(this); lines.add(line);
	}

	public void refreshAfterReceipt(OffsetDateTime receivedAt, String actor) {
		if (status != LaundryShipmentStatus.SENT && status != LaundryShipmentStatus.PARTIALLY_RECEIVED) {
			throw new IllegalStateException("Shipment cannot receive more items");
		}
		this.receivedAt = receivedAt;
		this.updatedBy = actor(actor);
		this.status = lines.stream().allMatch(line -> line.getPendingQuantity().signum() == 0)
				? LaundryShipmentStatus.RECEIVED : LaundryShipmentStatus.PARTIALLY_RECEIVED;
	}

	public void dispatch(OffsetDateTime sentAt, String notes, String actor) {
		if (kind != LaundryShipmentKind.REWASH || status != LaundryShipmentStatus.PENDING_DISPATCH) {
			throw new IllegalStateException("Only pending rewash shipments can be dispatched");
		}
		if (sentAt == null) throw new IllegalArgumentException("sentAt is required");
		this.sentAt = sentAt;
		this.notes = optional(notes, 500);
		this.status = LaundryShipmentStatus.SENT;
		this.updatedBy = actor(actor);
	}

	public void cancel(String reason, String actor) {
		boolean cancellablePendingRewash = kind == LaundryShipmentKind.REWASH
				&& status == LaundryShipmentStatus.PENDING_DISPATCH;
		if (status != LaundryShipmentStatus.SENT && !cancellablePendingRewash) {
			throw new IllegalStateException("Only untouched sent or pending rewash shipments can be cancelled");
		}
		this.cancellationNotes = required(reason, "cancellationNotes", 500);
		this.status = LaundryShipmentStatus.CANCELLED; this.updatedBy = actor(actor);
	}

	public Long getId() { return id; }
	public String getShipmentNumber() { return shipmentNumber; }
	public LaundryProvider getProvider() { return provider; }
	public String getProviderNameSnapshot() { return providerNameSnapshot; }
	public LaundryShipmentKind getKind() { return kind; }
	public LaundryShipmentStatus getStatus() { return status; }
	public OffsetDateTime getSentAt() { return sentAt; }
	public OffsetDateTime getReceivedAt() { return receivedAt; }
	public String getNotes() { return notes; }
	public String getCancellationNotes() { return cancellationNotes; }
	public long getVersion() { return version; }
	public List<LaundryShipmentLine> getLines() { return List.copyOf(lines); }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	public String getCreatedBy() { return createdBy; }
	public String getUpdatedBy() { return updatedBy; }
	@PrePersist void onCreate() { createdAt = OffsetDateTime.now(ZoneOffset.UTC); updatedAt = createdAt; }
	@PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }

	private static String actor(String value) { return required(value, "actor", 120).toLowerCase(); }
	private static String required(String value, String field, int max) {
		if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
		String normalized = value.trim();
		if (normalized.length() > max) throw new IllegalArgumentException(field + " must be <= " + max + " chars");
		return normalized;
	}
	private static String optional(String value, int max) {
		if (value == null || value.isBlank()) return null;
		String normalized = value.trim();
		if (normalized.length() > max) throw new IllegalArgumentException("notes must be <= " + max + " chars");
		return normalized;
	}
}
