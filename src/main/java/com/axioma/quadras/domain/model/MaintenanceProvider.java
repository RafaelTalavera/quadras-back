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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "maintenance_providers")
public class MaintenanceProvider {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_SERVICE_LABEL_LENGTH = 120;
	private static final int MAX_SCOPE_DESCRIPTION_LENGTH = 500;
	private static final int MAX_CONTACT_LENGTH = 160;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type", nullable = false, length = 20)
	private MaintenanceProviderType providerType;

	@Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(name = "service_label", nullable = false, length = MAX_SERVICE_LABEL_LENGTH)
	private String serviceLabel;

	@Column(name = "scope_description", length = MAX_SCOPE_DESCRIPTION_LENGTH)
	private String scopeDescription;

	@Column(name = "contact", length = MAX_CONTACT_LENGTH)
	private String contact;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "created_by", length = MAX_USERNAME_LENGTH, updatable = false)
	private String createdBy;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected MaintenanceProvider() {
	}

	public static MaintenanceProvider create(
			MaintenanceProviderType providerType,
			String name,
			String serviceLabel,
			String scopeDescription,
			String contact,
			boolean active,
			String actorUsername
	) {
		final MaintenanceProvider provider = new MaintenanceProvider();
		provider.providerType = requireType(providerType);
		provider.name = normalize(name, "name", MAX_NAME_LENGTH);
		provider.serviceLabel = normalize(serviceLabel, "serviceLabel", MAX_SERVICE_LABEL_LENGTH);
		provider.scopeDescription = normalizeOptional(
				scopeDescription,
				"scopeDescription",
				MAX_SCOPE_DESCRIPTION_LENGTH
		);
		provider.contact = normalizeOptional(contact, "contact", MAX_CONTACT_LENGTH);
		provider.active = active;
		provider.createdBy = normalizeActor(actorUsername, "createdBy");
		provider.updatedBy = provider.createdBy;
		return provider;
	}

	public void update(
			MaintenanceProviderType providerType,
			String name,
			String serviceLabel,
			String scopeDescription,
			String contact,
			boolean active,
			String actorUsername
	) {
		this.providerType = requireType(providerType);
		this.name = normalize(name, "name", MAX_NAME_LENGTH);
		this.serviceLabel = normalize(serviceLabel, "serviceLabel", MAX_SERVICE_LABEL_LENGTH);
		this.scopeDescription = normalizeOptional(
				scopeDescription,
				"scopeDescription",
				MAX_SCOPE_DESCRIPTION_LENGTH
		);
		this.contact = normalizeOptional(contact, "contact", MAX_CONTACT_LENGTH);
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
	}

	public Long getId() {
		return id;
	}

	public MaintenanceProviderType getProviderType() {
		return providerType;
	}

	public String getName() {
		return name;
	}

	public String getServiceLabel() {
		return serviceLabel;
	}

	public String getScopeDescription() {
		return scopeDescription;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
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

	private static MaintenanceProviderType requireType(MaintenanceProviderType value) {
		if (value == null) {
			throw new IllegalArgumentException("providerType is required");
		}
		return value;
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

	private static String normalizeOptional(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " must be <= " + maxLength + " chars");
		}
		return normalized;
	}

	private static String normalizeActor(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		final String normalized = value.trim().toLowerCase();
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException(fieldName + " must be <= " + MAX_USERNAME_LENGTH + " chars");
		}
		return normalized;
	}
}
