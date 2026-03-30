package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CancelTourBookingDto;
import com.axioma.quadras.domain.dto.CreateTourBookingDto;
import com.axioma.quadras.domain.dto.TourBookingDto;
import com.axioma.quadras.domain.dto.TourSummaryDetailDto;
import com.axioma.quadras.domain.dto.TourSummaryDetailItemDto;
import com.axioma.quadras.domain.dto.TourProviderSummaryDto;
import com.axioma.quadras.domain.dto.TourSummaryBreakdownDto;
import com.axioma.quadras.domain.dto.TourSummaryReportDto;
import com.axioma.quadras.domain.dto.UpdateTourBookingDto;
import com.axioma.quadras.domain.dto.UpdateTourPaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.TourBooking;
import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourPaymentMethod;
import com.axioma.quadras.domain.model.TourProvider;
import com.axioma.quadras.domain.model.TourProviderOffering;
import com.axioma.quadras.domain.model.TourServiceType;
import com.axioma.quadras.domain.model.TourSummaryGroupBy;
import com.axioma.quadras.repository.TourBookingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class TourBookingService {

	private static final int MAX_RANGE_DAYS = 93;

	private final TourBookingRepository tourBookingRepository;
	private final TourProviderService tourProviderService;

	public TourBookingService(
			TourBookingRepository tourBookingRepository,
			TourProviderService tourProviderService
	) {
		this.tourBookingRepository = tourBookingRepository;
		this.tourProviderService = tourProviderService;
	}

	@Transactional
	public TourBookingDto create(CreateTourBookingDto input, String actorUsername) {
		validateTimeWindow(input.startAt(), input.endAt());
		final TourProvider provider = tourProviderService.findProviderOrThrow(input.providerId());
		final TourProviderOffering providerOffering = resolveOffering(provider, input.providerOfferingId());
		final TourBooking saved = tourBookingRepository.save(
				TourBooking.schedule(
						input.serviceType(),
						input.startAt(),
						input.endAt(),
						input.clientName(),
						input.guestReference(),
						provider,
						providerOffering,
						input.amount(),
						input.commissionPercent(),
						input.description(),
						input.paid(),
						input.paymentMethod(),
						input.paymentDate(),
						input.paymentNotes(),
						actorUsername
				)
		);
		return TourBookingDto.from(saved);
	}

	@Transactional
	public TourBookingDto update(Long bookingId, UpdateTourBookingDto input, String actorUsername) {
		final TourBooking booking = findBookingOrThrow(bookingId);
		validateCanEdit(booking);
		validateTimeWindow(input.startAt(), input.endAt());
		final TourProvider provider = tourProviderService.findProviderOrThrow(input.providerId());
		final TourProviderOffering providerOffering = resolveOffering(provider, input.providerOfferingId());
		booking.updateBooking(
				input.serviceType(),
				input.startAt(),
				input.endAt(),
				input.clientName(),
				input.guestReference(),
				provider,
				providerOffering,
				input.amount(),
				input.commissionPercent(),
				input.description(),
				input.paid(),
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				actorUsername
		);
		return TourBookingDto.from(booking);
	}

	public List<TourBookingDto> list(
			LocalDate dateFrom,
			LocalDate dateTo,
			Long providerId,
			Boolean paid,
			TourServiceType serviceType
	) {
		return tourBookingRepository.findAllOrderedByStartAt(
				Specification.where(inDateRange(dateFrom, dateTo))
						.and(hasProvider(providerId))
						.and(hasPaid(paid))
						.and(hasServiceType(serviceType))
		).stream().map(TourBookingDto::from).toList();
	}

	@Transactional
	public TourBookingDto updatePayment(Long bookingId, UpdateTourPaymentDto input, String actorUsername) {
		final TourBooking booking = findBookingOrThrow(bookingId);
		validateCanEdit(booking);
		booking.markPayment(input.paymentMethod(), input.paymentDate(), input.paymentNotes(), actorUsername);
		return TourBookingDto.from(booking);
	}

	@Transactional
	public TourBookingDto cancel(Long bookingId, CancelTourBookingDto input, String actorUsername) {
		final TourBooking booking = findBookingOrThrow(bookingId);
		if (booking.getStatus() == TourBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled tour bookings cannot be cancelled again."
			);
		}
		booking.markCancelled(input.cancellationNotes(), actorUsername);
		return TourBookingDto.from(booking);
	}

	public List<TourProviderSummaryDto> providerSummary(LocalDate dateFrom, LocalDate dateTo) {
		final List<TourBooking> bookings = tourBookingRepository.findAllOrderedByStartAt(
				Specification.where(inDateRange(dateFrom, dateTo))
		);
		final Map<Long, ProviderSummaryAccumulator> summaryByProvider = new LinkedHashMap<>();
		for (final TourBooking booking : bookings) {
			final TourProvider provider = booking.getProvider();
			final ProviderSummaryAccumulator accumulator = summaryByProvider.computeIfAbsent(
					provider.getId(),
					ignored -> new ProviderSummaryAccumulator(provider.getId(), provider.getName(), provider.isActive())
			);
			accumulator.record(booking);
		}
		return summaryByProvider.values().stream().map(ProviderSummaryAccumulator::toDto).toList();
	}

	public TourSummaryReportDto summary(LocalDate dateFrom, LocalDate dateTo) {
		validateRange(dateFrom, dateTo);
		final List<TourBooking> bookings = tourBookingRepository.findAllOrderedByStartAt(
				Specification.where(inDateRange(dateFrom, dateTo))
		);
		int scheduledCount = 0;
		int cancelledCount = 0;
		int paidCount = 0;
		int pendingCount = 0;
		BigDecimal totalHours = BigDecimal.ZERO;
		BigDecimal grossAmount = BigDecimal.ZERO;
		BigDecimal paidAmount = BigDecimal.ZERO;
		BigDecimal pendingAmount = BigDecimal.ZERO;
		BigDecimal commissionAmount = BigDecimal.ZERO;
		final Map<Long, SummaryAccumulator> providerBreakdown = new LinkedHashMap<>();
		final Map<TourServiceType, SummaryAccumulator> serviceTypeBreakdown = initServiceTypeBreakdown();
		final Map<TourPaymentMethod, SummaryAccumulator> paymentMethodBreakdown = initPaymentMethodBreakdown();

		for (final TourBooking booking : bookings) {
			if (booking.getStatus() == TourBookingStatus.CANCELLED) {
				cancelledCount++;
				continue;
			}

			scheduledCount++;
			final BigDecimal bookingHours = durationHours(booking);
			totalHours = totalHours.add(bookingHours);
			grossAmount = grossAmount.add(booking.getAmount());
			commissionAmount = commissionAmount.add(booking.getCommissionAmount());

			final SummaryAccumulator providerAccumulator = providerBreakdown.computeIfAbsent(
					booking.getProvider().getId(),
					ignored -> SummaryAccumulator.forProvider(
							String.valueOf(booking.getProvider().getId()),
							booking.getProvider().getName(),
							booking.getProvider().isActive()
					)
			);
			providerAccumulator.record(
					bookingHours,
					booking.getAmount(),
					booking.getCommissionAmount(),
					booking.isPaid()
			);
			serviceTypeBreakdown.get(booking.getServiceType()).record(
					bookingHours,
					booking.getAmount(),
					booking.getCommissionAmount(),
					booking.isPaid()
			);

			if (booking.isPaid()) {
				paidCount++;
				paidAmount = paidAmount.add(booking.getAmount());
				if (booking.getPaymentMethod() != null) {
					paymentMethodBreakdown.get(booking.getPaymentMethod()).record(
							bookingHours,
							booking.getAmount(),
							booking.getCommissionAmount(),
							true
					);
				}
			} else {
				pendingCount++;
				pendingAmount = pendingAmount.add(booking.getAmount());
			}
		}

		final BigDecimal averageTicket = scheduledCount == 0
				? BigDecimal.ZERO
				: grossAmount.divide(BigDecimal.valueOf(scheduledCount), 2, RoundingMode.HALF_UP);

		return new TourSummaryReportDto(
				scheduledCount,
				cancelledCount,
				paidCount,
				pendingCount,
				totalHours,
				grossAmount,
				paidAmount,
				pendingAmount,
				commissionAmount,
				grossAmount.subtract(commissionAmount),
				averageTicket,
				providerBreakdown.values().stream().map(SummaryAccumulator::toDto).toList(),
				serviceTypeBreakdown.entrySet().stream()
						.map(entry -> entry.getValue().toDto(entry.getKey().name(), labelForServiceType(entry.getKey()), null))
						.toList(),
				paymentMethodBreakdown.entrySet().stream()
						.map(entry -> entry.getValue().toDto(entry.getKey().name(), labelForPaymentMethod(entry.getKey()), null))
						.toList()
		);
	}

	public TourSummaryDetailDto summaryDetails(
			TourSummaryGroupBy groupBy,
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

		final DetailContext context = resolveDetailContext(groupBy, code);
		final List<TourBooking> bookings = tourBookingRepository.findAllOrderedByStartAt(
				Specification.where(inDateRange(dateFrom, dateTo)).and(context.specification())
		);
		final SummaryAccumulator accumulator = SummaryAccumulator.forProvider(
				context.code(),
				context.label(),
				context.active()
		);
		final List<TourSummaryDetailItemDto> items = new ArrayList<>();
		for (final TourBooking booking : bookings) {
			if (booking.getStatus() == TourBookingStatus.CANCELLED) {
				continue;
			}
			accumulator.record(
					durationHours(booking),
					booking.getAmount(),
					booking.getCommissionAmount(),
					booking.isPaid()
			);
			items.add(TourSummaryDetailItemDto.from(booking));
		}

		return new TourSummaryDetailDto(
				groupBy,
				context.code(),
				context.label(),
				context.active(),
				accumulator.toDto(),
				items
		);
	}

	private TourBooking findBookingOrThrow(Long bookingId) {
		return tourBookingRepository.findById(bookingId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Tour booking " + bookingId + " not found"
				));
	}

	private void validateCanEdit(TourBooking booking) {
		if (booking.getStatus() == TourBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled tour bookings cannot be edited."
			);
		}
	}

	private void validateTimeWindow(LocalDateTime startAt, LocalDateTime endAt) {
		if (startAt == null || endAt == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "startAt and endAt are required.");
		}
		if (!startAt.isBefore(endAt)) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "endAt must be after startAt.");
		}
	}

	private void validateRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateFrom is required");
		}
		if (dateTo == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateTo is required");
		}
		if (dateFrom.isAfter(dateTo)) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateFrom must be before or equal to dateTo");
		}
		if (dateFrom.plusDays(MAX_RANGE_DAYS - 1L).isBefore(dateTo)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"date range must be less than or equal to " + MAX_RANGE_DAYS + " days"
			);
		}
	}

	private TourProviderOffering resolveOffering(TourProvider provider, Long providerOfferingId) {
		if (providerOfferingId == null) {
			return null;
		}
		final TourProviderOffering offering = tourProviderService.findOfferingOrThrow(providerOfferingId);
		if (!offering.getProvider().getId().equals(provider.getId())) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Tour provider offering does not belong to the selected provider."
			);
		}
		return offering;
	}

	private Specification<TourBooking> inDateRange(LocalDate dateFrom, LocalDate dateTo) {
		return (root, query, builder) -> {
			if (dateFrom == null && dateTo == null) {
				return null;
			}
			if (dateFrom != null && dateTo != null) {
				return builder.between(
						root.get("startAt"),
						dateFrom.atStartOfDay(),
						dateTo.atTime(LocalTime.MAX)
				);
			}
			if (dateFrom != null) {
				return builder.greaterThanOrEqualTo(root.get("startAt"), dateFrom.atStartOfDay());
			}
			return builder.lessThanOrEqualTo(root.get("startAt"), dateTo.atTime(LocalTime.MAX));
		};
	}

	private Specification<TourBooking> hasProvider(Long providerId) {
		return (root, query, builder) -> providerId == null
				? null
				: builder.equal(root.get("provider").get("id"), providerId);
	}

	private Specification<TourBooking> hasPaid(Boolean paid) {
		return (root, query, builder) -> paid == null
				? null
				: builder.equal(root.get("paid"), paid);
	}

	private Specification<TourBooking> hasServiceType(TourServiceType serviceType) {
		return (root, query, builder) -> serviceType == null
				? null
				: builder.equal(root.get("serviceType"), serviceType);
	}

	private Specification<TourBooking> hasPaymentMethod(TourPaymentMethod paymentMethod) {
		return (root, query, builder) -> paymentMethod == null
				? null
				: builder.equal(root.get("paymentMethod"), paymentMethod);
	}

	private Map<TourServiceType, SummaryAccumulator> initServiceTypeBreakdown() {
		final Map<TourServiceType, SummaryAccumulator> breakdown = new LinkedHashMap<>();
		for (final TourServiceType value : TourServiceType.values()) {
			breakdown.put(value, new SummaryAccumulator());
		}
		return breakdown;
	}

	private Map<TourPaymentMethod, SummaryAccumulator> initPaymentMethodBreakdown() {
		final Map<TourPaymentMethod, SummaryAccumulator> breakdown = new LinkedHashMap<>();
		for (final TourPaymentMethod value : TourPaymentMethod.values()) {
			breakdown.put(value, new SummaryAccumulator());
		}
		return breakdown;
	}

	private String labelForServiceType(TourServiceType serviceType) {
		return switch (serviceType) {
			case TOUR -> "Tour";
			case TRAVEL -> "Traslado";
		};
	}

	private String labelForPaymentMethod(TourPaymentMethod paymentMethod) {
		return switch (paymentMethod) {
			case PIX -> "Pix";
			case CARD -> "Cartao";
			case CASH -> "Dinheiro";
			case TRANSFER -> "Transferencia";
		};
	}

	private BigDecimal durationHours(TourBooking booking) {
		final long minutes = java.time.Duration.between(booking.getStartAt(), booking.getEndAt()).toMinutes();
		return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

	private DetailContext resolveDetailContext(TourSummaryGroupBy groupBy, String code) {
		return switch (groupBy) {
			case PROVIDER -> resolveProviderDetailContext(code);
			case SERVICE_TYPE -> resolveServiceTypeDetailContext(code);
			case PAYMENT_METHOD -> resolvePaymentMethodDetailContext(code);
		};
	}

	private DetailContext resolveProviderDetailContext(String code) {
		final Long providerId;
		try {
			providerId = Long.valueOf(code);
		} catch (NumberFormatException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "provider code must be a number");
		}
		final TourProvider provider = tourProviderService.findProviderOrThrow(providerId);
		return new DetailContext(
				String.valueOf(provider.getId()),
				provider.getName(),
				provider.isActive(),
				hasProvider(provider.getId())
		);
	}

	private DetailContext resolveServiceTypeDetailContext(String code) {
		final TourServiceType serviceType;
		try {
			serviceType = TourServiceType.valueOf(code.trim().toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Unknown serviceType code: " + code);
		}
		return new DetailContext(
				serviceType.name(),
				labelForServiceType(serviceType),
				null,
				hasServiceType(serviceType)
		);
	}

	private DetailContext resolvePaymentMethodDetailContext(String code) {
		final TourPaymentMethod paymentMethod;
		try {
			paymentMethod = TourPaymentMethod.valueOf(code.trim().toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Unknown paymentMethod code: " + code);
		}
		return new DetailContext(
				paymentMethod.name(),
				labelForPaymentMethod(paymentMethod),
				null,
				hasPaymentMethod(paymentMethod)
		);
	}

	private static final class ProviderSummaryAccumulator {
		private final Long providerId;
		private final String providerName;
		private final boolean providerActive;
		private int scheduledCount;
		private int cancelledCount;
		private int paidCount;
		private int pendingCount;
		private BigDecimal grossAmount = BigDecimal.ZERO;
		private BigDecimal paidAmount = BigDecimal.ZERO;
		private BigDecimal pendingAmount = BigDecimal.ZERO;
		private BigDecimal commissionAmount = BigDecimal.ZERO;
		private LocalDateTime lastBookingAt;

		private ProviderSummaryAccumulator(Long providerId, String providerName, boolean providerActive) {
			this.providerId = providerId;
			this.providerName = providerName;
			this.providerActive = providerActive;
		}

		private void record(TourBooking booking) {
			if (lastBookingAt == null || booking.getStartAt().isAfter(lastBookingAt)) {
				lastBookingAt = booking.getStartAt();
			}
			if (booking.getStatus() == TourBookingStatus.CANCELLED) {
				cancelledCount++;
				return;
			}
			scheduledCount++;
			grossAmount = grossAmount.add(booking.getAmount());
			commissionAmount = commissionAmount.add(booking.getCommissionAmount());
			if (booking.isPaid()) {
				paidCount++;
				paidAmount = paidAmount.add(booking.getAmount());
			} else {
				pendingCount++;
				pendingAmount = pendingAmount.add(booking.getAmount());
			}
		}

		private TourProviderSummaryDto toDto() {
			return new TourProviderSummaryDto(
					providerId,
					providerName,
					providerActive,
					scheduledCount,
					cancelledCount,
					paidCount,
					pendingCount,
					grossAmount,
					paidAmount,
					pendingAmount,
					commissionAmount,
					lastBookingAt
			);
		}
	}

	private static final class SummaryAccumulator {
		private final String code;
		private final String label;
		private final Boolean active;
		private int scheduledCount;
		private int paidCount;
		private int pendingCount;
		private BigDecimal totalHours = BigDecimal.ZERO;
		private BigDecimal grossAmount = BigDecimal.ZERO;
		private BigDecimal paidAmount = BigDecimal.ZERO;
		private BigDecimal pendingAmount = BigDecimal.ZERO;
		private BigDecimal commissionAmount = BigDecimal.ZERO;

		private SummaryAccumulator() {
			this(null, null, null);
		}

		private SummaryAccumulator(String code, String label, Boolean active) {
			this.code = code;
			this.label = label;
			this.active = active;
		}

		private static SummaryAccumulator forProvider(String code, String label, Boolean active) {
			return new SummaryAccumulator(code, label, active);
		}

		private void record(
				BigDecimal bookingHours,
				BigDecimal bookingAmount,
				BigDecimal bookingCommissionAmount,
				boolean paid
		) {
			scheduledCount++;
			totalHours = totalHours.add(bookingHours);
			grossAmount = grossAmount.add(bookingAmount);
			commissionAmount = commissionAmount.add(bookingCommissionAmount);
			if (paid) {
				paidCount++;
				paidAmount = paidAmount.add(bookingAmount);
			} else {
				pendingCount++;
				pendingAmount = pendingAmount.add(bookingAmount);
			}
		}

		private TourSummaryBreakdownDto toDto() {
			return toDto(code, label, active);
		}

		private TourSummaryBreakdownDto toDto(String itemCode, String itemLabel, Boolean itemActive) {
			return new TourSummaryBreakdownDto(
					itemCode,
					itemLabel,
					itemActive,
					scheduledCount,
					paidCount,
					pendingCount,
					totalHours,
					grossAmount,
					paidAmount,
					pendingAmount,
					commissionAmount
			);
		}
	}

	private record DetailContext(
			String code,
			String label,
			Boolean active,
			Specification<TourBooking> specification
	) {}
}
