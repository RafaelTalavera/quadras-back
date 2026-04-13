package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.MaintenanceSummaryBreakdownDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryDetailDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryDetailItemDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryReportDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceSummaryGroupBy;
import com.axioma.quadras.repository.MaintenanceSummaryAggregateView;
import com.axioma.quadras.repository.MaintenanceSummaryBreakdownView;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import com.axioma.quadras.repository.MaintenanceSummaryDetailItemView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceReportService {

	private static final long MAX_REPORT_DAYS = 93;

	private final MaintenanceOrderRepository maintenanceOrderRepository;

	public MaintenanceReportService(MaintenanceOrderRepository maintenanceOrderRepository) {
		this.maintenanceOrderRepository = maintenanceOrderRepository;
	}

	public MaintenanceSummaryReportDto summary(LocalDate dateFrom, LocalDate dateTo) {
		validateRange(dateFrom, dateTo);
		final LocalDateTime scheduledFrom = scheduledFrom(dateFrom);
		final LocalDateTime scheduledToExclusive = scheduledToExclusive(dateTo);
		final java.time.OffsetDateTime reportedFrom = reportedFrom(dateFrom);
		final java.time.OffsetDateTime reportedToExclusive = reportedToExclusive(dateTo);
		final MaintenanceSummaryAggregateView aggregate = maintenanceOrderRepository.findSummaryAggregate(
				scheduledFrom,
				scheduledToExclusive,
				reportedFrom,
				reportedToExclusive
		);

		return new MaintenanceSummaryReportDto(
				Math.toIntExact(aggregate.getOpenCount()),
				Math.toIntExact(aggregate.getAssignedCount()),
				Math.toIntExact(aggregate.getScheduledCount()),
				Math.toIntExact(aggregate.getInProgressCount()),
				Math.toIntExact(aggregate.getCompletedCount()),
				Math.toIntExact(aggregate.getCancelledCount()),
				Math.toIntExact(aggregate.getInternalCount()),
				Math.toIntExact(aggregate.getExternalCount()),
				Math.toIntExact(aggregate.getUnassignedCount()),
				Math.toIntExact(aggregate.getRoomsCount()),
				Math.toIntExact(aggregate.getCommonAreasCount()),
				Math.toIntExact(aggregate.getUrgentCount()),
				Math.toIntExact(aggregate.getGuestPriorityCount()),
				toHours(aggregate.getAverageResolutionMinutes()),
				maintenanceOrderRepository.findProviderSummaryBreakdown(
						scheduledFrom,
						scheduledToExclusive,
						reportedFrom,
						reportedToExclusive
				).stream().map(this::toBreakdownDto).toList(),
				buildProviderTypeBreakdown(scheduledFrom, scheduledToExclusive, reportedFrom, reportedToExclusive),
				buildLocationTypeBreakdown(scheduledFrom, scheduledToExclusive, reportedFrom, reportedToExclusive),
				buildStatusBreakdown(scheduledFrom, scheduledToExclusive, reportedFrom, reportedToExclusive)
		);
	}

	public MaintenanceSummaryDetailDto summaryDetails(
			MaintenanceSummaryGroupBy groupBy,
			String code,
			LocalDate dateFrom,
			LocalDate dateTo
	) {
		validateRange(dateFrom, dateTo);
		if (groupBy == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "groupBy is required");
		}
		if (code == null || code.isBlank()) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "code is required");
		}
		final DetailFilter filter = resolveDetailFilter(groupBy, code);
		final List<MaintenanceSummaryDetailItemView> orders = filteredDetailItems(dateFrom, dateTo, filter).stream()
				.sorted(Comparator.comparing(this::referenceDateTime))
				.toList();
		if (orders.isEmpty()) {
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Maintenance summary group not found.");
		}
		final MaintenanceSummaryDetailItemView firstOrder = orders.get(0);
		return new MaintenanceSummaryDetailDto(
				groupBy,
				code,
				labelForGroup(firstOrder, groupBy),
				toBreakdown(code, labelForGroup(firstOrder, groupBy), orders),
				orders.stream().map(this::toDetailItemDto).toList()
		);
	}

	private List<MaintenanceSummaryDetailItemView> filteredDetailItems(
			LocalDate dateFrom,
			LocalDate dateTo,
			DetailFilter filter
	) {
		return maintenanceOrderRepository.findSummaryDetailItems(
				filter.providerId(),
				filter.providerUnassigned(),
				filter.providerType(),
				filter.providerTypeUnassigned(),
				filter.locationType(),
				filter.status(),
				scheduledFrom(dateFrom),
				scheduledToExclusive(dateTo),
				reportedFrom(dateFrom),
				reportedToExclusive(dateTo)
		);
	}

	private LocalDateTime referenceDateTime(MaintenanceSummaryDetailItemView order) {
		return order.getScheduledStartAt() != null
				? order.getScheduledStartAt()
				: order.getReportedAt().toLocalDateTime();
	}

	private MaintenanceSummaryBreakdownDto toBreakdown(
			String code,
			String label,
			List<MaintenanceSummaryDetailItemView> orders
	) {
		final BreakdownAccumulator accumulator = new BreakdownAccumulator(label);
		orders.forEach(accumulator::record);
		return accumulator.toDto(code);
	}

	private List<MaintenanceSummaryBreakdownDto> buildProviderTypeBreakdown(
			LocalDateTime scheduledFrom,
			LocalDateTime scheduledToExclusive,
			java.time.OffsetDateTime reportedFrom,
			java.time.OffsetDateTime reportedToExclusive
	) {
		final Map<String, MaintenanceSummaryBreakdownView> breakdownByCode = indexByCode(
				maintenanceOrderRepository.findProviderTypeSummaryBreakdown(
						scheduledFrom,
						scheduledToExclusive,
						reportedFrom,
						reportedToExclusive
				)
		);
		final List<MaintenanceSummaryBreakdownDto> breakdown = new java.util.ArrayList<>();
		for (final MaintenanceProviderType value : MaintenanceProviderType.values()) {
			final MaintenanceSummaryBreakdownView view = breakdownByCode.get(value.name());
			if (view != null) {
				breakdown.add(toBreakdownDto(view));
			}
		}
		final MaintenanceSummaryBreakdownView unassigned = breakdownByCode.get("UNASSIGNED");
		if (unassigned != null) {
			breakdown.add(toBreakdownDto(unassigned));
		}
		return breakdown;
	}

	private List<MaintenanceSummaryBreakdownDto> buildLocationTypeBreakdown(
			LocalDateTime scheduledFrom,
			LocalDateTime scheduledToExclusive,
			java.time.OffsetDateTime reportedFrom,
			java.time.OffsetDateTime reportedToExclusive
	) {
		final Map<String, MaintenanceSummaryBreakdownView> breakdownByCode = indexByCode(
				maintenanceOrderRepository.findLocationTypeSummaryBreakdown(
						scheduledFrom,
						scheduledToExclusive,
						reportedFrom,
						reportedToExclusive
				)
		);
		final List<MaintenanceSummaryBreakdownDto> breakdown = new java.util.ArrayList<>();
		for (final MaintenanceLocationType value : MaintenanceLocationType.values()) {
			final MaintenanceSummaryBreakdownView view = breakdownByCode.get(value.name());
			if (view != null) {
				breakdown.add(toBreakdownDto(view));
			}
		}
		return breakdown;
	}

	private List<MaintenanceSummaryBreakdownDto> buildStatusBreakdown(
			LocalDateTime scheduledFrom,
			LocalDateTime scheduledToExclusive,
			java.time.OffsetDateTime reportedFrom,
			java.time.OffsetDateTime reportedToExclusive
	) {
		final Map<String, MaintenanceSummaryBreakdownView> breakdownByCode = indexByCode(
				maintenanceOrderRepository.findStatusSummaryBreakdown(
						scheduledFrom,
						scheduledToExclusive,
						reportedFrom,
						reportedToExclusive
				)
		);
		final List<MaintenanceSummaryBreakdownDto> breakdown = new java.util.ArrayList<>();
		for (final MaintenanceOrderStatus value : MaintenanceOrderStatus.values()) {
			final MaintenanceSummaryBreakdownView view = breakdownByCode.get(value.name());
			if (view != null) {
				breakdown.add(toBreakdownDto(view));
			}
		}
		return breakdown;
	}

	private Map<String, MaintenanceSummaryBreakdownView> indexByCode(List<MaintenanceSummaryBreakdownView> views) {
		final Map<String, MaintenanceSummaryBreakdownView> indexed = new LinkedHashMap<>();
		for (final MaintenanceSummaryBreakdownView view : views) {
			indexed.put(view.getCode(), view);
		}
		return indexed;
	}

	private MaintenanceSummaryBreakdownDto toBreakdownDto(MaintenanceSummaryBreakdownView view) {
		return new MaintenanceSummaryBreakdownDto(
				view.getCode(),
				view.getLabel(),
				Math.toIntExact(view.getOpenCount()),
				Math.toIntExact(view.getScheduledCount()),
				Math.toIntExact(view.getInProgressCount()),
				Math.toIntExact(view.getCompletedCount()),
				Math.toIntExact(view.getCancelledCount()),
				Math.toIntExact(view.getUrgentCount())
		);
	}

	private String labelForGroup(
			MaintenanceSummaryDetailItemView order,
			MaintenanceSummaryGroupBy groupBy
	) {
		return switch (groupBy) {
			case PROVIDER -> order.getProviderNameSnapshot() == null
					? "Sem responsavel"
					: order.getProviderNameSnapshot();
			case PROVIDER_TYPE -> labelForProviderType(order.getProviderTypeSnapshot());
			case LOCATION_TYPE -> labelForLocationType(order.getLocationTypeSnapshot());
			case STATUS -> labelForStatus(order.getStatus());
		};
	}

	private DetailFilter resolveDetailFilter(MaintenanceSummaryGroupBy groupBy, String code) {
		return switch (groupBy) {
			case PROVIDER -> resolveProviderDetailFilter(code);
			case PROVIDER_TYPE -> resolveProviderTypeDetailFilter(code);
			case LOCATION_TYPE -> resolveLocationTypeDetailFilter(code);
			case STATUS -> resolveStatusDetailFilter(code);
		};
	}

	private DetailFilter resolveProviderDetailFilter(String code) {
		if ("UNASSIGNED".equals(code)) {
			return new DetailFilter(null, true, null, false, null, null);
		}
		try {
			return new DetailFilter(Long.valueOf(code), false, null, false, null, null);
		} catch (NumberFormatException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "provider code must be a number");
		}
	}

	private DetailFilter resolveProviderTypeDetailFilter(String code) {
		if ("UNASSIGNED".equals(code)) {
			return new DetailFilter(null, false, null, true, null, null);
		}
		try {
			return new DetailFilter(
					null,
					false,
					MaintenanceProviderType.valueOf(code.trim().toUpperCase()),
					false,
					null,
					null
			);
		} catch (IllegalArgumentException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Unknown providerType code: " + code);
		}
	}

	private DetailFilter resolveLocationTypeDetailFilter(String code) {
		try {
			return new DetailFilter(
					null,
					false,
					null,
					false,
					MaintenanceLocationType.valueOf(code.trim().toUpperCase()),
					null
			);
		} catch (IllegalArgumentException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Unknown locationType code: " + code);
		}
	}

	private DetailFilter resolveStatusDetailFilter(String code) {
		try {
			return new DetailFilter(
					null,
					false,
					null,
					false,
					null,
					MaintenanceOrderStatus.valueOf(code.trim().toUpperCase())
			);
		} catch (IllegalArgumentException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Unknown status code: " + code);
		}
	}

	private MaintenanceSummaryDetailItemDto toDetailItemDto(MaintenanceSummaryDetailItemView order) {
		return new MaintenanceSummaryDetailItemDto(
				order.getOrderId(),
				order.getLocationTypeSnapshot(),
				order.getLocationLabelSnapshot(),
				order.getProviderTypeSnapshot(),
				order.getProviderNameSnapshot(),
				order.getServiceLabelSnapshot(),
				order.getTitle(),
				order.getPriority(),
				order.getBusinessPriority(),
				order.getRequestOrigin(),
				Boolean.TRUE.equals(order.getRequestedForGuest()),
				order.getAssignedUsername(),
				order.getEstimatedExecutionMinutes(),
				order.getStatus(),
				order.getReportedAt(),
				expectedCompletionAt(order),
				order.getScheduledStartAt(),
				order.getScheduledEndAt(),
				order.getStartedAt(),
				order.getCompletedAt()
		);
	}

	private LocalDateTime expectedCompletionAt(MaintenanceSummaryDetailItemView order) {
		if (order.getScheduledEndAt() != null) {
			return order.getScheduledEndAt();
		}
		if (order.getEstimatedExecutionMinutes() == null) {
			return null;
		}
		if (order.getStartedAt() != null) {
			return order.getStartedAt().toLocalDateTime().plusMinutes(order.getEstimatedExecutionMinutes());
		}
		return order.getReportedAt().toLocalDateTime().plusMinutes(order.getEstimatedExecutionMinutes());
	}

	private LocalDateTime scheduledFrom(LocalDate dateFrom) {
		return dateFrom == null ? null : dateFrom.atStartOfDay();
	}

	private LocalDateTime scheduledToExclusive(LocalDate dateTo) {
		return dateTo == null ? null : dateTo.plusDays(1).atStartOfDay();
	}

	private java.time.OffsetDateTime reportedFrom(LocalDate dateFrom) {
		return dateFrom == null ? null : dateFrom.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
	}

	private java.time.OffsetDateTime reportedToExclusive(LocalDate dateTo) {
		return dateTo == null ? null : dateTo.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
	}

	private BigDecimal toHours(Double minutes) {
		if (minutes == null || minutes <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(minutes)
				.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

	private String labelForProviderType(MaintenanceProviderType providerType) {
		if (providerType == null) {
			return "Sem responsavel";
		}
		return switch (providerType) {
			case INTERNAL -> "Interno";
			case EXTERNAL -> "Externo";
		};
	}

	private String labelForLocationType(MaintenanceLocationType locationType) {
		return switch (locationType) {
			case ROOM -> "Quarto";
			case COMMON_AREA -> "Area comum";
		};
	}

	private String labelForStatus(MaintenanceOrderStatus status) {
		return switch (status) {
			case OPEN -> "Aberta";
			case ASSIGNED -> "Atribuida";
			case SCHEDULED -> "Agendada";
			case IN_PROGRESS -> "Em andamento";
			case COMPLETED -> "Concluida";
			case CANCELLED -> "Cancelada";
		};
	}

	private void validateRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom == null || dateTo == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateFrom and dateTo are required");
		}
		if (dateFrom.isAfter(dateTo)) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateFrom must be before or equal to dateTo");
		}
		if (Duration.between(dateFrom.atStartOfDay(), dateTo.plusDays(1).atStartOfDay()).toDays() > MAX_REPORT_DAYS) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "date range cannot exceed 93 days");
		}
	}

	private static final class BreakdownAccumulator {
		private final String label;
		private int openCount;
		private int scheduledCount;
		private int inProgressCount;
		private int completedCount;
		private int cancelledCount;
		private int urgentCount;

		private BreakdownAccumulator(String label) {
			this.label = label;
		}

		private void record(MaintenanceSummaryDetailItemView order) {
			record(order.getStatus(), order.getPriority());
		}

		private void record(MaintenanceOrderStatus status, MaintenancePriority priority) {
			switch (status) {
				case OPEN -> openCount++;
				case ASSIGNED -> openCount++;
				case SCHEDULED -> scheduledCount++;
				case IN_PROGRESS -> inProgressCount++;
				case COMPLETED -> completedCount++;
				case CANCELLED -> cancelledCount++;
			}
			if (priority == MaintenancePriority.URGENT) {
				urgentCount++;
			}
		}

		private MaintenanceSummaryBreakdownDto toDto(String code) {
			return new MaintenanceSummaryBreakdownDto(
					code,
					label,
					openCount,
					scheduledCount,
					inProgressCount,
					completedCount,
					cancelledCount,
					urgentCount
			);
		}
	}

	private record DetailFilter(
			Long providerId,
			boolean providerUnassigned,
			MaintenanceProviderType providerType,
			boolean providerTypeUnassigned,
			MaintenanceLocationType locationType,
			MaintenanceOrderStatus status
	) {}
}
