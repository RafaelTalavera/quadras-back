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
@Table(name = "massage_providers")
public class MassageProvider {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_SPECIALTY_LENGTH = 120;
	private static final int MAX_CONTACT_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(name = "specialty", nullable = false, length = MAX_SPECIALTY_LENGTH)
	private String specialty;

	@Column(name = "contact", nullable = false, length = MAX_CONTACT_LENGTH)
	private String contact;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected MassageProvider() {
	}

	private MassageProvider(String name, String specialty, String contact, boolean active) {
		this.name = normalize(name, "name", MAX_NAME_LENGTH);
		this.specialty = normalize(specialty, "specialty", MAX_SPECIALTY_LENGTH);
		this.contact = normalize(contact, "contact", MAX_CONTACT_LENGTH);
		this.active = active;
	}

	public static MassageProvider create(String name, String specialty, String contact) {
		return new MassageProvider(name, specialty, contact, true);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSpecialty() {
		return specialty;
	}

	public String getContact() {
		return contact;
	}

	public boolean isActive() {
		return active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void update(String name, String specialty, String contact, boolean active) {
		this.name = normalize(name, "name", MAX_NAME_LENGTH);
		this.specialty = normalize(specialty, "specialty", MAX_SPECIALTY_LENGTH);
		this.contact = normalize(contact, "contact", MAX_CONTACT_LENGTH);
		this.active = active;
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

	private static String normalize(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " must be <= " + maxLength + " chars");
		}
		return normalized;
	}
}
