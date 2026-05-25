package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CancelCourtBookingDto;
import com.axioma.quadras.domain.dto.CreateCourtBookingDto;
import com.axioma.quadras.domain.dto.CreateCourtBookingRecurrenceDto;
import com.axioma.quadras.domain.dto.CourtBookingDto;
import com.axioma.quadras.domain.dto.CourtBookingMaterialDto;
import com.axioma.quadras.domain.dto.CourtBookingMaterialInputDto;
import com.axioma.quadras.domain.dto.CourtSummaryBreakdownDto;
import com.axioma.quadras.domain.dto.CourtSummaryReportDto;
import com.axioma.quadras.domain.dto.UpdateCourtBookingDto;
import com.axioma.quadras.domain.dto.UpdateCourtPaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtBookingCancellationScope;
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
import java.util.Objects;
import java.util.UUID;
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
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public CourtBookingService(
			CourtBookingRepository courtBookingRepository,
			CourtBookingMaterialRepository courtBookingMaterialRepository,
			CourtRateRepository courtRateRepository,
			CourtMaterialSettingRepository courtMaterialSettingRepository,
			CourtConfigurationService courtConfigurationService,
			ScheduleLockService scheduleLockService,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.courtBookingRepository = courtBookingRepository;
		this.courtBookingMaterialRepository = courtBookingMaterialRepository;
		this.courtRateRepository = courtRateRepository;
		this.courtMaterialSettingRepository = courtMaterialSettingRepository;
		this.courtConfigurationService = courtConfigurationService;
		this.scheduleLockService = scheduleLockService;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
	}

	@Transactional
	public CourtBookingDto create(CreateCourtBookingDto input, String actorUsername) {
		validateTimeWindow(input.bookingDate(), input.startTime(), input.endTime());
		validatePartnerCoachName(input.customerType(), input.customerName());
		validateRecurrence(input.customerType(), input.bookingDate(), input.recurrence());
		final List<LocalDate> bookingDates = resolveBookingDates(input.bookingDate(), input.recurrence());
		scheduleLockService.acquireCourtBookingDates(bookingDates);
		for (final LocalDate bookingDate : bookingDates) {
			validateOverlapping(bookingDate, input.startTime(), input.endTime(), null);
		}
		final String recurrenceGroupId = input.recurrence() == null ? null : UUID.randomUUID().toString();
		final LocalDate recurrenceStartDate = recurrenceGroupId == null ? null : input.bookingDate();
		final LocalDate recurrenceEndDate = recurrenceGroupId == null ? null : input.recurrence().endDate();
		final List<CourtBooking> savedBookings = courtBookingRepository.saveAll(
				bookingDates.stream().map(bookingDate -> {
					final CourtPricingSnapshot pricing = resolvePricing(
							bookingDate,
							input.startTime(),
							input.endTime(),
							input.customerType(),
							input.materials()
					);
					return CourtBooking.schedule(
							bookingDate,
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
							recurrenceGroupId,
							recurrenceStartDate,
							recurrenceEndDate,
							pricing.materials(),
							actorUsername
					);
				}).toList()
		);
		final CourtBooking saved = savedBookings.get(0);
		savedBookings.forEach(booking -> auditTrailService.record(
				"courts",
				"court-booking",
				booking.getId(),
				"CREATED",
				"Reserva de cancha creada",
				List.of(),
				null,
				snapshot(booking)
		));
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.COURTS,
				"created",
				saved.getId(),
				bookingDates.get(0),
				bookingDates.get(bookingDates.size() - 1)
		);
		return CourtBookingDto.from(saved);
	}

	@Transactional
	public CourtBookingDto update(Long bookingId, UpdateCourtBookingDto input, String actorUsername) {
		final CourtBooking booking = findBookingOrThrow(bookingId);
		final Map<String, Object> beforeState = snapshot(booking);
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
		final Map<String, Object> afterState = snapshot(booking);
		auditTrailService.record(
				"courts",
				"court-booking",
				booking.getId(),
				"UPDATED",
				"Reserva de cancha actualizada",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.COURTS,
				"updated",
				booking.getId(),
				minDate(previousBookingDate, booking.getBookingDate()),
				maxDate(previousBookingDate, booking.getBookingDate())
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
		final Map<String, Object> beforeState = snapshot(booking);
		validateCanEdit(booking);
		booking.markPayment(input.paymentMethod(), input.paymentDate(), input.paymentNotes(), actorUsername);
		final Map<String, Object> afterState = snapshot(booking);
		auditTrailService.record(
				"courts",
				"court-booking",
				booking.getId(),
				"PAYMENT_UPDATED",
				"Pago de reserva de cancha actualizado",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.COURTS,
				"payment-updated",
				booking.getId(),
				booking.getBookingDate(),
				booking.getBookingDate()
		);
		return CourtBookingDto.from(booking);
	}

	@Transactional
	public CourtBookingDto cancel(Long bookingId, CancelCourtBookingDto input, String actorUsername) {
		final CourtBooking booking = findBookingOrThrow(bookingId);
		final CourtBookingCancellationScope scope = input.cancellationScope() == null
				? CourtBookingCancellationScope.SINGLE
				: input.cancellationScope();
		final List<CourtBooking> bookingsToCancel;
		if (scope == CourtBookingCancellationScope.SERIES) {
			bookingsToCancel = resolveSeriesBookingsForCancellation(booking);
		} else {
			if (booking.getStatus() == CourtBookingStatus.CANCELLED) {
				throw new ApplicationException(
						HttpStatus.CONFLICT,
						"Cancelled court bookings cannot be cancelled again."
				);
			}
			bookingsToCancel = List.of(booking);
		}
		final Map<Long, Map<String, Object>> beforeStateById = new LinkedHashMap<>();
		bookingsToCancel.forEach(item -> beforeStateById.put(item.getId(), snapshot(item)));
		bookingsToCancel.forEach(item -> item.markCancelled(input.cancellationNotes(), actorUsername));
		bookingsToCancel.forEach(item -> {
			final Map<String, Object> beforeState = beforeStateById.get(item.getId());
			final Map<String, Object> afterState = snapshot(item);
			auditTrailService.record(
					"courts",
					"court-booking",
					item.getId(),
					"CANCELLED",
					"Reserva de cancha cancelada",
					diff(beforeState, afterState),
					beforeState,
					afterState
			);
		});
		final List<LocalDate> affectedDates = bookingsToCancel.stream()
				.map(CourtBooking::getBookingDate)
				.sorted()
				.toList();
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.COURTS,
				"cancelled",
				booking.getId(),
				affectedDates.get(0),
				affectedDates.get(affectedDates.size() - 1)
		);
		return CourtBookingDto.from(booking);
	}

	public List<AuditEventDto> audit(Long bookingId) {
		findBookingOrThrow(bookingId);
		return auditTrailService.findByEntity("court-booking", bookingId);
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
			String groupKey,
			String code,
			String label,
			CourtSummaryBreakdownView view
	) {
		if (view == null) {
			return new CourtSummaryBreakdownDto(
					groupKey,
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
				groupKey,
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

	private void validateRecurrence(
			CourtCustomerType customerType,
			LocalDate bookingDate,
			CreateCourtBookingRecurrenceDto recurrence
	) {
		if (recurrence == null) {
			return;
		}
		if (customerType != CourtCustomerType.PARTNER_COACH) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Recurring court bookings are only available for partner coaches."
			);
		}
		if (recurrence.endDate() == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Recurring booking end date is required.");
		}
		if (recurrence.endDate().isBefore(bookingDate)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Recurring booking end date must be on or after the first booking date."
			);
		}
	}

	private List<LocalDate> resolveBookingDates(LocalDate bookingDate, CreateCourtBookingRecurrenceDto recurrence) {
		if (recurrence == null) {
			return List.of(bookingDate);
		}
		final List<LocalDate> bookingDates = new ArrayList<>();
		LocalDate current = bookingDate;
		while (!current.isAfter(recurrence.endDate())) {
			bookingDates.add(current);
			current = current.plusWeeks(1);
		}
		return bookingDates;
	}

	private List<CourtBooking> resolveSeriesBookingsForCancellation(CourtBooking booking) {
		if (booking.getRecurrenceGroupId() == null || booking.getRecurrenceGroupId().isBlank()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Only recurring court bookings can be cancelled as a series."
			);
		}
		final List<CourtBooking> seriesBookings = courtBookingRepository
				.findByRecurrenceGroupIdAndStatusOrderByBookingDateAscStartTimeAscIdAsc(
						booking.getRecurrenceGroupId(),
						CourtBookingStatus.SCHEDULED
				);
		if (seriesBookings.isEmpty()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled court bookings cannot be cancelled again."
			);
		}
		return seriesBookings;
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

	private LocalDate minDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? right : left;
	}

	private LocalDate maxDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? left : right;
	}

	private Map<String, Object> snapshot(CourtBooking booking) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", booking.getId());
		snapshot.put("bookingDate", toValue(booking.getBookingDate()));
		snapshot.put("startTime", toValue(booking.getStartTime()));
		snapshot.put("endTime", toValue(booking.getEndTime()));
		snapshot.put("customerName", booking.getCustomerName());
		snapshot.put("customerReference", booking.getCustomerReference());
		snapshot.put("customerType", booking.getCustomerType() == null ? null : booking.getCustomerType().name());
		snapshot.put("status", booking.getStatus() == null ? null : booking.getStatus().name());
		snapshot.put("pricingPeriod", booking.getPricingPeriod() == null ? null : booking.getPricingPeriod().name());
		snapshot.put("courtAmount", toValue(booking.getCourtAmount()));
		snapshot.put("materialsAmount", toValue(booking.getMaterialsAmount()));
		snapshot.put("totalAmount", toValue(booking.getTotalAmount()));
		snapshot.put("paid", booking.isPaid());
		snapshot.put("paymentMethod", booking.getPaymentMethod() == null ? null : booking.getPaymentMethod().name());
		snapshot.put("paymentDate", toValue(booking.getPaymentDate()));
		snapshot.put("paymentNotes", booking.getPaymentNotes());
		snapshot.put("cancellationNotes", booking.getCancellationNotes());
		snapshot.put("recurrenceGroupId", booking.getRecurrenceGroupId());
		snapshot.put("recurrenceStartDate", toValue(booking.getRecurrenceStartDate()));
		snapshot.put("recurrenceEndDate", toValue(booking.getRecurrenceEndDate()));
		snapshot.put("createdAt", toValue(booking.getCreatedAt()));
		snapshot.put("updatedAt", toValue(booking.getUpdatedAt()));
		snapshot.put("cancelledAt", toValue(booking.getCancelledAt()));
		snapshot.put("createdBy", booking.getCreatedBy());
		snapshot.put("updatedBy", booking.getUpdatedBy());
		snapshot.put("cancelledBy", booking.getCancelledBy());
		snapshot.put("materials", booking.getMaterials().stream().map(material -> {
			final Map<String, Object> item = new LinkedHashMap<>();
			item.put("code", material.getMaterialCode());
			item.put("label", material.getMaterialLabel());
			item.put("quantity", material.getQuantity());
			item.put("unitPrice", toValue(material.getUnitPrice()));
			item.put("totalPrice", toValue(material.getTotalPrice()));
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
