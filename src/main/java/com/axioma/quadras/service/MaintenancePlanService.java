package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateMaintenancePlanDto;
import com.axioma.quadras.domain.dto.GenerateMaintenancePlanOrderDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.MaintenancePlanDto;
import com.axioma.quadras.domain.dto.UpdateMaintenancePlanDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderKind;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePlan;
import com.axioma.quadras.domain.model.MaintenancePlanRecurrenceUnit;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import com.axioma.quadras.repository.MaintenancePlanRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenancePlanService {

	private static final EnumSet<MaintenanceOrderStatus> ACTIVE_ORDER_STATUSES = EnumSet.of(
			MaintenanceOrderStatus.OPEN,
			MaintenanceOrderStatus.ASSIGNED,
			MaintenanceOrderStatus.SCHEDULED,
			MaintenanceOrderStatus.IN_PROGRESS
	);

	private final MaintenancePlanRepository maintenancePlanRepository;
	private final MaintenanceOrderRepository maintenanceOrderRepository;
	private final MaintenanceLocationService maintenanceLocationService;
	private final MaintenanceProviderService maintenanceProviderService;
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public MaintenancePlanService(
			MaintenancePlanRepository maintenancePlanRepository,
			MaintenanceOrderRepository maintenanceOrderRepository,
			MaintenanceLocationService maintenanceLocationService,
			MaintenanceProviderService maintenanceProviderService,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.maintenancePlanRepository = maintenancePlanRepository;
		this.maintenanceOrderRepository = maintenanceOrderRepository;
		this.maintenanceLocationService = maintenanceLocationService;
		this.maintenanceProviderService = maintenanceProviderService;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
	}

	public List<MaintenancePlanDto> list() {
		return maintenancePlanRepository.findAllDetailed().stream()
				.map(MaintenancePlanDto::from)
				.toList();
	}

	@Transactional
	public MaintenancePlanDto create(CreateMaintenancePlanDto input, String actorUsername) {
		final MaintenancePlan plan = maintenancePlanRepository.save(
				MaintenancePlan.create(
						requireActiveLocation(input.locationId()),
						requireActiveProvider(input.providerId()),
						input.title(),
						input.description(),
						resolveRecurrenceUnit(input.recurrenceUnit()),
						input.recurrenceInterval(),
						input.nextDueDate(),
						input.active() == null || input.active(),
						actorUsername
				)
		);
		auditTrailService.record(
				"maintenance",
				"maintenance-plan",
				plan.getId(),
				"CREATED",
				"Plan preventivo creado",
				List.of(),
				null,
				snapshot(plan)
		);
		publishCatalogEvent(plan.getId(), "plan-created");
		return MaintenancePlanDto.from(plan);
	}

	@Transactional
	public MaintenancePlanDto update(Long planId, UpdateMaintenancePlanDto input, String actorUsername) {
		final MaintenancePlan plan = findOrThrow(planId);
		final Map<String, Object> beforeState = snapshot(plan);
		plan.update(
				requireActiveLocation(input.locationId()),
				requireActiveProvider(input.providerId()),
				input.title(),
				input.description(),
				resolveRecurrenceUnit(input.recurrenceUnit()),
				input.recurrenceInterval(),
				input.nextDueDate(),
				Boolean.TRUE.equals(input.active()),
				actorUsername
		);
		recordAudit(planId, "UPDATED", "Plan preventivo actualizado", beforeState, snapshot(plan));
		publishCatalogEvent(plan.getId(), "plan-updated");
		return MaintenancePlanDto.from(plan);
	}

	@Transactional
	public MaintenanceOrderDto generateOrderFromPlan(
			Long planId,
			GenerateMaintenancePlanOrderDto input,
			String actorUsername,
			String actorRole
	) {
		final MaintenancePlan plan = findOrThrow(planId);
		if (!plan.isActive()) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Maintenance plan must be active.");
		}
		if (maintenancePlanRepository.existsActiveOrders(planId, ACTIVE_ORDER_STATUSES)) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance plan already has an active generated order."
			);
		}
		final LocalDateTime scheduledStartAt = input == null ? null : input.scheduledStartAt();
		final LocalDateTime scheduledEndAt = input == null ? null : input.scheduledEndAt();
		final MaintenanceOrder order = maintenanceOrderRepository.save(
				MaintenanceOrder.report(
						plan.getLocation(),
						plan.getProvider(),
						plan.getTitle(),
						plan.getDescription(),
						MaintenancePriority.MEDIUM,
						com.axioma.quadras.domain.model.MaintenanceRequestOrigin.INTERNAL_ROLE,
						false,
						null,
						null,
						MaintenanceBusinessPriority.INTERNAL_STANDARD,
						null,
						null,
						scheduledStartAt,
						scheduledEndAt,
						MaintenanceOrderKind.PREVENTIVE,
						plan,
						actorUsername,
						actorRole
				)
		);
		final Map<String, Object> beforePlanState = snapshot(plan);
		final LocalDate generationDate = scheduledStartAt == null ? LocalDate.now() : scheduledStartAt.toLocalDate();
		plan.markGenerated(generationDate);
		recordAudit(planId, "ORDER_GENERATED", "Orden preventiva generada desde plan", beforePlanState, snapshot(plan));
		auditTrailService.record(
				"maintenance",
				"maintenance-order",
				order.getId(),
				"CREATED_FROM_PLAN",
				"Orden preventiva generada desde plan",
				List.of(),
				null,
				Map.of(
						"planId", plan.getId(),
						"orderKind", order.getOrderKind().name(),
						"scheduledStartAt", String.valueOf(order.getScheduledStartAt()),
						"scheduledEndAt", String.valueOf(order.getScheduledEndAt())
				)
		);
		publishCatalogEvent(planId, "plan-order-generated");
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MAINTENANCE,
				"created-from-plan",
				order.getId(),
				order.getScheduledStartAt() == null ? LocalDate.now() : order.getScheduledStartAt().toLocalDate(),
				order.getScheduledStartAt() == null ? LocalDate.now() : order.getScheduledStartAt().toLocalDate()
		);
		return MaintenanceOrderDto.from(order);
	}

	public MaintenancePlan findOrThrow(Long planId) {
		return maintenancePlanRepository.findById(planId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Maintenance plan " + planId + " not found"
				));
	}

	private com.axioma.quadras.domain.model.MaintenanceLocation requireActiveLocation(Long locationId) {
		final com.axioma.quadras.domain.model.MaintenanceLocation location =
				maintenanceLocationService.findOrThrow(locationId);
		if (!location.isActive()) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Maintenance location must be active.");
		}
		return location;
	}

	private MaintenanceProvider requireActiveProvider(Long providerId) {
		if (providerId == null) {
			return null;
		}
		final MaintenanceProvider provider = maintenanceProviderService.findOrThrow(providerId);
		if (!provider.isActive()) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Maintenance provider must be active.");
		}
		return provider;
	}

	private MaintenancePlanRecurrenceUnit resolveRecurrenceUnit(MaintenancePlanRecurrenceUnit recurrenceUnit) {
		if (recurrenceUnit == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "recurrenceUnit is required");
		}
		return recurrenceUnit;
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
				"maintenance-plan",
				entityId,
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(MaintenancePlan plan) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", plan.getId());
		snapshot.put("locationId", plan.getLocation() == null ? null : plan.getLocation().getId());
		snapshot.put("providerId", plan.getProvider() == null ? null : plan.getProvider().getId());
		snapshot.put("title", plan.getTitle());
		snapshot.put("description", plan.getDescription());
		snapshot.put("recurrenceUnit", plan.getRecurrenceUnit() == null ? null : plan.getRecurrenceUnit().name());
		snapshot.put("recurrenceInterval", plan.getRecurrenceInterval());
		snapshot.put("nextDueDate", toValue(plan.getNextDueDate()));
		snapshot.put("lastGeneratedOn", toValue(plan.getLastGeneratedOn()));
		snapshot.put("active", plan.isActive());
		snapshot.put("createdAt", toValue(plan.getCreatedAt()));
		snapshot.put("updatedAt", toValue(plan.getUpdatedAt()));
		snapshot.put("createdBy", plan.getCreatedBy());
		snapshot.put("updatedBy", plan.getUpdatedBy());
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
