package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_idempotency_keys")
public class LaundryIdempotencyRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "actor_username", nullable = false, length = 120)
	private String actorUsername;

	@Column(name = "idempotency_key", nullable = false, length = 120)
	private String idempotencyKey;

	@Column(name = "request_fingerprint", nullable = false, length = 64)
	private String requestFingerprint;

	@Column(nullable = false)
	private boolean completed;

	@Column(name = "response_status")
	private Integer responseStatus;

	@Column(name = "response_body")
	private String responseBody;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	protected LaundryIdempotencyRecord() {
	}

	public static LaundryIdempotencyRecord inProgress(
			String actorUsername,
			String idempotencyKey,
			String requestFingerprint,
			OffsetDateTime expiresAt
	) {
		final LaundryIdempotencyRecord record = new LaundryIdempotencyRecord();
		record.actorUsername = actorUsername;
		record.idempotencyKey = idempotencyKey;
		record.requestFingerprint = requestFingerprint;
		record.completed = false;
		record.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		record.expiresAt = expiresAt;
		return record;
	}

	public void complete(int status, String body) {
		this.completed = true;
		this.responseStatus = status;
		this.responseBody = body;
	}

	public boolean isExpired(OffsetDateTime now) {
		return !expiresAt.isAfter(now);
	}

	public Long getId() {
		return id;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getRequestFingerprint() {
		return requestFingerprint;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Integer getResponseStatus() {
		return responseStatus;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}
}
