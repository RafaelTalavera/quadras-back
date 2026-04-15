package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CancelCourtBookingDto;
import com.axioma.quadras.domain.dto.CreateCourtBookingDto;
import com.axioma.quadras.domain.dto.CourtBookingDto;
import com.axioma.quadras.domain.dto.CourtBookingMaterialDto;
import com.axioma.quadras.domain.dto.CourtBookingMaterialInputDto;
import com.axioma.quadras.domain.dto.CourtSummaryBreakdownDto;
import com.axioma.quadras.domain.dto.CourtSummaryReportDto;
import com.axioma.quadras.domain.dto.UpdateCourtBookingDto;
import com.axioma.quadras.domain.dto.UpdateCourtPaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtBookingMaterial;
import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtMaterialSetting;
import com.axioma.quadras.domain.model.CourtPaymentMethod;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import com.axioma.quadras.domain.model.CourtRate;
import com.axioma.quadras.repository.CourtBookingRepository;
import com.axioma.quadras.repository.CourtBookingMaterialRepository;
import com.axioma.quadras.repository.CourtMaterialSettingRepository;
import com.axioma.quadras.repository.CourtRateRepository;
import com.axioma.quadras.repository.CourtSummaryAggregateView;
import com.axioma.quadras.repository.CourtSummaryBreakdownView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourtBookingService {

	private static final LocalTime OPENING_TIME = LocalTime.of(7, 0);
	private static final LocalTime CLOSING_TIME = LocalTime.of(23, 0);

	private final CourtBookingRepository courtBookingRepository;
	private final CourtBookingMaterialRepository courtBookingMaterialRepository;
	private final CourtRateRepository courtRateRepository;
	private final CourtMaterialSettingRepository courtMaterialSettingRepository;
	private final CourtConfigurationService courtConfigurationService;
	private final ScheduleLockService scheduleLockService;

	public CourtBookingService(
			CourtBookingRepository courtBookingRepository,
			CourtBookingMaterialRepository courtBookingMaterialRepository,
			CourtRateRepository courtRateRepository,
			CourtMaterialSettingRepository courtMaterialSettingRepository,
			CourtConfigurationService courtConfigurationService,
			ScheduleLockService scheduleLockService
	) {
		this.courtBookingRepository = courtBookingRepository;
		this.courtBookingMaterialRepository = courtBookingMaterialRepository;
		this.courtRateRepository = courtRateRepository;
		this.courtMaterialSettingRepository = courtMaterialSettingRepository;
		this.courtConfigurationService = courtConfigurationService;
		this.scheduleLockService = scheduleLockService;
	}

	@Transactional
	public CourtBookingDto create(CreateCourtBookingDto input, String actorUsername) {
		validateTimeWindow(input.bookingDate(), input.startTime(), input.endTime());
		validatePartnerCoachName(input.customerType(), input.customerName());
		final CourtPricingSnapshot pricing = resolvePricing(
				input.bookingDate(),
				input.startTime(),
				input.endTime(),
				input.customerType(),
				input.materials()
		);
		scheduleLockService.acquireCourtBookingDates(List.of(input.bookingDate()));
		validateOverlapping(input.bookingDate(), input.startTime(), input.endTime(), null);
		final CourtBooking saved = courtBookingRepository.save(
				CourtBooking.schedule(
						input.bookingDate(),
						input.startTime(),
						input.endTime(),
						input.customerName(),
						input.customerReference(),
						input.customerType(),
						pricing.period(),
						pricing.sunriseEstimate(),
						pricing.sunsetEstimate(),
						pricing.courtAmount(),
						pricing.materialsAmount(),
						pricing.totalAmount(),
						input.paid(),
						input.paymentMethod(),
						input.paymentDate(),
						input.paymentNotes(),
						pricing.materials(),
						actorUsername
				)
		);
		return CourtBookingDto.from(saved);
	}

	@Transactional
	public CourtBookingDto update(Long bookingId, UpdateCourtBookingDto input, String actorUsername) {
		final CourtBooking booking = findBookingOrThrow(bookingId);
		final LocalDate previousBookingDate = booking.getBookingDate();
		validateCanEdit(booking);
		validateTimeWindow(input.bookingDate(), input.startTime(), input.endTime());
		validatePartnerCoachName(input.customerType(), input.customerName());
		final CourtPricingSnapshot pricing = resolvePricing(
				input.bookingDate(),
				input.startTime(),
				input.endTime(),
				input.customerType(),
				input.materials()
		);
		scheduleLockService.acquireCourtBookingDates(List.of(previousBookingDate, input.bookingDate()));
		validateOverlapping(input.bookingDate(), input.startTime(), input.endTime(), bookingId);
		booking.updateBooking(
				input.bookingDate(),
				input.startTime(),
				input.endTime(),
				input.customerName(),
				input.customerReference(),
				input.customerType(),
				pricing.period(),
				pricing.sunriseEstimate(),
				pricing.sunsetEstimate(),
				pricing.courtAmount(),
				pricing.materialsAmount(),
				pricing.totalAmount(),
				input.paid(),
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				pricing.materials(),
				actorUsername
		);
		return CourtBookingDto.from(booking);
	}

	public List<CourtBookingDto> list(
			LocalDate bookingDate,
			LocalDate dateFrom,
			LocalDate dateTo,
			CourtCustomerType customerType,
			Boolean paid
	) {
		validateListRange(dateFrom, dateTo);
		final LocalDate effectiveDateFrom = bookingDate != null ? bookingDate : dateFrom;
		final LocalDate effectiveDateTo = bookingDate != null ? bookingDate : dateTo;
		final var bookings = courtBookingRepository.findListItems(
				bookingDate,
				effectiveDateFrom,
				effectiveDateTo,
				customerType,
				paid
		);
		final Map<Long, List<CourtBookingMaterialDto>> materialsByBookingId =
				loadMaterialsByBookingId(bookings.stream().map(item -> item.getId()).toList());
		return bookings.stream()
				.map(booking -> CourtBookingDto.from(
						booking,
						materialsByBookingId.getOrDefault(booking.getId(), List.of())
				))
				.toList();
	}

	private void validateListRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"dateFrom must be before or equal to dateTo"
			);
		}
	}

	@Transactional
	public CourtBookingDto updatePayment(Long bookingId, UpdateCourtPaymentDto input, String actorUsername) {
		final CourtBooking booking = findBookingOrThrow(bookingId);
		validateCanEdit(booking);
		booking.markPayment(input.paymentMethod(), input.paymentDate(), input.paymentNotes(), actorUsername);
		return CourtBookingDto.from(booking);
	}

	@Transactional
	public CourtBookingDto cancel(Long bookingId, CancelCourtBookingDto input, String actorUsername) {
		final CourtBooking booking = findBookingOrThrow(bookingId);
		if (booking.getStatus() == CourtBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled court bookings cannot be cancelled again."
			);
		}
		booking.markCancelled(input.cancellationNotes(), actorUsername);
		return CourtBookingDto.from(booking);
	}

	public CourtSummaryReportDto summary(LocalDate dateFrom, LocalDate dateTo) {
		final CourtSummaryAggregateView aggregate = courtBookingRepository.findSummaryAggregate(dateFrom, dateTo);
		final int scheduledCount = Math.toIntExact(aggregate.getScheduledCount());
		final BigDecimal paidAmount = safeAmount(aggregate.getPaidAmount());
		final BigDecimal pendingAmount = safeAmount(aggregate.getPendingAmount());
		final BigDecimal courtAmount = safeAmount(aggregate.getCourtAmount());
		final BigDecimal materialsAmount = safeAmount(aggregate.getMaterialsAmount());
		final BigDecimal expectedAmount = paidAmount.add(pendingAmount);
		final BigDecimal averageTicket = scheduledCount == 0
				? BigDecimal.ZERO
				: expectedAmount.divide(BigDecimal.valueOf(scheduledCount), 2, RoundingMode.HALF_UP);

		return new CourtSummaryReportDto(
				scheduledCount,
				Math.toIntExact(aggregate.getCancelledCount()),
				Math.toIntExact(aggregate.getPaidCount()),
				Math.toIntExact(aggregate.getPendingCount()),
				toHours(aggregate.getTotalMinutes()),
				toHours(aggregate.getGuestMinutes()),
				toHours(aggregate.getVipMinutes()),
				toHours(aggregate.getExternalMinutes()),
				toHours(aggregate.getPartnerCoachMinutes()),
				paidAmount,
				pendingAmount,
				courtAmount,
				materialsAmount,
				expectedAmount,
				averageTicket,
				buildCustomerTypeBreakdown(dateFrom, dateTo),
				buildPricingPeriodBreakdown(dateFrom, dateTo),
				buildPaymentMethodBreakdown(dateFrom, dateTo)
		);
	}

	private List<CourtSummaryBreakdownDto> buildCustomerTypeBreakdown(LocalDate dateFrom, LocalDate dateTo) {
		final Map<String, CourtSummaryBreakdownView> breakdownByCode = indexByCode(
				courtBookingRepository.findCustomerTypeSummaryBreakdown(dateFrom, dateTo)
		);
		final List<CourtSummaryBreakdownDto> breakdown = new ArrayList<>();
		for (final CourtCustomerType value : CourtCustomerType.values()) {
			breakdown.add(toBreakdownDto(
					value.name(),
					labelForCustomerType(value),
					breakdownByCode.get(value.name())
			));
		}
		return breakdown;
	}

	private List<CourtSummaryBreakdownDto> buildPricingPeriodBreakdown(LocalDate dateFrom, LocalDate dateTo) {
		final Map<String, CourtSummaryBreakdownView> breakdownByCode = indexByCode(
				courtBookingRepository.findPricingPeriodSummaryBreakdown(dateFrom, dateTo)
		);
		final List<CourtSummaryBreakdownDto> breakdown = new ArrayList<>();
		for (final CourtPricingPeriod value : CourtPricingPeriod.values()) {
			breakdown.add(toBreakdownDto(
					value.name(),
					labelForPricingPeriod(value),
					breakdownByCode.get(value.name())
			));
		}
		return breakdown;
	}

	private List<CourtSummaryBreakdownDto> buildPaymentMethodBreakdown(LocalDate dateFrom, LocalDate dateTo) {
		final Map<String, CourtSummaryBreakdownView> breakdownByCode = indexByCode(
				courtBookingRepository.findPaymentMethodSummaryBreakdown(dateFrom, dateTo)
		);
		final List<CourtSummaryBreakdownDto> breakdown = new ArrayList<>();
		for (final CourtPaymentMethod value : CourtPaymentMethod.values()) {
			breakdown.add(toBreakdownDto(
					value.name(),
					labelForPaymentMethod(value),
					breakdownByCode.get(value.name())
			));
		}
		return breakdown;
	}

	private Map<String, CourtSummaryBreakdownView> indexByCode(List<CourtSummaryBreakdownView> views) {
		final Map<String, CourtSummaryBreakdownView> indexed = new LinkedHashMap<>();
		for (final CourtSummaryBreakdownView view : views) {
			indexed.put(view.getCode(), view);
		}
		return indexed;
	}

	private CourtSummaryBreakdownDto toBreakdownDto(
			String code,
			String label,
			CourtSummaryBreakdownView view
	) {
		if (view == null) {
			return new CourtSummaryBreakdownDto(
					code,
					label,
					0,
					0,
					0,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO
			);
		}
		return new CourtSummaryBreakdownDto(
				code,
				label,
				Math.toIntExact(view.getScheduledCount()),
				Math.toIntExact(view.getPaidCount()),
				Math.toIntExact(view.getPendingCount()),
				toHours(view.getTotalMinutes()),
				safeAmount(view.getCourtAmount()),
				safeAmount(view.getMaterialsAmount()),
				safeAmount(view.getTotalAmount())
		);
	}

	private String labelForCustomerType(CourtCustomerType customerType) {
		return switch (customerType) {
			case GUEST -> "Hospede";
			case VIP -> "VIP";
			case EXTERNAL -> "Externo";
			case PARTNER_COACH -> "Professor parceiro";
		};
	}

	private String labelForPricingPeriod(CourtPricingPeriod pricingPeriod) {
		return switch (pricingPeriod) {
			case DAY -> "Diurno";
			case NIGHT -> "Noturno";
		};
	}

	private String labelForPaymentMethod(CourtPaymentMethod paymentMethod) {
		return switch (paymentMethod) {
			case PIX -> "Pix";
			case CARD -> "Cartao";
			case CASH -> "Dinheiro";
			case COURTESY -> "Cortesia";
			case TRANSFER -> "Transferencia";
		};
	}

	private BigDecimal toHours(long minutes) {
		if (minutes <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(minutes)
				.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal safeAmount(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private CourtPricingSnapshot resolvePricing(
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			CourtCustomerType customerType,
			List<CourtBookingMaterialInputDto> requestedMaterials
	) {
		final SolarEstimate solarEstimate = estimateSolarWindow(bookingDate);
		final CourtPricingPeriod period = resolvePricingPeriod(startTime, endTime, solarEstimate.sunset());
		final CourtRate rate = courtRateRepository
				.findByCustomerTypeAndPricingPeriod(customerType, period)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.CONFLICT,
						"No active rate configuration found for customer type and period."
				));
		if (!rate.isActive()) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Selected court rate is inactive.");
		}

		BigDecimal courtAmount = rate.getAmount();
		if (customerType == CourtCustomerType.GUEST || customerType == CourtCustomerType.VIP) {
			courtAmount = BigDecimal.ZERO;
		}

		final List<CourtBookingMaterial> materialItems = new ArrayList<>();
		BigDecimal materialsAmount = BigDecimal.ZERO;
		if (requestedMaterials != null) {
			for (final CourtBookingMaterialInputDto requested : requestedMaterials) {
				if (requested == null || requested.quantity() == null || requested.quantity() == 0) {
					continue;
				}
				final CourtMaterialSetting setting = courtMaterialSettingRepository
						.findByCode(requested.materialCode())
						.orElseThrow(() -> new ApplicationException(
								HttpStatus.CONFLICT,
								"Court material configuration not found for " + requested.materialCode()
						));
				if (!setting.isActive()) {
					throw new ApplicationException(
							HttpStatus.CONFLICT,
							"Court material " + setting.getCode() + " is inactive."
					);
				}
				final BigDecimal unitPrice = setting.charges(customerType)
						? setting.getUnitPrice()
						: BigDecimal.ZERO;
				final BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(requested.quantity()));
				materialsAmount = materialsAmount.add(totalPrice);
				materialItems.add(
						CourtBookingMaterial.of(
								setting.getCode(),
								setting.getLabel(),
								requested.quantity(),
								unitPrice,
								totalPrice
						)
				);
			}
		}

		return new CourtPricingSnapshot(
				period,
				solarEstimate.sunrise(),
				solarEstimate.sunset(),
				courtAmount,
				materialsAmount,
				courtAmount.add(materialsAmount),
				materialItems
		);
	}

	private void validateTimeWindow(LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
		if (bookingDate == null || startTime == null || endTime == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Booking date and times are required.");
		}
		if (!startTime.isBefore(endTime)) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "startTime must be before endTime.");
		}
		if (startTime.isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Court bookings must be within operating hours 07:00 to 23:00."
			);
		}
	}

	private void validateOverlapping(
			LocalDate bookingDate,
			LocalTime startTime,
			LocalTime endTime,
			Long excludedBookingId
	) {
		final boolean overlaps = excludedBookingId == null
				? courtBookingRepository.existsByBookingDateAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
						bookingDate,
						CourtBookingStatus.SCHEDULED,
						endTime,
						startTime
				)
				: courtBookingRepository.existsByBookingDateAndStatusAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
						bookingDate,
						CourtBookingStatus.SCHEDULED,
						excludedBookingId,
						endTime,
						startTime
				);
		if (overlaps) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Court booking overlaps with an existing active booking."
			);
		}
	}

	private CourtBooking findBookingOrThrow(Long bookingId) {
		return courtBookingRepository.findById(bookingId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Court booking " + bookingId + " not found"
				));
	}

	private void validateCanEdit(CourtBooking booking) {
		if (booking.getStatus() == CourtBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled court bookings cannot be edited."
			);
		}
	}

	private void validatePartnerCoachName(CourtCustomerType customerType, String customerName) {
		if (customerType != CourtCustomerType.PARTNER_COACH) {
			return;
		}
		if (!courtConfigurationService.isActivePartnerCoachName(customerName)) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Partner coach name must match an active predefined coach."
			);
		}
	}

	private Map<Long, List<CourtBookingMaterialDto>> loadMaterialsByBookingId(Collection<Long> bookingIds) {
		if (bookingIds == null || bookingIds.isEmpty()) {
			return Map.of();
		}
		final Map<Long, List<CourtBookingMaterialDto>> materialsByBookingId = new LinkedHashMap<>();
		courtBookingMaterialRepository.findListItemsByBookingIdIn(bookingIds)
				.forEach(item -> materialsByBookingId.computeIfAbsent(
						item.getBookingId(),
						ignored -> new ArrayList<>()
				).add(CourtBookingMaterialDto.from(item)));
		return materialsByBookingId;
	}

	private CourtPricingPeriod resolvePricingPeriod(
			LocalTime startTime,
			LocalTime endTime,
			LocalTime sunsetEstimate
	) {
		return endTime.isAfter(sunsetEstimate) || !startTime.isBefore(sunsetEstimate)
				? CourtPricingPeriod.NIGHT
				: CourtPricingPeriod.DAY;
	}

	private SolarEstimate estimateSolarWindow(LocalDate bookingDate) {
		return switch (bookingDate.getMonth()) {
			case JANUARY -> new SolarEstimate(LocalTime.of(5, 20), LocalTime.of(19, 5));
			case FEBRUARY -> new SolarEstimate(LocalTime.of(5, 40), LocalTime.of(18, 45));
			case MARCH -> new SolarEstimate(LocalTime.of(6, 17), LocalTime.of(18, 25));
			case APRIL -> new SolarEstimate(LocalTime.of(6, 25), LocalTime.of(17, 55));
			case MAY -> new SolarEstimate(LocalTime.of(6, 45), LocalTime.of(17, 35));
			case JUNE -> new SolarEstimate(LocalTime.of(7, 4), LocalTime.of(17, 27));
			case JULY -> new SolarEstimate(LocalTime.of(6, 58), LocalTime.of(17, 38));
			case AUGUST -> new SolarEstimate(LocalTime.of(6, 35), LocalTime.of(17, 55));
			case SEPTEMBER -> new SolarEstimate(LocalTime.of(6, 5), LocalTime.of(18, 5));
			case OCTOBER -> new SolarEstimate(LocalTime.of(5, 35), LocalTime.of(18, 20));
			case NOVEMBER -> new SolarEstimate(LocalTime.of(5, 15), LocalTime.of(18, 42));
			case DECEMBER -> new SolarEstimate(LocalTime.of(5, 15), LocalTime.of(19, 9));
		};
	}

	private record SolarEstimate(LocalTime sunrise, LocalTime sunset) {}

	private record CourtPricingSnapshot(
			CourtPricingPeriod period,
			LocalTime sunriseEstimate,
			LocalTime sunsetEstimate,
			BigDecimal courtAmount,
			BigDecimal materialsAmount,
			BigDecimal totalAmount,
			List<CourtBookingMaterial> materials
	) {}

}
