package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CancelCourtBookingDto;
import com.axioma.quadras.domain.dto.CreateCourtBookingDto;
import com.axioma.quadras.domain.dto.CourtBookingDto;
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
import com.axioma.quadras.repository.CourtMaterialSettingRepository;
import com.axioma.quadras.repository.CourtRateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourtBookingService {

	private static final LocalTime OPENING_TIME = LocalTime.of(7, 0);
	private static final LocalTime CLOSING_TIME = LocalTime.of(23, 0);

	private final CourtBookingRepository courtBookingRepository;
	private final CourtRateRepository courtRateRepository;
	private final CourtMaterialSettingRepository courtMaterialSettingRepository;
	private final CourtConfigurationService courtConfigurationService;

	public CourtBookingService(
			CourtBookingRepository courtBookingRepository,
			CourtRateRepository courtRateRepository,
			CourtMaterialSettingRepository courtMaterialSettingRepository,
			CourtConfigurationService courtConfigurationService
	) {
		this.courtBookingRepository = courtBookingRepository;
		this.courtRateRepository = courtRateRepository;
		this.courtMaterialSettingRepository = courtMaterialSettingRepository;
		this.courtConfigurationService = courtConfigurationService;
	}

	@Transactional
	public CourtBookingDto create(CreateCourtBookingDto input, String actorUsername) {
		validateTimeWindow(input.bookingDate(), input.startTime(), input.endTime());
		validatePartnerCoachName(input.customerType(), input.customerName());
		validateOverlapping(input.bookingDate(), input.startTime(), input.endTime(), null);
		final CourtPricingSnapshot pricing = resolvePricing(
				input.bookingDate(),
				input.startTime(),
				input.endTime(),
				input.customerType(),
				input.materials()
		);
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
		validateCanEdit(booking);
		validateTimeWindow(input.bookingDate(), input.startTime(), input.endTime());
		validatePartnerCoachName(input.customerType(), input.customerName());
		validateOverlapping(input.bookingDate(), input.startTime(), input.endTime(), bookingId);
		final CourtPricingSnapshot pricing = resolvePricing(
				input.bookingDate(),
				input.startTime(),
				input.endTime(),
				input.customerType(),
				input.materials()
		);
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

	public List<CourtBookingDto> list(LocalDate bookingDate, CourtCustomerType customerType, Boolean paid) {
		final Specification<CourtBooking> specification = Specification
				.where(hasBookingDate(bookingDate))
				.and(hasCustomerType(customerType))
				.and(hasPaid(paid));
		return courtBookingRepository.findAllOrderedByDateAndTime(specification).stream()
				.map(CourtBookingDto::from)
				.toList();
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
		final List<CourtBooking> bookings = courtBookingRepository.findAllOrderedByDateAndTime(
				Specification.where(inDateRange(dateFrom, dateTo))
		);
		int scheduledCount = 0;
		int cancelledCount = 0;
		int paidCount = 0;
		int pendingCount = 0;
		BigDecimal totalHours = BigDecimal.ZERO;
		BigDecimal guestHours = BigDecimal.ZERO;
		BigDecimal vipHours = BigDecimal.ZERO;
		BigDecimal externalHours = BigDecimal.ZERO;
		BigDecimal partnerCoachHours = BigDecimal.ZERO;
		BigDecimal paidAmount = BigDecimal.ZERO;
		BigDecimal pendingAmount = BigDecimal.ZERO;
		BigDecimal courtAmount = BigDecimal.ZERO;
		BigDecimal materialsAmount = BigDecimal.ZERO;
		final Map<CourtCustomerType, SummaryAccumulator> customerTypeBreakdown = initCustomerTypeBreakdown();
		final Map<CourtPricingPeriod, SummaryAccumulator> pricingPeriodBreakdown = initPricingPeriodBreakdown();
		final Map<CourtPaymentMethod, SummaryAccumulator> paymentMethodBreakdown = initPaymentMethodBreakdown();

		for (final CourtBooking booking : bookings) {
			if (booking.getStatus() == CourtBookingStatus.CANCELLED) {
				cancelledCount++;
				continue;
			}
			scheduledCount++;
			final BigDecimal bookingHours = BigDecimal.valueOf(booking.getDurationMinutes())
					.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
			totalHours = totalHours.add(bookingHours);
			courtAmount = courtAmount.add(booking.getCourtAmount());
			materialsAmount = materialsAmount.add(booking.getMaterialsAmount());
			switch (booking.getCustomerType()) {
				case GUEST -> guestHours = guestHours.add(bookingHours);
				case VIP -> vipHours = vipHours.add(bookingHours);
				case EXTERNAL -> externalHours = externalHours.add(bookingHours);
				case PARTNER_COACH -> partnerCoachHours = partnerCoachHours.add(bookingHours);
			}
			customerTypeBreakdown.get(booking.getCustomerType())
					.record(
							bookingHours,
							booking.getCourtAmount(),
							booking.getMaterialsAmount(),
							booking.getTotalAmount(),
							booking.isPaid()
					);
			pricingPeriodBreakdown.get(booking.getPricingPeriod())
					.record(
							bookingHours,
							booking.getCourtAmount(),
							booking.getMaterialsAmount(),
							booking.getTotalAmount(),
							booking.isPaid()
					);
			if (booking.isPaid()) {
				paidCount++;
				paidAmount = paidAmount.add(booking.getTotalAmount());
				if (booking.getPaymentMethod() != null) {
					paymentMethodBreakdown.get(booking.getPaymentMethod())
							.record(
									bookingHours,
									booking.getCourtAmount(),
									booking.getMaterialsAmount(),
									booking.getTotalAmount(),
									true
							);
				}
			} else {
				pendingCount++;
				pendingAmount = pendingAmount.add(booking.getTotalAmount());
			}
		}

		final BigDecimal expectedAmount = paidAmount.add(pendingAmount);
		final BigDecimal averageTicket = scheduledCount == 0
				? BigDecimal.ZERO
				: expectedAmount.divide(BigDecimal.valueOf(scheduledCount), 2, RoundingMode.HALF_UP);

		return new CourtSummaryReportDto(
				scheduledCount,
				cancelledCount,
				paidCount,
				pendingCount,
				totalHours,
				guestHours,
				vipHours,
				externalHours,
				partnerCoachHours,
				paidAmount,
				pendingAmount,
				courtAmount,
				materialsAmount,
				expectedAmount,
				averageTicket,
				customerTypeBreakdown.entrySet().stream()
						.map(entry -> entry.getValue().toDto(entry.getKey().name(), labelForCustomerType(entry.getKey())))
						.toList(),
				pricingPeriodBreakdown.entrySet().stream()
						.map(entry -> entry.getValue().toDto(entry.getKey().name(), labelForPricingPeriod(entry.getKey())))
						.toList(),
				paymentMethodBreakdown.entrySet().stream()
						.map(entry -> entry.getValue().toDto(entry.getKey().name(), labelForPaymentMethod(entry.getKey())))
						.toList()
		);
	}

	private Map<CourtCustomerType, SummaryAccumulator> initCustomerTypeBreakdown() {
		final Map<CourtCustomerType, SummaryAccumulator> breakdown = new LinkedHashMap<>();
		for (final CourtCustomerType value : CourtCustomerType.values()) {
			breakdown.put(value, new SummaryAccumulator());
		}
		return breakdown;
	}

	private Map<CourtPricingPeriod, SummaryAccumulator> initPricingPeriodBreakdown() {
		final Map<CourtPricingPeriod, SummaryAccumulator> breakdown = new LinkedHashMap<>();
		for (final CourtPricingPeriod value : CourtPricingPeriod.values()) {
			breakdown.put(value, new SummaryAccumulator());
		}
		return breakdown;
	}

	private Map<CourtPaymentMethod, SummaryAccumulator> initPaymentMethodBreakdown() {
		final Map<CourtPaymentMethod, SummaryAccumulator> breakdown = new LinkedHashMap<>();
		for (final CourtPaymentMethod value : CourtPaymentMethod.values()) {
			breakdown.put(value, new SummaryAccumulator());
		}
		return breakdown;
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

	private Specification<CourtBooking> hasBookingDate(LocalDate bookingDate) {
		return (root, query, builder) -> bookingDate == null
				? null
				: builder.equal(root.get("bookingDate"), bookingDate);
	}

	private Specification<CourtBooking> hasCustomerType(CourtCustomerType customerType) {
		return (root, query, builder) -> customerType == null
				? null
				: builder.equal(root.get("customerType"), customerType);
	}

	private Specification<CourtBooking> hasPaid(Boolean paid) {
		return (root, query, builder) -> paid == null
				? null
				: builder.equal(root.get("paid"), paid);
	}

	private Specification<CourtBooking> inDateRange(LocalDate dateFrom, LocalDate dateTo) {
		return (root, query, builder) -> {
			if (dateFrom == null && dateTo == null) {
				return null;
			}
			if (dateFrom != null && dateTo != null) {
				return builder.between(root.get("bookingDate"), dateFrom, dateTo);
			}
			if (dateFrom != null) {
				return builder.greaterThanOrEqualTo(root.get("bookingDate"), dateFrom);
			}
			return builder.lessThanOrEqualTo(root.get("bookingDate"), dateTo);
		};
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

	private static final class SummaryAccumulator {
		private int scheduledCount;
		private int paidCount;
		private int pendingCount;
		private BigDecimal totalHours = BigDecimal.ZERO;
		private BigDecimal courtAmount = BigDecimal.ZERO;
		private BigDecimal materialsAmount = BigDecimal.ZERO;
		private BigDecimal totalAmount = BigDecimal.ZERO;

		private void record(
				BigDecimal bookingHours,
				BigDecimal bookingCourtAmount,
				BigDecimal bookingMaterialsAmount,
				BigDecimal bookingTotalAmount,
				boolean paid
		) {
			scheduledCount++;
			totalHours = totalHours.add(bookingHours);
			courtAmount = courtAmount.add(bookingCourtAmount);
			materialsAmount = materialsAmount.add(bookingMaterialsAmount);
			totalAmount = totalAmount.add(bookingTotalAmount);
			if (paid) {
				paidCount++;
			} else {
				pendingCount++;
			}
		}

		private CourtSummaryBreakdownDto toDto(String code, String label) {
			return new CourtSummaryBreakdownDto(
					code,
					label,
					scheduledCount,
					paidCount,
					pendingCount,
					totalHours,
					courtAmount,
					materialsAmount,
					totalAmount
			);
		}
	}
}
