package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CancelTourBookingDto;
import com.axioma.quadras.domain.dto.CreateTourBookingDto;
import com.axioma.quadras.domain.dto.TourBookingDto;
import com.axioma.quadras.domain.dto.TourProviderSummaryDto;
import com.axioma.quadras.domain.dto.UpdateTourBookingDto;
import com.axioma.quadras.domain.dto.UpdateTourPaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.TourBooking;
import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourProvider;
import com.axioma.quadras.domain.model.TourServiceType;
import com.axioma.quadras.repository.TourBookingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
		final TourBooking saved = tourBookingRepository.save(
				TourBooking.schedule(
						input.serviceType(),
						input.startAt(),
						input.endAt(),
						input.clientName(),
						input.guestReference(),
						provider,
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
		booking.updateBooking(
				input.serviceType(),
				input.startAt(),
				input.endAt(),
				input.clientName(),
				input.guestReference(),
				provider,
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
}
