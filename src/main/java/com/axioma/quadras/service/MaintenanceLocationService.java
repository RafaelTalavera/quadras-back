package com.axioma.quadras.service;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceLocationService {

	private final MaintenanceLocationRepository maintenanceLocationRepository;
	private final MaintenanceOrderRepository maintenanceOrderRepository;
	private final MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository;

	public MaintenanceLocationService(
			MaintenanceLocationRepository maintenanceLocationRepository,
			MaintenanceOrderRepository maintenanceOrderRepository,
			MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository
	) {
		this.maintenanceLocationRepository = maintenanceLocationRepository;
		this.maintenanceOrderRepository = maintenanceOrderRepository;
		this.maintenanceOrderAttachmentRepository = maintenanceOrderAttachmentRepository;
	}

	public List<MaintenanceLocationDto> list() {
		return maintenanceLocationRepository.findAllProjectedByOrderByLocationTypeAscCodeAsc().stream()
				.map(MaintenanceLocationDto::from)
				.toList();
	}

	@Transactional
	public MaintenanceLocationDto create(CreateMaintenanceLocationDto input, String actorUsername) {
		validateUniqueCode(input.locationType(), input.code(), null);
		final MaintenanceLocation location = maintenanceLocationRepository.save(
				MaintenanceLocation.create(
						input.locationType(),
						input.locationCategory(),
						input.code(),
						input.label(),
						input.floor(),
						input.description(),
						input.active() == null || input.active(),
						actorUsername
				)
		);
		return MaintenanceLocationDto.from(location);
	}

	@Transactional
	public MaintenanceLocationDto update(
			Long locationId,
			UpdateMaintenanceLocationDto input,
			String actorUsername
	) {
		final MaintenanceLocation location = findOrThrow(locationId);
		validateUniqueCode(input.locationType(), input.code(), locationId);
		location.update(
				input.locationType(),
				input.locationCategory(),
				input.code(),
				input.label(),
				input.floor(),
				input.description(),
				input.active(),
				actorUsername
		);
		return MaintenanceLocationDto.from(location);
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
}
