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
@Table(name = "court_partner_coaches")
public class CourtPartnerCoach {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	protected CourtPartnerCoach() {
	}

	private CourtPartnerCoach(String name, boolean active, String actorUsername) {
		this.name = normalizeName(name);
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername);
	}

	public static CourtPartnerCoach create(String name, String actorUsername) {
		return new CourtPartnerCoach(name, true, actorUsername);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public void update(String name, boolean active, String actorUsername) {
		this.name = normalizeName(name);
		this.active = active;
		this.updatedBy = normalizeActor(actorUsername);
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

	private static String normalizeName(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("name must be <= " + MAX_NAME_LENGTH + " chars");
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
