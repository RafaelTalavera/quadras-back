package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CreateMaintenanceProviderDto;
import com.axioma.quadras.domain.dto.MaintenanceProviderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceProviderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceProviderService {

	private final MaintenanceProviderRepository maintenanceProviderRepository;
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public MaintenanceProviderService(
			MaintenanceProviderRepository maintenanceProviderRepository,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.maintenanceProviderRepository = maintenanceProviderRepository;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
	}

	public List<MaintenanceProviderDto> list() {
		return maintenanceProviderRepository.findAllProjectedByOrderByProviderTypeAscNameAsc().stream()
				.map(MaintenanceProviderDto::from)
				.toList();
	}

	@Transactional
	public MaintenanceProviderDto create(CreateMaintenanceProviderDto input, String actorUsername) {
		validateUniqueName(input.providerType(), input.name(), null);
		final MaintenanceProvider provider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						input.providerType(),
						input.specialty(),
						input.name(),
						input.serviceLabel(),
						input.scopeDescription(),
						input.contact(),
						input.active() == null || input.active(),
						actorUsername
				)
		);
		auditTrailService.record(
				"maintenance",
				"maintenance-provider",
				provider.getId(),
				"CREATED",
				"Proveedor de mantenimiento creado",
				List.of(),
				null,
				snapshot(provider)
		);
		publishCatalogEvent(provider.getId(), "provider-created");
		return MaintenanceProviderDto.from(provider);
	}

	@Transactional
	public MaintenanceProviderDto update(
			Long providerId,
			UpdateMaintenanceProviderDto input,
			String actorUsername
	) {
		final MaintenanceProvider provider = findOrThrow(providerId);
		final Map<String, Object> beforeState = snapshot(provider);
		validateUniqueName(input.providerType(), input.name(), providerId);
		provider.update(
				input.providerType(),
				input.specialty(),
				input.name(),
				input.serviceLabel(),
				input.scopeDescription(),
				input.contact(),
				input.active(),
				actorUsername
		);
		recordAudit(provider.getId(), "UPDATED", "Proveedor de mantenimiento actualizado", beforeState, snapshot(provider));
		publishCatalogEvent(provider.getId(), "provider-updated");
		return MaintenanceProviderDto.from(provider);
	}

	public List<AuditEventDto> audit(Long providerId) {
		findOrThrow(providerId);
		return auditTrailService.findByEntity("maintenance-provider", providerId);
	}

	public MaintenanceProvider findOrThrow(Long providerId) {
		return maintenanceProviderRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Maintenance provider " + providerId + " not found"
				));
	}

	private void validateUniqueName(
			com.axioma.quadras.domain.model.MaintenanceProviderType providerType,
			String name,
			Long providerId
	) {
		final boolean exists = providerId == null
				? maintenanceProviderRepository.existsByProviderTypeAndNameIgnoreCase(providerType, name)
				: maintenanceProviderRepository.existsByProviderTypeAndNameIgnoreCaseAndIdNot(
						providerType,
						name,
						providerId
				);
		if (exists) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance provider name already exists for that type."
			);
		}
	}

	private void publishCatalogEvent(Long entityId, String action) {
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MAINTENANCE,
				action,
				entityId,
				null,
				null
		);
	}

	private void recordAudit(
			Long entityId,
			String actionName,
			String summaryText,
			Map<String, Object> beforeState,
			Map<String, Object> afterState
	) {
		auditTrailService.record(
				"maintenance",
				"maintenance-provider",
				entityId,
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(MaintenanceProvider provider) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", provider.getId());
		snapshot.put("providerType", provider.getProviderType() == null ? null : provider.getProviderType().name());
		snapshot.put("specialty", provider.getSpecialty() == null ? null : provider.getSpecialty().name());
		snapshot.put("name", provider.getName());
		snapshot.put("serviceLabel", provider.getServiceLabel());
		snapshot.put("scopeDescription", provider.getScopeDescription());
		snapshot.put("contact", provider.getContact());
		snapshot.put("active", provider.isActive());
		snapshot.put("createdAt", toValue(provider.getCreatedAt()));
		snapshot.put("updatedAt", toValue(provider.getUpdatedAt()));
		snapshot.put("createdBy", provider.getCreatedBy());
		snapshot.put("updatedBy", provider.getUpdatedBy());
		return snapshot;
	}

	private List<Map<String, Object>> diff(Map<String, Object> before, Map<String, Object> after) {
		return before.keySet().stream()
				.filter(field -> !Objects.equals(before.get(field), after.get(field)))
				.map(field -> {
					final Map<String, Object> change = new LinkedHashMap<>();
					change.put("field", field);
					change.put("before", before.get(field));
					change.put("after", after.get(field));
					return change;
				})
				.toList();
	}

	private String toValue(Object value) {
		return value == null ? null : value.toString();
	}
}
