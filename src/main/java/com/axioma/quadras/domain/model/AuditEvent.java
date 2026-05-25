package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

	private static final int MAX_MODULE_LENGTH = 40;
	private static final int MAX_ENTITY_TYPE_LENGTH = 80;
	private static final int MAX_ACTION_LENGTH = 40;
	private static final int MAX_USERNAME_LENGTH = 120;
	private static final int MAX_ROLE_LENGTH = 60;
	private static final int MAX_SUMMARY_LENGTH = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "module_name", nullable = false, length = MAX_MODULE_LENGTH)
	private String moduleName;

	@Column(name = "entity_type", nullable = false, length = MAX_ENTITY_TYPE_LENGTH)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	@Column(name = "action_name", nullable = false, length = MAX_ACTION_LENGTH)
	private String actionName;

	@Column(name = "occurred_at", nullable = false)
	private OffsetDateTime occurredAt;

	@Column(name = "actor_username", length = MAX_USERNAME_LENGTH)
	private String actorUsername;

	@Column(name = "actor_role", length = MAX_ROLE_LENGTH)
	private String actorRole;

	@Column(name = "summary_text", length = MAX_SUMMARY_LENGTH)
	private String summaryText;

	@Column(name = "changes_json")
	private String changesJson;

	@Column(name = "before_state_json")
	private String beforeStateJson;

	@Column(name = "after_state_json")
	private String afterStateJson;

	protected AuditEvent() {
	}

	public static AuditEvent create(
			String moduleName,
			String entityType,
			Long entityId,
			String actionName,
			String actorUsername,
			String actorRole,
			String summaryText,
			String changesJson,
			String beforeStateJson,
			String afterStateJson
	) {
		final AuditEvent event = new AuditEvent();
		event.moduleName = normalizeRequired(moduleName, "moduleName", MAX_MODULE_LENGTH);
		event.entityType = normalizeRequired(entityType, "entityType", MAX_ENTITY_TYPE_LENGTH);
		if (entityId == null) {
			throw new IllegalArgumentException("entityId is required");
		}
		event.entityId = entityId;
		event.actionName = normalizeRequired(actionName, "actionName", MAX_ACTION_LENGTH);
		event.actorUsername = normalizeOptional(actorUsername, "actorUsername", MAX_USERNAME_LENGTH);
		event.actorRole = normalizeOptional(actorRole, "actorRole", MAX_ROLE_LENGTH);
		event.summaryText = normalizeOptional(summaryText, "summaryText", MAX_SUMMARY_LENGTH);
		event.changesJson = normalizeJson(changesJson);
		event.beforeStateJson = normalizeJson(beforeStateJson);
		event.afterStateJson = normalizeJson(afterStateJson);
		return event;
	}

	public Long getId() {
		return id;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getEntityType() {
		return entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public String getActionName() {
		return actionName;
	}

	public OffsetDateTime getOccurredAt() {
		return occurredAt;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public String getActorRole() {
		return actorRole;
	}

	public String getSummaryText() {
		return summaryText;
	}

	public String getChangesJson() {
		return changesJson;
	}

	public String getBeforeStateJson() {
		return beforeStateJson;
	}

	public String getAfterStateJson() {
		return afterStateJson;
	}

	@PrePersist
	void onCreate() {
		if (occurredAt == null) {
			occurredAt = OffsetDateTime.now(ZoneOffset.UTC);
		}
	}

	private static String normalizeRequired(String value, String fieldName, int maxLength) {
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

	private static String normalizeJson(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
