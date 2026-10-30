package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_providers")
public class LaundryProvider {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 120)
	private String name;
	@Column(length = 160)
	private String contact;
	@Column(nullable = false)
	private boolean active;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected LaundryProvider() { }

	public static LaundryProvider create(String name, String contact, boolean active) {
		LaundryProvider value = new LaundryProvider();
		value.name = required(name, "name", 120);
		value.contact = optional(contact, 160);
		value.active = active;
		return value;
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public String getContact() { return contact; }
	public boolean isActive() { return active; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public OffsetDateTime getUpdatedAt() { return updatedAt; }

	@PrePersist void onCreate() { createdAt = OffsetDateTime.now(ZoneOffset.UTC); updatedAt = createdAt; }
	@PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }

	private static String required(String value, String field, int max) {
		if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
		String normalized = value.trim();
		if (normalized.length() > max) throw new IllegalArgumentException(field + " must be <= " + max + " chars");
		return normalized;
	}

	private static String optional(String value, int max) {
		if (value == null || value.isBlank()) return null;
		String normalized = value.trim();
		if (normalized.length() > max) throw new IllegalArgumentException("contact must be <= " + max + " chars");
		return normalized;
	}
}
