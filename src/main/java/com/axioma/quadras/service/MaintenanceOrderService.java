package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AddMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.CancelMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.CompleteMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.CreateMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.MaintenanceConflictDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.StartMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenancePaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderAttachment;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentMetadataView;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentRepository;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceOrderService {

	private static final int MAX_ATTACHMENT_BYTES = 8 * 1024 * 1024;
	private static final EnumSet<MaintenanceOrderStatus> ACTIVE_CONFLICT_STATUSES = EnumSet.of(
			MaintenanceOrderStatus.SCHEDULED,
			MaintenanceOrderStatus.IN_PROGRESS
	);

	private final MaintenanceOrderRepository maintenanceOrderRepository;
	private final MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository;
	private final MaintenanceLocationService maintenanceLocationService;
	private final MaintenanceProviderService maintenanceProviderService;

	public MaintenanceOrderService(
			MaintenanceOrderRepository maintenanceOrderRepository,
			MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository,
			MaintenanceLocationService maintenanceLocationService,
			MaintenanceProviderService maintenanceProviderService
	) {
		this.maintenanceOrderRepository = maintenanceOrderRepository;
		this.maintenanceOrderAttachmentRepository = maintenanceOrderAttachmentRepository;
		this.maintenanceLocationService = maintenanceLocationService;
		this.maintenanceProviderService = maintenanceProviderService;
	}

	public List<MaintenanceOrderDto> list(
			LocalDate dateFrom,
			LocalDate dateTo,
			Long locationId,
			Long providerId,
			com.axioma.quadras.domain.model.MaintenanceProviderType providerType,
			MaintenanceOrderStatus status,
			com.axioma.quadras.domain.model.MaintenancePriority priority
	) {
		validateDateRange(dateFrom, dateTo);
		final var items = maintenanceOrderRepository.findFilteredItems(
				locationId,
				providerId,
				providerType,
				status,
				priority,
				scheduledFrom(dateFrom),
				scheduledToExclusive(dateTo),
				reportedFrom(dateFrom),
				reportedToExclusive(dateTo)
		);
		final Map<Long, List<MaintenanceOrderAttachmentDto>> attachmentsByOrderId =
				loadAttachmentMetadataByOrderId(items.stream().map(item -> item.getId()).toList());
		return items.stream()
				.sorted(orderComparator())
				.map(item -> MaintenanceOrderDto.from(
						item,
						attachmentsByOrderId.getOrDefault(item.getId(), List.of())
				))
				.toList();
	}

	@Transactional
	public MaintenanceOrderDto create(
			CreateMaintenanceOrderDto input,
			String actorUsername,
			String actorRole
	) {
		final MaintenanceOrder order = maintenanceOrderRepository.save(
				MaintenanceOrder.report(
						requireActiveLocation(input.locationId()),
						requireActiveProvider(input.providerId()),
						input.title(),
						input.description(),
						input.priority(),
						input.requestOrigin(),
						input.requestedForGuest() != null && input.requestedForGuest(),
						input.guestName(),
						input.guestReference(),
						input.businessPriority(),
						input.estimatedExecutionMinutes(),
						input.assignedUsername(),
						input.scheduledStartAt(),
						input.scheduledEndAt(),
						actorUsername,
						actorRole
				)
		);
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto update(Long orderId, UpdateMaintenanceOrderDto input, String actorUsername) {
		final MaintenanceOrder order = findOrThrow(orderId);
		order.update(
				requireActiveLocation(input.locationId()),
				requireActiveProvider(input.providerId()),
				input.title(),
				input.description(),
				input.priority(),
				input.requestOrigin(),
				input.requestedForGuest() != null && input.requestedForGuest(),
				input.guestName(),
				input.guestReference(),
				input.businessPriority(),
				input.estimatedExecutionMinutes(),
				input.assignedUsername(),
				input.scheduledStartAt(),
				input.scheduledEndAt(),
				actorUsername
		);
		return toDto(order);
	}

	public List<MaintenanceConflictDto> findConflicts(
			Long locationId,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt,
			Long excludeOrderId
	) {
		if (locationId == null || scheduledStartAt == null || scheduledEndAt == null) {
			return List.of();
		}
		if (!scheduledStartAt.isBefore(scheduledEndAt)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"scheduledStartAt must be before scheduledEndAt"
			);
		}
		return maintenanceOrderRepository.findScheduleConflicts(
						locationId,
						scheduledStartAt,
						scheduledEndAt,
						excludeOrderId,
						ACTIVE_CONFLICT_STATUSES
				).stream()
				.map(MaintenanceConflictDto::from)
				.toList();
	}

	@Transactional
	public MaintenanceOrderDto start(
			Long orderId,
			StartMaintenanceOrderDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		order.start(input == null ? null : input.startedAt(), actorUsername);
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto updatePayment(
			Long orderId,
			UpdateMaintenancePaymentDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		order.markPayment(
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				actorUsername
		);
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto complete(
			Long orderId,
			CompleteMaintenanceOrderDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		order.complete(input.completedAt(), input.resolutionNotes(), actorUsername);
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto cancel(
			Long orderId,
			CancelMaintenanceOrderDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		order.cancel(input.cancellationNotes(), actorUsername);
		return toDto(order);
	}

	public List<MaintenanceOrderAttachmentDto> listAttachments(Long orderId) {
		findOrThrow(orderId);
		return maintenanceOrderAttachmentRepository.findMetadataByOrderIdOrderByCreatedAtDesc(orderId).stream()
				.map(this::toAttachmentDto)
				.toList();
	}

	@Transactional
	public MaintenanceOrderAttachmentDto addAttachment(
			Long orderId,
			AddMaintenanceAttachmentDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		final byte[] content = decodeBase64(input.base64Content());
		final MaintenanceOrderAttachment attachment = MaintenanceOrderAttachment.create(
				input.attachmentType(),
				input.fileName(),
				input.contentType(),
				content,
				actorUsername
		);
		order.addAttachment(attachment);
		final MaintenanceOrderAttachment savedAttachment =
				maintenanceOrderAttachmentRepository.save(attachment);
		return MaintenanceOrderAttachmentDto.from(savedAttachment);
	}

	@Transactional
	public void deleteAttachment(Long orderId, Long attachmentId) {
		findOrThrow(orderId);
		final MaintenanceOrderAttachment attachment = maintenanceOrderAttachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Maintenance attachment " + attachmentId + " not found"
				));
		if (!attachment.getOrder().getId().equals(orderId)) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance attachment does not belong to the selected order."
			);
		}
		maintenanceOrderAttachmentRepository.delete(attachment);
	}

	public MaintenanceOrder findOrThrow(Long orderId) {
		return maintenanceOrderRepository.findById(orderId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Maintenance order " + orderId + " not found"
				));
	}

	private Comparator<com.axioma.quadras.repository.MaintenanceOrderHistoryItemView> orderComparator() {
		return Comparator
				.comparing(this::referenceDateTime)
				.thenComparing(com.axioma.quadras.repository.MaintenanceOrderHistoryItemView::getId);
	}

	private LocalDateTime referenceDateTime(
			com.axioma.quadras.repository.MaintenanceOrderHistoryItemView order
	) {
		if (order.getScheduledStartAt() != null) {
			return order.getScheduledStartAt();
		}
		return order.getReportedAt().toLocalDateTime();
	}

	private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"dateFrom must be before or equal to dateTo"
			);
		}
	}

	private LocalDateTime scheduledFrom(LocalDate dateFrom) {
		return dateFrom == null ? null : dateFrom.atStartOfDay();
	}

	private LocalDateTime scheduledToExclusive(LocalDate dateTo) {
		return dateTo == null ? null : dateTo.plusDays(1).atStartOfDay();
	}

	private OffsetDateTime reportedFrom(LocalDate dateFrom) {
		return dateFrom == null ? null : dateFrom.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
	}

	private OffsetDateTime reportedToExclusive(LocalDate dateTo) {
		return dateTo == null ? null : dateTo.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
	}

	private com.axioma.quadras.domain.model.MaintenanceLocation requireActiveLocation(Long locationId) {
		final com.axioma.quadras.domain.model.MaintenanceLocation location =
				maintenanceLocationService.findOrThrow(locationId);
		if (!location.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance location must be active."
			);
		}
		return location;
	}

	private MaintenanceProvider requireActiveProvider(Long providerId) {
		if (providerId == null) {
			return null;
		}
		final MaintenanceProvider provider = maintenanceProviderService.findOrThrow(providerId);
		if (!provider.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance provider must be active."
			);
		}
		return provider;
	}

	private byte[] decodeBase64(String base64Content) {
		try {
			final byte[] decoded = Base64.getDecoder().decode(base64Content);
			if (decoded.length == 0) {
				throw new ApplicationException(
						HttpStatus.BAD_REQUEST,
						"Attachment content cannot be empty."
				);
			}
			if (decoded.length > MAX_ATTACHMENT_BYTES) {
				throw new ApplicationException(
						HttpStatus.BAD_REQUEST,
						"Attachment exceeds the maximum allowed size."
				);
			}
			return decoded;
		} catch (IllegalArgumentException error) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Attachment content is not valid base64.");
		}
	}

	private MaintenanceOrderDto toDto(MaintenanceOrder order) {
		return MaintenanceOrderDto.from(
				order,
				loadAttachmentMetadataByOrderId(List.of(order.getId())).getOrDefault(order.getId(), List.of())
		);
	}

	private Map<Long, List<MaintenanceOrderAttachmentDto>> loadAttachmentMetadataByOrderId(
			Collection<Long> orderIds
	) {
		if (orderIds == null || orderIds.isEmpty()) {
			return Map.of();
		}
		final Map<Long, List<MaintenanceOrderAttachmentDto>> attachmentsByOrderId = new LinkedHashMap<>();
		for (final MaintenanceOrderAttachmentMetadataView metadata :
				maintenanceOrderAttachmentRepository.findMetadataByOrderIdInOrderByCreatedAtDesc(orderIds)) {
			attachmentsByOrderId.computeIfAbsent(metadata.getOrderId(), ignored -> new java.util.ArrayList<>())
					.add(toAttachmentDto(metadata));
		}
		return attachmentsByOrderId;
	}

	private MaintenanceOrderAttachmentDto toAttachmentDto(
			MaintenanceOrderAttachmentMetadataView metadata
	) {
		return MaintenanceOrderAttachmentDto.from(metadata);
	}
}
