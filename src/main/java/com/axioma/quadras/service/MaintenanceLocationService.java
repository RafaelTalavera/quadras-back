package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CreateMaintenanceLocationDto;
import com.axioma.quadras.domain.dto.MaintenanceLocationDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceLocationDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentRepository;
import com.axioma.quadras.repository.MaintenanceLocationRepository;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceLocationService {
	private static final Pattern NUMERIC_CODE_PATTERN = Pattern.compile("^\\d+$");

	private final MaintenanceLocationRepository maintenanceLocationRepository;
	private final MaintenanceOrderRepository maintenanceOrderRepository;
	private final MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository;
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public MaintenanceLocationService(
			MaintenanceLocationRepository maintenanceLocationRepository,
			MaintenanceOrderRepository maintenanceOrderRepository,
			MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.maintenanceLocationRepository = maintenanceLocationRepository;
		this.maintenanceOrderRepository = maintenanceOrderRepository;
		this.maintenanceOrderAttachmentRepository = maintenanceOrderAttachmentRepository;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
	}

	public List<MaintenanceLocationDto> list() {
		return maintenanceLocationRepository.findAll().stream()
				.sorted(locationComparator())
				.map(MaintenanceLocationDto::from)
				.toList();
	}

	@Transactional
	public MaintenanceLocationDto create(CreateMaintenanceLocationDto input, String actorUsername) {
		final String resolvedCode = resolveReferenceCode(input.code());
		final String resolvedLabel = resolveLabel(input.locationType(), resolvedCode, input.label());
		validateUniqueCode(input.locationType(), resolvedCode, null);
		final MaintenanceLocation location = maintenanceLocationRepository.save(
				MaintenanceLocation.create(
						input.locationType(),
						input.locationCategory(),
						resolvedCode,
						resolvedLabel,
						input.floor(),
						input.description(),
						input.active() == null || input.active(),
						actorUsername
				)
		);
		auditTrailService.record(
				"maintenance",
				"maintenance-location",
				location.getId(),
				"CREATED",
				"Ubicacion de mantenimiento creada",
				List.of(),
				null,
				snapshot(location)
		);
		publishCatalogEvent(location.getId(), "location-created");
		return MaintenanceLocationDto.from(location);
	}

	@Transactional
	public MaintenanceLocationDto update(
			Long locationId,
			UpdateMaintenanceLocationDto input,
			String actorUsername
	) {
		final MaintenanceLocation location = findOrThrow(locationId);
		final Map<String, Object> beforeState = snapshot(location);
		final String resolvedCode = resolveReferenceCode(input.code());
		final String resolvedLabel = resolveLabel(input.locationType(), resolvedCode, input.label());
		validateUniqueCode(input.locationType(), resolvedCode, locationId);
		location.update(
				input.locationType(),
				input.locationCategory(),
				resolvedCode,
				resolvedLabel,
				input.floor(),
				input.description(),
				input.active(),
				actorUsername
		);
		recordAudit(location.getId(), "UPDATED", "Ubicacion de mantenimiento actualizada", beforeState, snapshot(location));
		publishCatalogEvent(location.getId(), "location-updated");
		return MaintenanceLocationDto.from(location);
	}

	public List<AuditEventDto> audit(Long locationId) {
		findOrThrow(locationId);
		return auditTrailService.findByEntity("maintenance-location", locationId);
	}

	public List<MaintenanceOrderDto> history(Long locationId) {
		findOrThrow(locationId);
		final var items = maintenanceOrderRepository.findHistoryItemsByLocationIdOrderByReportedAtDescIdDesc(
				locationId
		);
		final Map<Long, List<com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto>> attachmentsByOrderId =
				loadAttachmentMetadataByOrderId(items.stream().map(item -> item.getId()).toList());
		return items.stream()
				.map(item -> MaintenanceOrderDto.from(
						item,
						attachmentsByOrderId.getOrDefault(item.getId(), List.of())
				))
				.toList();
	}

	private Map<Long, List<com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto>> loadAttachmentMetadataByOrderId(
			Collection<Long> orderIds
	) {
		if (orderIds == null || orderIds.isEmpty()) {
			return Map.of();
		}
		final Map<Long, List<com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto>> attachmentsByOrderId =
				new LinkedHashMap<>();
		maintenanceOrderAttachmentRepository.findMetadataByOrderIdInOrderByCreatedAtDesc(orderIds)
				.forEach(metadata -> attachmentsByOrderId.computeIfAbsent(
						metadata.getOrderId(),
						ignored -> new java.util.ArrayList<>()
				).add(com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto.from(metadata)));
		return attachmentsByOrderId;
	}

	public MaintenanceLocation findOrThrow(Long locationId) {
		return maintenanceLocationRepository.findById(locationId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Maintenance location " + locationId + " not found"
				));
	}

	private void validateUniqueCode(
			com.axioma.quadras.domain.model.MaintenanceLocationType locationType,
			String code,
			Long locationId
	) {
		final boolean exists = locationId == null
				? maintenanceLocationRepository.existsByLocationTypeAndCodeIgnoreCase(locationType, code)
				: maintenanceLocationRepository.existsByLocationTypeAndCodeIgnoreCaseAndIdNot(
						locationType,
						code,
						locationId
				);
		if (exists) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance location code already exists for that type."
			);
		}
	}

	private String resolveReferenceCode(String rawCode) {
		if (rawCode == null || rawCode.isBlank()) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Maintenance location code is required.");
		}
		final String code = rawCode.trim();
		if (!Pattern.matches("^\\d{2,3}$", code)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Maintenance location code must be a 2-3 digit numeric reference."
			);
		}
		return code;
	}

	private String resolveLabel(
			com.axioma.quadras.domain.model.MaintenanceLocationType locationType,
			String code,
			String rawLabel
	) {
		final String label = rawLabel == null ? "" : rawLabel.trim();
		if (locationType != com.axioma.quadras.domain.model.MaintenanceLocationType.COMMON_AREA) {
			return label;
		}
		final String stripped = label.replaceFirst("^\\Q" + code + "\\E\\s*-\\s*", "").trim();
		return code + " - " + stripped;
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

	private Comparator<MaintenanceLocation> locationComparator() {
		return Comparator
				.comparingInt(this::locationCodeGroup)
				.thenComparingInt(this::locationNumericValue)
				.thenComparing(MaintenanceLocation::getCode, String.CASE_INSENSITIVE_ORDER)
				.thenComparing(MaintenanceLocation::getLabel, String.CASE_INSENSITIVE_ORDER);
	}

	private int locationCodeGroup(MaintenanceLocation location) {
		final String code = location.getCode() == null ? "" : location.getCode().trim();
		if (NUMERIC_CODE_PATTERN.matcher(code).matches()) {
			return code.length() <= 2 ? 1 : 0;
		}
		return 2;
	}

	private int locationNumericValue(MaintenanceLocation location) {
		final String code = location.getCode() == null ? "" : location.getCode().trim();
		if (!NUMERIC_CODE_PATTERN.matcher(code).matches()) {
			return Integer.MAX_VALUE;
		}
		return Integer.parseInt(code);
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
				"maintenance-location",
				entityId,
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(MaintenanceLocation location) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", location.getId());
		snapshot.put("locationType", location.getLocationType() == null ? null : location.getLocationType().name());
		snapshot.put("locationCategory", location.getLocationCategory() == null ? null : location.getLocationCategory().name());
		snapshot.put("code", location.getCode());
		snapshot.put("label", location.getLabel());
		snapshot.put("floor", location.getFloor());
		snapshot.put("description", location.getDescription());
		snapshot.put("active", location.isActive());
		snapshot.put("createdAt", toValue(location.getCreatedAt()));
		snapshot.put("updatedAt", toValue(location.getUpdatedAt()));
		snapshot.put("createdBy", location.getCreatedBy());
		snapshot.put("updatedBy", location.getUpdatedBy());
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
