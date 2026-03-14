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
import java.util.Locale;

@Entity
@Table(name = "app_users")
public class AppUser {

	private static final int MAX_USERNAME_LENGTH = 80;
	private static final int MAX_PASSWORD_HASH_LENGTH = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", nullable = false, length = MAX_USERNAME_LENGTH, unique = true)
	private String username;

	@Column(name = "password_hash", nullable = false, length = MAX_PASSWORD_HASH_LENGTH)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 40)
	private AppUserRole role;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected AppUser() {
	}

	private AppUser(String username, String passwordHash, AppUserRole role, boolean enabled) {
		this.username = normalizeUsername(username);
		this.passwordHash = normalizePasswordHash(passwordHash);
		this.role = requireRole(role);
		this.enabled = enabled;
	}

	public static AppUser create(String username, String passwordHash, AppUserRole role, boolean enabled) {
		return new AppUser(username, passwordHash, role, enabled);
	}

	public static String normalizeUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username is required");
		}
		final String normalized = username.trim().toLowerCase(Locale.ROOT);
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException(
					"username must be <= " + MAX_USERNAME_LENGTH + " chars"
			);
		}
		return normalized;
	}

	public void updateSecurity(String passwordHash, AppUserRole role, boolean enabled) {
		this.passwordHash = normalizePasswordHash(passwordHash);
		this.role = requireRole(role);
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public AppUserRole getRole() {
		return role;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
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

	private static String normalizePasswordHash(String passwordHash) {
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new IllegalArgumentException("passwordHash is required");
		}
		final String normalized = passwordHash.trim();
		if (normalized.length() > MAX_PASSWORD_HASH_LENGTH) {
			throw new IllegalArgumentException(
					"passwordHash must be <= " + MAX_PASSWORD_HASH_LENGTH + " chars"
			);
		}
		return normalized;
	}

	private static AppUserRole requireRole(AppUserRole role) {
		if (role == null) {
			throw new IllegalArgumentException("role is required");
		}
		return role;
	}
}
