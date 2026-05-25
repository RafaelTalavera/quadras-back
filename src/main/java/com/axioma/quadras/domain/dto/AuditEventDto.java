package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;

public record AuditEventDto(
		Long id,
		String moduleName,
		String entityType,
		Long entityId,
		String actionName,
		OffsetDateTime occurredAt,
		String actorUsername,
		String actorRole,
		String summaryText,
		Object changes,
		Object beforeState,
		Object afterState
) {
	public static AuditEventDto from(AuditEvent event, ObjectMapper objectMapper) {
		return new AuditEventDto(
				event.getId(),
				event.getModuleName(),
				event.getEntityType(),
				event.getEntityId(),
				event.getActionName(),
				event.getOccurredAt(),
				event.getActorUsername(),
				event.getActorRole(),
				event.getSummaryText(),
				parseJson(event.getChangesJson(), objectMapper),
				parseJson(event.getBeforeStateJson(), objectMapper),
				parseJson(event.getAfterStateJson(), objectMapper)
		);
	}

	private static Object parseJson(String rawJson, ObjectMapper objectMapper) {
		if (rawJson == null || rawJson.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(rawJson, Object.class);
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to parse persisted audit json", ex);
		}
	}
}
