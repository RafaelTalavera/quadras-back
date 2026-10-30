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

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String username;

	@Column(name = "installation_id", nullable = false, length = 120)
	private String installationId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PushPlatform platform;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PushProviderType provider;

	@Column(name = "encrypted_token", length = 6000)
	private String encryptedToken;

	@Column(name = "token_hash", length = 64)
	private String tokenHash;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "last_seen_at", nullable = false)
	private OffsetDateTime lastSeenAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected PushSubscription() {
	}

	public static PushSubscription create(
			String username,
			String installationId,
			PushPlatform platform,
			PushProviderType provider,
			String encryptedToken,
			String tokenHash,
			OffsetDateTime now
	) {
		final PushSubscription subscription = new PushSubscription();
		subscription.username = requiredText(username, "username");
		subscription.installationId = requiredText(installationId, "installationId");
		subscription.refresh(platform, provider, encryptedToken, tokenHash, now);
		return subscription;
	}

	public void refresh(
			PushPlatform platform,
			PushProviderType provider,
			String encryptedToken,
			String tokenHash,
			OffsetDateTime now
	) {
		if (platform == null || provider == null || now == null) {
			throw new IllegalArgumentException("platform, provider and now are required");
		}
		this.platform = platform;
		this.provider = provider;
		this.encryptedToken = requiredText(encryptedToken, "encryptedToken");
		this.tokenHash = requiredText(tokenHash, "tokenHash");
		this.active = true;
		this.lastSeenAt = now;
	}

	public void heartbeat(OffsetDateTime now) {
		if (now == null) {
			throw new IllegalArgumentException("now is required");
		}
		lastSeenAt = now;
	}

	public void deactivate(OffsetDateTime now) {
		active = false;
		encryptedToken = null;
		tokenHash = null;
		lastSeenAt = now;
	}

	@PrePersist
	void onCreate() {
		createdAt = lastSeenAt;
		updatedAt = lastSeenAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = lastSeenAt;
	}

	public Long getId() { return id; }
	public String getUsername() { return username; }
	public String getInstallationId() { return installationId; }
	public PushPlatform getPlatform() { return platform; }
	public PushProviderType getProvider() { return provider; }
	public String getEncryptedToken() { return encryptedToken; }
	public String getTokenHash() { return tokenHash; }
	public boolean isActive() { return active; }
	public OffsetDateTime getLastSeenAt() { return lastSeenAt; }

	private static String requiredText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required");
		}
		return value.trim();
	}
}
