package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.model.AuditEvent;
import com.axioma.quadras.repository.AuditEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditTrailService {

	private final AuditEventRepository auditEventRepository;
	private final CurrentActorService currentActorService;
	private final ObjectMapper objectMapper;

	public AuditTrailService(
			AuditEventRepository auditEventRepository,
			CurrentActorService currentActorService,
			ObjectMapper objectMapper
	) {
		this.auditEventRepository = auditEventRepository;
		this.currentActorService = currentActorService;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void record(
			String moduleName,
			String entityType,
			Long entityId,
			String actionName,
			String summaryText,
			Object changes,
			Object beforeState,
			Object afterState
	) {
		auditEventRepository.save(
				AuditEvent.create(
						moduleName,
						entityType,
						entityId,
						actionName,
						currentActorService.currentUsername(),
						currentActorService.currentRole(),
						summaryText,
						writeJson(changes),
						writeJson(beforeState),
						writeJson(afterState)
				)
		);
	}

	public List<AuditEventDto> findByEntity(String entityType, Long entityId) {
		return auditEventRepository.findAllByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(entityType, entityId)
				.stream()
				.map(event -> AuditEventDto.from(event, objectMapper))
				.toList();
	}

	private String writeJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to serialize audit payload", ex);
		}
	}
}
