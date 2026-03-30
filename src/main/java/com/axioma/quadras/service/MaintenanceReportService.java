package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.MaintenanceSummaryBreakdownDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryDetailDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryDetailItemDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryReportDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceSummaryGroupBy;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
		final List<MaintenanceOrder> orders = filteredOrders(dateFrom, dateTo);
		final int openCount = countByStatus(orders, MaintenanceOrderStatus.OPEN);
		final int scheduledCount = countByStatus(orders, MaintenanceOrderStatus.SCHEDULED);
		final int inProgressCount = countByStatus(orders, MaintenanceOrderStatus.IN_PROGRESS);
		final int completedCount = countByStatus(orders, MaintenanceOrderStatus.COMPLETED);
		final int cancelledCount = countByStatus(orders, MaintenanceOrderStatus.CANCELLED);
		final int internalCount = (int) orders.stream()
				.filter(order -> order.getProviderTypeSnapshot() == MaintenanceProviderType.INTERNAL)
				.count();
		final int externalCount = orders.size() - internalCount;
		final int roomsCount = (int) orders.stream()
				.filter(order -> order.getLocationTypeSnapshot() == MaintenanceLocationType.ROOM)
				.count();
		final int commonAreasCount = orders.size() - roomsCount;
		final int urgentCount = (int) orders.stream()
				.filter(order -> order.getPriority() == MaintenancePriority.URGENT)
				.count();
		final BigDecimal averageResolutionHours = calculateAverageResolutionHours(orders);

		return new MaintenanceSummaryReportDto(
				openCount,
				scheduledCount,
				inProgressCount,
				completedCount,
				cancelledCount,
				internalCount,
				externalCount,
				roomsCount,
				commonAreasCount,
				urgentCount,
				averageResolutionHours,
				buildBreakdown(
						orders,
						order -> String.valueOf(order.getProvider().getId()),
						MaintenanceOrder::getProviderNameSnapshot
				),
				buildBreakdown(
						orders,
						order -> order.getProviderTypeSnapshot().name(),
						order -> labelForProviderType(order.getProviderTypeSnapshot())
				),
				buildBreakdown(
						orders,
						order -> order.getLocationTypeSnapshot().name(),
						order -> labelForLocationType(order.getLocationTypeSnapshot())
				),
				buildBreakdown(
						orders,
						order -> order.getStatus().name(),
						order -> labelForStatus(order.getStatus())
				)
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
		final List<MaintenanceOrder> orders = filteredOrders(dateFrom, dateTo).stream()
				.filter(order -> matchesGroup(order, groupBy, code))
				.sorted(Comparator.comparing(this::referenceDateTime))
				.toList();
		if (orders.isEmpty()) {
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Maintenance summary group not found.");
		}
		final MaintenanceOrder firstOrder = orders.get(0);
		return new MaintenanceSummaryDetailDto(
				groupBy,
				code,
				labelForGroup(firstOrder, groupBy),
				toBreakdown(code, labelForGroup(firstOrder, groupBy), orders),
				orders.stream().map(MaintenanceSummaryDetailItemDto::from).toList()
		);
	}

	private List<MaintenanceOrder> filteredOrders(LocalDate dateFrom, LocalDate dateTo) {
		return maintenanceOrderRepository.findAllOrdered().stream()
				.filter(order -> matchesRange(order, dateFrom, dateTo))
				.toList();
	}

	private boolean matchesRange(MaintenanceOrder order, LocalDate dateFrom, LocalDate dateTo) {
		final LocalDate referenceDate = referenceDateTime(order).toLocalDate();
		if (dateFrom != null && referenceDate.isBefore(dateFrom)) {
			return false;
		}
		if (dateTo != null && referenceDate.isAfter(dateTo)) {
			return false;
		}
		return true;
	}

	private LocalDateTime referenceDateTime(MaintenanceOrder order) {
		return order.getScheduledStartAt() != null
				? order.getScheduledStartAt()
				: order.getReportedAt().toLocalDateTime();
	}

	private int countByStatus(List<MaintenanceOrder> orders, MaintenanceOrderStatus status) {
		return (int) orders.stream().filter(order -> order.getStatus() == status).count();
	}

	private BigDecimal calculateAverageResolutionHours(List<MaintenanceOrder> orders) {
		final List<BigDecimal> resolvedDurations = orders.stream()
				.filter(order -> order.getStatus() == MaintenanceOrderStatus.COMPLETED)
				.filter(order -> order.getCompletedAt() != null)
				.map(order -> {
					final java.time.OffsetDateTime startReference =
							order.getStartedAt() != null ? order.getStartedAt() : order.getReportedAt();
					final long minutes = Duration.between(startReference, order.getCompletedAt()).toMinutes();
					if (minutes <= 0) {
						return BigDecimal.ZERO;
					}
					return BigDecimal.valueOf(minutes)
							.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
				})
				.toList();
		if (resolvedDurations.isEmpty()) {
			return BigDecimal.ZERO;
		}
		final BigDecimal total = resolvedDurations.stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return total.divide(BigDecimal.valueOf(resolvedDurations.size()), 2, RoundingMode.HALF_UP);
	}

	private List<MaintenanceSummaryBreakdownDto> buildBreakdown(
			List<MaintenanceOrder> orders,
			Function<MaintenanceOrder, String> codeExtractor,
			Function<MaintenanceOrder, String> labelExtractor
	) {
		final Map<String, BreakdownAccumulator> grouped = new LinkedHashMap<>();
		for (final MaintenanceOrder order : orders) {
			final String code = codeExtractor.apply(order);
			grouped.computeIfAbsent(code, ignored -> new BreakdownAccumulator(labelExtractor.apply(order)))
					.record(order);
		}
		return grouped.entrySet().stream()
				.map(entry -> entry.getValue().toDto(entry.getKey()))
				.toList();
	}

	private MaintenanceSummaryBreakdownDto toBreakdown(
			String code,
			String label,
			List<MaintenanceOrder> orders
	) {
		final BreakdownAccumulator accumulator = new BreakdownAccumulator(label);
		orders.forEach(accumulator::record);
		return accumulator.toDto(code);
	}

	private boolean matchesGroup(MaintenanceOrder order, MaintenanceSummaryGroupBy groupBy, String code) {
		return switch (groupBy) {
			case PROVIDER -> String.valueOf(order.getProvider().getId()).equals(code);
			case PROVIDER_TYPE -> order.getProviderTypeSnapshot().name().equals(code);
			case LOCATION_TYPE -> order.getLocationTypeSnapshot().name().equals(code);
			case STATUS -> order.getStatus().name().equals(code);
		};
	}

	private String labelForGroup(MaintenanceOrder order, MaintenanceSummaryGroupBy groupBy) {
		return switch (groupBy) {
			case PROVIDER -> order.getProviderNameSnapshot();
			case PROVIDER_TYPE -> labelForProviderType(order.getProviderTypeSnapshot());
			case LOCATION_TYPE -> labelForLocationType(order.getLocationTypeSnapshot());
			case STATUS -> labelForStatus(order.getStatus());
		};
	}

	private String labelForProviderType(MaintenanceProviderType providerType) {
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

		private void record(MaintenanceOrder order) {
			switch (order.getStatus()) {
				case OPEN -> openCount++;
				case SCHEDULED -> scheduledCount++;
				case IN_PROGRESS -> inProgressCount++;
				case COMPLETED -> completedCount++;
				case CANCELLED -> cancelledCount++;
			}
			if (order.getPriority() == MaintenancePriority.URGENT) {
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
}
