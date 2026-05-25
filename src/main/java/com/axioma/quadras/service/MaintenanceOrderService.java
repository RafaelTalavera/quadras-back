package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AddMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CancelMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.CompleteMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.CreateMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.MaintenanceConflictDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderListDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderListPageDto;
import com.axioma.quadras.domain.dto.StartMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenancePaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderAttachment;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
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
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceOrderService {

	private static final int MAX_ATTACHMENT_BYTES = 8 * 1024 * 1024;
	private static final int DEFAULT_COMPACT_PAGE_SIZE = 25;
	private static final int MAX_COMPACT_PAGE_SIZE = 100;
	private static final MaintenancePriority DEFAULT_FORM_PRIORITY = MaintenancePriority.MEDIUM;
	private static final EnumSet<MaintenanceOrderStatus> ACTIVE_CONFLICT_STATUSES = EnumSet.of(
			MaintenanceOrderStatus.SCHEDULED,
			MaintenanceOrderStatus.IN_PROGRESS
	);

	private final MaintenanceOrderRepository maintenanceOrderRepository;
	private final MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository;
	private final MaintenanceLocationService maintenanceLocationService;
	private final MaintenanceProviderService maintenanceProviderService;
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public MaintenanceOrderService(
			MaintenanceOrderRepository maintenanceOrderRepository,
			MaintenanceOrderAttachmentRepository maintenanceOrderAttachmentRepository,
			MaintenanceLocationService maintenanceLocationService,
			MaintenanceProviderService maintenanceProviderService,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.maintenanceOrderRepository = maintenanceOrderRepository;
		this.maintenanceOrderAttachmentRepository = maintenanceOrderAttachmentRepository;
		this.maintenanceLocationService = maintenanceLocationService;
		this.maintenanceProviderService = maintenanceProviderService;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
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

	public MaintenanceOrderListPageDto listCompact(
			LocalDate dateFrom,
			LocalDate dateTo,
			Long locationId,
			Long providerId,
			com.axioma.quadras.domain.model.MaintenanceProviderType providerType,
			MaintenanceOrderStatus status,
			com.axioma.quadras.domain.model.MaintenancePriority priority,
			Integer page,
			Integer size
	) {
		validateDateRange(dateFrom, dateTo);
		final int safePage = normalizePage(page);
		final int safeSize = normalizePageSize(size);
		final var items = maintenanceOrderRepository.findCompactItems(
				locationId,
				providerId,
				providerType,
				status,
				priority,
				scheduledFrom(dateFrom),
				scheduledToExclusive(dateTo),
				reportedFrom(dateFrom),
				reportedToExclusive(dateTo),
				PageRequest.of(safePage, safeSize)
		);
		return new MaintenanceOrderListPageDto(
				safePage,
				safeSize,
				items.hasNext(),
				items.getContent().stream().map(MaintenanceOrderListDto::from).toList()
		);
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
						DEFAULT_FORM_PRIORITY,
						input.requestOrigin(),
						input.requestedForGuest() != null && input.requestedForGuest(),
						input.guestName(),
						input.guestReference(),
						input.businessPriority(),
						null,
						null,
						input.scheduledStartAt(),
						input.scheduledEndAt(),
						actorUsername,
						actorRole
				)
		);
		auditTrailService.record(
				"maintenance",
				"maintenance-order",
				order.getId(),
				"CREATED",
				"Orden de mantenimiento creada",
				List.of(),
				null,
				snapshot(order)
		);
		publishOrderEvent(order, "created");
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto update(Long orderId, UpdateMaintenanceOrderDto input, String actorUsername) {
		final MaintenanceOrder order = findOrThrow(orderId);
		final Map<String, Object> beforeState = snapshot(order);
		final LocalDate previousReferenceDate = referenceDate(order);
		order.update(
				requireActiveLocation(input.locationId()),
				requireActiveProvider(input.providerId()),
				input.title(),
				input.description(),
				order.getPriority(),
				input.requestOrigin(),
				input.requestedForGuest() != null && input.requestedForGuest(),
				input.guestName(),
				input.guestReference(),
				input.businessPriority(),
				order.getEstimatedExecutionMinutes(),
				order.getAssignedUsername(),
				input.scheduledStartAt(),
				input.scheduledEndAt(),
				actorUsername
		);
		recordAudit("UPDATED", "Orden de mantenimiento actualizada", order, beforeState);
		publishOrderEvent(order, "updated", previousReferenceDate);
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
		final Map<String, Object> beforeState = snapshot(order);
		order.start(input == null ? null : input.startedAt(), actorUsername);
		recordAudit("STARTED", "Orden de mantenimiento iniciada", order, beforeState);
		publishOrderEvent(order, "started");
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto updatePayment(
			Long orderId,
			UpdateMaintenancePaymentDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		final Map<String, Object> beforeState = snapshot(order);
		order.markPayment(
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				actorUsername
		);
		recordAudit("PAYMENT_UPDATED", "Pago de orden de mantenimiento actualizado", order, beforeState);
		publishOrderEvent(order, "payment-updated");
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto complete(
			Long orderId,
			CompleteMaintenanceOrderDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		final Map<String, Object> beforeState = snapshot(order);
		order.complete(input.completedAt(), input.resolutionNotes(), actorUsername);
		recordAudit("COMPLETED", "Orden de mantenimiento completada", order, beforeState);
		publishOrderEvent(order, "completed");
		return toDto(order);
	}

	@Transactional
	public MaintenanceOrderDto cancel(
			Long orderId,
			CancelMaintenanceOrderDto input,
			String actorUsername
	) {
		final MaintenanceOrder order = findOrThrow(orderId);
		final Map<String, Object> beforeState = snapshot(order);
		order.cancel(input.cancellationNotes(), actorUsername);
		recordAudit("CANCELLED", "Orden de mantenimiento cancelada", order, beforeState);
		publishOrderEvent(order, "cancelled");
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
		final Map<String, Object> beforeState = snapshot(order);
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
		final Map<String, Object> afterState = snapshot(order);
		auditTrailService.record(
				"maintenance",
				"maintenance-order",
				order.getId(),
				"ATTACHMENT_ADDED",
				"Anexo agregado a orden de mantenimiento",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		publishOrderEvent(order, "attachment-added");
		return MaintenanceOrderAttachmentDto.from(savedAttachment);
	}

	@Transactional
	public void deleteAttachment(Long orderId, Long attachmentId) {
		final MaintenanceOrder order = findOrThrow(orderId);
		final Map<String, Object> beforeState = snapshot(order);
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
		order.getAttachments().removeIf(item -> Objects.equals(item.getId(), attachmentId));
		final Map<String, Object> afterState = snapshot(order);
		auditTrailService.record(
				"maintenance",
				"maintenance-order",
				order.getId(),
				"ATTACHMENT_DELETED",
				"Anexo eliminado de orden de mantenimiento",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		publishOrderEvent(order, "attachment-deleted");
	}

	public List<AuditEventDto> audit(Long orderId) {
		findOrThrow(orderId);
		return auditTrailService.findByEntity("maintenance-order", orderId);
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

	private int normalizePage(Integer page) {
		if (page == null) {
			return 0;
		}
		if (page < 0) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "page must be greater than or equal to zero");
		}
		return page;
	}

	private int normalizePageSize(Integer size) {
		if (size == null) {
			return DEFAULT_COMPACT_PAGE_SIZE;
		}
		if (size < 1) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "size must be greater than zero");
		}
		if (size > MAX_COMPACT_PAGE_SIZE) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"size must be less than or equal to " + MAX_COMPACT_PAGE_SIZE
			);
		}
		return size;
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

	private void publishOrderEvent(MaintenanceOrder order, String action) {
		final LocalDate referenceDate = referenceDate(order);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MAINTENANCE,
				action,
				order.getId(),
				referenceDate,
				referenceDate
		);
	}

	private void publishOrderEvent(MaintenanceOrder order, String action, LocalDate previousReferenceDate) {
		final LocalDate currentReferenceDate = referenceDate(order);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MAINTENANCE,
				action,
				order.getId(),
				minDate(previousReferenceDate, currentReferenceDate),
				maxDate(previousReferenceDate, currentReferenceDate)
		);
	}

	private LocalDate referenceDate(MaintenanceOrder order) {
		if (order.getScheduledStartAt() != null) {
			return order.getScheduledStartAt().toLocalDate();
		}
		return order.getReportedAt().toLocalDate();
	}

	private LocalDate minDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? right : left;
	}

	private LocalDate maxDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? left : right;
	}

	private void recordAudit(
			String actionName,
			String summaryText,
			MaintenanceOrder order,
			Map<String, Object> beforeState
	) {
		final Map<String, Object> afterState = snapshot(order);
		auditTrailService.record(
				"maintenance",
				"maintenance-order",
				order.getId(),
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(MaintenanceOrder order) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", order.getId());
		snapshot.put("locationId", order.getLocation() == null ? null : order.getLocation().getId());
		snapshot.put("locationLabel", order.getLocation() == null ? null : order.getLocation().getLabel());
		snapshot.put("providerId", order.getProvider() == null ? null : order.getProvider().getId());
		snapshot.put("providerName", order.getProviderNameSnapshot());
		snapshot.put("providerType", order.getProviderTypeSnapshot() == null ? null : order.getProviderTypeSnapshot().name());
		snapshot.put("status", order.getStatus() == null ? null : order.getStatus().name());
		snapshot.put("priority", order.getPriority() == null ? null : order.getPriority().name());
		snapshot.put("title", order.getTitle());
		snapshot.put("description", order.getDescription());
		snapshot.put("requestOrigin", order.getRequestOrigin());
		snapshot.put("requestedForGuest", order.isRequestedForGuest());
		snapshot.put("guestName", order.getGuestName());
		snapshot.put("guestReference", order.getGuestReference());
		snapshot.put("businessPriority", order.getBusinessPriority());
		snapshot.put("estimatedExecutionMinutes", order.getEstimatedExecutionMinutes());
		snapshot.put("assignedUsername", order.getAssignedUsername());
		snapshot.put("scheduledStartAt", toValue(order.getScheduledStartAt()));
		snapshot.put("scheduledEndAt", toValue(order.getScheduledEndAt()));
		snapshot.put("reportedAt", toValue(order.getReportedAt()));
		snapshot.put("startedAt", toValue(order.getStartedAt()));
		snapshot.put("completedAt", toValue(order.getCompletedAt()));
		snapshot.put("resolutionNotes", order.getResolutionNotes());
		snapshot.put("cancellationNotes", order.getCancellationNotes());
		snapshot.put("paymentMethod", order.getPaymentMethod() == null ? null : order.getPaymentMethod().name());
		snapshot.put("paymentDate", toValue(order.getPaymentDate()));
		snapshot.put("paymentNotes", order.getPaymentNotes());
		snapshot.put("createdAt", toValue(order.getCreatedAt()));
		snapshot.put("updatedAt", toValue(order.getUpdatedAt()));
		snapshot.put("cancelledAt", toValue(order.getCancelledAt()));
		snapshot.put("createdBy", order.getCreatedBy());
		snapshot.put("updatedBy", order.getUpdatedBy());
		snapshot.put("cancelledBy", order.getCancelledBy());
		snapshot.put("attachments", order.getAttachments().stream().map(attachment -> {
			final Map<String, Object> item = new LinkedHashMap<>();
			item.put("id", attachment.getId());
			item.put("attachmentType", attachment.getAttachmentType() == null ? null : attachment.getAttachmentType().name());
			item.put("fileName", attachment.getFileName());
			item.put("contentType", attachment.getContentType());
			item.put("fileSize", attachment.getFileSize());
			item.put("createdAt", toValue(attachment.getCreatedAt()));
			item.put("createdBy", attachment.getCreatedBy());
			return item;
		}).toList());
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
