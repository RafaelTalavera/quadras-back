package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CancelTourBookingDto;
import com.axioma.quadras.domain.dto.TourBookingCompactDto;
import com.axioma.quadras.domain.dto.TourBookingCompactPageDto;
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
import com.axioma.quadras.repository.TourBookingListItemView;
import com.axioma.quadras.repository.TourSummaryDetailItemView;
import com.axioma.quadras.repository.TourProviderSummaryView;
import com.axioma.quadras.repository.TourSummaryAggregateView;
import com.axioma.quadras.repository.TourSummaryBreakdownView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TourBookingService {

	private static final int MAX_RANGE_DAYS = 93;
	private static final int DEFAULT_COMPACT_PAGE_SIZE = 25;
	private static final int MAX_COMPACT_PAGE_SIZE = 100;

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
		final List<TourBookingListItemView> bookings = tourBookingRepository.findListItems(
				dateFrom == null ? null : dateFrom.atStartOfDay(),
				dateTo == null ? null : dateTo.atTime(LocalTime.MAX),
				providerId,
				paid,
				serviceType
		);
		return bookings.stream().map(TourBookingDto::from).toList();
	}

	public TourBookingCompactPageDto listCompact(
			LocalDate dateFrom,
			LocalDate dateTo,
			Long providerId,
			Boolean paid,
			TourServiceType serviceType,
			Integer page,
			Integer size
	) {
		final int safePage = normalizePage(page);
		final int safeSize = normalizePageSize(size);
		final var bookings = tourBookingRepository.findCompactItems(
				dateFrom == null ? null : dateFrom.atStartOfDay(),
				dateTo == null ? null : dateTo.atTime(LocalTime.MAX),
				providerId,
				paid,
				serviceType,
				PageRequest.of(safePage, safeSize)
		);
		return new TourBookingCompactPageDto(
				safePage,
				safeSize,
				bookings.hasNext(),
				bookings.getContent().stream().map(TourBookingCompactDto::from).toList()
		);
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
		return tourBookingRepository.findProviderSummary(
						dateFrom == null ? null : dateFrom.atStartOfDay(),
						dateTo == null ? null : dateTo.atTime(LocalTime.MAX),
						TourBookingStatus.SCHEDULED,
						TourBookingStatus.CANCELLED
				).stream()
				.map(this::toProviderSummaryDto)
				.toList();
	}

	public TourSummaryReportDto summary(LocalDate dateFrom, LocalDate dateTo) {
		validateRange(dateFrom, dateTo);
		final LocalDateTime startAtFrom = dateFrom.atStartOfDay();
		final LocalDateTime startAtTo = dateTo.atTime(LocalTime.MAX);
		final TourSummaryAggregateView aggregate = tourBookingRepository.findSummaryAggregate(startAtFrom, startAtTo);
		final int scheduledCount = Math.toIntExact(aggregate.getScheduledCount());
		final BigDecimal grossAmount = safeAmount(aggregate.getGrossAmount());
		final BigDecimal paidAmount = safeAmount(aggregate.getPaidAmount());
		final BigDecimal pendingAmount = safeAmount(aggregate.getPendingAmount());
		final BigDecimal commissionAmount = safeAmount(aggregate.getCommissionAmount());

		final BigDecimal averageTicket = scheduledCount == 0
				? BigDecimal.ZERO
				: grossAmount.divide(BigDecimal.valueOf(scheduledCount), 2, RoundingMode.HALF_UP);

		return new TourSummaryReportDto(
				scheduledCount,
				Math.toIntExact(aggregate.getCancelledCount()),
				Math.toIntExact(aggregate.getPaidCount()),
				Math.toIntExact(aggregate.getPendingCount()),
				toHours(aggregate.getTotalMinutes()),
				grossAmount,
				paidAmount,
				pendingAmount,
				commissionAmount,
				grossAmount.subtract(commissionAmount),
				averageTicket,
				buildProviderBreakdown(startAtFrom, startAtTo),
				buildServiceTypeBreakdown(startAtFrom, startAtTo),
				buildPaymentMethodBreakdown(startAtFrom, startAtTo)
		);
	}

	private List<TourSummaryBreakdownDto> buildProviderBreakdown(LocalDateTime startAtFrom, LocalDateTime startAtTo) {
		return tourBookingRepository.findProviderSummaryBreakdown(startAtFrom, startAtTo).stream()
				.map(view -> toBreakdownDto(view.getCode(), view.getLabel(), view.getActive(), view))
				.toList();
	}

	private List<TourSummaryBreakdownDto> buildServiceTypeBreakdown(LocalDateTime startAtFrom, LocalDateTime startAtTo) {
		final Map<String, TourSummaryBreakdownView> breakdownByCode = indexByCode(
				tourBookingRepository.findServiceTypeSummaryBreakdown(startAtFrom, startAtTo)
		);
		final List<TourSummaryBreakdownDto> breakdown = new ArrayList<>();
		for (final TourServiceType value : TourServiceType.values()) {
			breakdown.add(toBreakdownDto(
					value.name(),
					labelForServiceType(value),
					null,
					breakdownByCode.get(value.name())
			));
		}
		return breakdown;
	}

	private List<TourSummaryBreakdownDto> buildPaymentMethodBreakdown(LocalDateTime startAtFrom, LocalDateTime startAtTo) {
		final Map<String, TourSummaryBreakdownView> breakdownByCode = indexByCode(
				tourBookingRepository.findPaymentMethodSummaryBreakdown(startAtFrom, startAtTo)
		);
		final List<TourSummaryBreakdownDto> breakdown = new ArrayList<>();
		for (final TourPaymentMethod value : TourPaymentMethod.values()) {
			breakdown.add(toBreakdownDto(
					value.name(),
					labelForPaymentMethod(value),
					null,
					breakdownByCode.get(value.name())
			));
		}
		return breakdown;
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
		final LocalDateTime startAtFrom = dateFrom.atStartOfDay();
		final LocalDateTime startAtTo = dateTo.atTime(LocalTime.MAX);
		final TourSummaryBreakdownView summaryView = findDetailSummaryView(groupBy, context, startAtFrom, startAtTo);
		final List<TourSummaryDetailItemDto> items = tourBookingRepository.findSummaryDetailItems(
						startAtFrom,
						startAtTo,
						context.providerId(),
						context.serviceTypeCode(),
						context.paymentMethodCode()
				).stream()
				.map(this::toDetailItemDto)
				.toList();

		return new TourSummaryDetailDto(
				groupBy,
				context.code(),
				context.label(),
				context.active(),
				toDetailSummaryDto(
						context.code(),
						context.label(),
						context.active(),
						summaryView
				),
				items
		);
	}

	private TourSummaryBreakdownView findDetailSummaryView(
			TourSummaryGroupBy groupBy,
			DetailContext context,
			LocalDateTime startAtFrom,
			LocalDateTime startAtTo
	) {
		return switch (groupBy) {
			case PROVIDER -> tourBookingRepository.findProviderSummaryBreakdown(startAtFrom, startAtTo).stream()
					.filter(view -> context.code().equals(view.getCode()))
					.findFirst()
					.orElse(null);
			case SERVICE_TYPE -> tourBookingRepository.findServiceTypeSummaryBreakdown(startAtFrom, startAtTo).stream()
					.filter(view -> context.code().equals(view.getCode()))
					.findFirst()
					.orElse(null);
			case PAYMENT_METHOD -> tourBookingRepository.findPaymentMethodSummaryBreakdown(startAtFrom, startAtTo).stream()
					.filter(view -> context.code().equals(view.getCode()))
					.findFirst()
					.orElse(null);
		};
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

	private Map<String, TourSummaryBreakdownView> indexByCode(List<TourSummaryBreakdownView> views) {
		final Map<String, TourSummaryBreakdownView> indexed = new LinkedHashMap<>();
		for (final TourSummaryBreakdownView view : views) {
			indexed.put(view.getCode(), view);
		}
		return indexed;
	}

	private TourSummaryBreakdownDto toBreakdownDto(
			String code,
			String label,
			Boolean active,
			TourSummaryBreakdownView view
	) {
		if (view == null) {
			return new TourSummaryBreakdownDto(
					code,
					label,
					active,
					0,
					0,
					0,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO
			);
		}
		return new TourSummaryBreakdownDto(
				code,
				label,
				active,
				Math.toIntExact(view.getScheduledCount()),
				Math.toIntExact(view.getPaidCount()),
				Math.toIntExact(view.getPendingCount()),
				toHours(view.getTotalMinutes()),
				safeAmount(view.getGrossAmount()),
				safeAmount(view.getPaidAmount()),
				safeAmount(view.getPendingAmount()),
				safeAmount(view.getCommissionAmount())
		);
	}

	private BigDecimal toHours(long minutes) {
		if (minutes <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(minutes)
				.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

	private TourSummaryBreakdownDto toDetailSummaryDto(
			String code,
			String label,
			Boolean active,
			TourSummaryBreakdownView view
	) {
		if (view == null) {
			return new TourSummaryBreakdownDto(
					code,
					label,
					active,
					0,
					0,
					0,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO
			);
		}
		return toBreakdownDto(code, label, active, view);
	}

	private TourSummaryDetailItemDto toDetailItemDto(TourSummaryDetailItemView view) {
		return new TourSummaryDetailItemDto(
				view.getBookingId(),
				view.getStartAt(),
				view.getEndAt(),
				TourServiceType.valueOf(view.getServiceType()),
				view.getProviderId(),
				view.getProviderName(),
				view.getProviderOfferingId(),
				view.getProviderOfferingName(),
				view.getClientName(),
				view.getGuestReference(),
				view.getAmount(),
				view.getCommissionAmount(),
				Boolean.TRUE.equals(view.getPaid()),
				view.getPaymentMethod() == null ? null : TourPaymentMethod.valueOf(view.getPaymentMethod()),
				view.getPaymentDate(),
				TourBookingStatus.valueOf(view.getStatus()),
				view.getDescription()
		);
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
				provider.getId(),
				null,
				null
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
				null,
				serviceType.name(),
				null
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
				null,
				null,
				paymentMethod.name()
		);
	}

	private TourProviderSummaryDto toProviderSummaryDto(TourProviderSummaryView view) {
		return new TourProviderSummaryDto(
				view.getProviderId(),
				view.getProviderName(),
				view.getProviderActive(),
				Math.toIntExact(view.getScheduledCount()),
				Math.toIntExact(view.getCancelledCount()),
				Math.toIntExact(view.getPaidCount()),
				Math.toIntExact(view.getPendingCount()),
				view.getGrossAmount() == null ? BigDecimal.ZERO : view.getGrossAmount(),
				view.getPaidAmount() == null ? BigDecimal.ZERO : view.getPaidAmount(),
				view.getPendingAmount() == null ? BigDecimal.ZERO : view.getPendingAmount(),
				view.getCommissionAmount() == null ? BigDecimal.ZERO : view.getCommissionAmount(),
				view.getLastBookingAt()
		);
	}

	private BigDecimal safeAmount(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private record DetailContext(
			String code,
			String label,
			Boolean active,
			Long providerId,
			String serviceTypeCode,
			String paymentMethodCode
	) {}
}
