package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "massage_therapists")
public class MassageTherapist {

	private static final int MAX_NAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private MassageProvider provider;

	@Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected MassageTherapist() {
	}

	private MassageTherapist(MassageProvider provider, String name, boolean active) {
		this.provider = requireProvider(provider);
		this.name = normalize(name);
		this.active = active;
	}

	public static MassageTherapist create(MassageProvider provider, String name) {
		return new MassageTherapist(provider, name, true);
	}

	public Long getId() {
		return id;
	}

	public MassageProvider getProvider() {
		return provider;
	}

	public String getName() {
		return name;
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

	public void update(String name, boolean active) {
		this.name = normalize(name);
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

	private static MassageProvider requireProvider(MassageProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider is required");
		}
		return provider;
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("name must be <= 120 chars");
		}
		return normalized;
	}
}
