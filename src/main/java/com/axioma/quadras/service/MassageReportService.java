package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.MassageProviderDetailReportDto;
import com.axioma.quadras.domain.dto.MassageProviderReportItemDto;
import com.axioma.quadras.domain.dto.MassageProviderSummaryDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.repository.MassageBookingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageReportService {

	private static final int MAX_RANGE_DAYS = 93;

	private final MassageBookingRepository massageBookingRepository;
	private final MassageProviderService massageProviderService;

	public MassageReportService(
			MassageBookingRepository massageBookingRepository,
			MassageProviderService massageProviderService
	) {
		this.massageBookingRepository = massageBookingRepository;
		this.massageProviderService = massageProviderService;
	}

	public List<MassageProviderSummaryDto> listProviderSummary(LocalDate dateFrom, LocalDate dateTo) {
		validateRange(dateFrom, dateTo);
		final List<MassageBooking> bookings = massageBookingRepository.findAllOrderedByDateAndTime(
				withDateRange(dateFrom, dateTo)
		);
		final Map<Long, _ProviderAccumulator> grouped = new LinkedHashMap<>();
		for (final MassageBooking booking : bookings) {
			final Long providerId = booking.getProvider().getId();
			final _ProviderAccumulator accumulator = grouped.computeIfAbsent(
					providerId,
					ignored -> new _ProviderAccumulator(booking.getProvider())
			);
			accumulator.add(booking);
		}

		return grouped.values().stream()
				.map(_ProviderAccumulator::toSummaryDto)
				.sorted(Comparator.comparing(
						MassageProviderSummaryDto::providerName,
						String.CASE_INSENSITIVE_ORDER
				))
				.toList();
	}

	public MassageProviderDetailReportDto getProviderDetails(
			Long providerId,
			LocalDate dateFrom,
			LocalDate dateTo
	) {
		validateRange(dateFrom, dateTo);
		final MassageProvider provider = massageProviderService.findProviderOrThrow(providerId);
		final List<MassageBooking> bookings = massageBookingRepository.findAllOrderedByDateAndTime(
				withDateRange(dateFrom, dateTo).and(hasProviderId(providerId))
		);

		final _ProviderAccumulator accumulator = new _ProviderAccumulator(provider);
		for (final MassageBooking booking : bookings) {
			accumulator.add(booking);
		}

		final List<MassageProviderReportItemDto> items = bookings.stream()
				.map(MassageProviderReportItemDto::from)
				.toList();

		return new MassageProviderDetailReportDto(
				provider.getId(),
				provider.getName(),
				provider.isActive(),
				accumulator.toSummaryDto(),
				items
		);
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

	private Specification<MassageBooking> withDateRange(LocalDate dateFrom, LocalDate dateTo) {
		return Specification.where(hasBookingDateFrom(dateFrom)).and(hasBookingDateTo(dateTo));
	}

	private Specification<MassageBooking> hasBookingDateFrom(LocalDate dateFrom) {
		return (root, query, builder) -> builder.greaterThanOrEqualTo(
				root.get("bookingDate"),
				dateFrom
		);
	}

	private Specification<MassageBooking> hasBookingDateTo(LocalDate dateTo) {
		return (root, query, builder) -> builder.lessThanOrEqualTo(
				root.get("bookingDate"),
				dateTo
		);
	}

	private Specification<MassageBooking> hasProviderId(Long providerId) {
		return (root, query, builder) -> builder.equal(root.get("provider").get("id"), providerId);
	}

	private static final class _ProviderAccumulator {

		private final MassageProvider provider;
		private int scheduledCount;
		private int cancelledCount;
		private int paidCount;
		private int pendingCount;
		private BigDecimal grossAmount = BigDecimal.ZERO;
		private BigDecimal paidAmount = BigDecimal.ZERO;
		private BigDecimal pendingAmount = BigDecimal.ZERO;
		private final List<OffsetDateTime> bookingInstants = new ArrayList<>();

		private _ProviderAccumulator(MassageProvider provider) {
			this.provider = provider;
		}

		private void add(MassageBooking booking) {
			bookingInstants.add(
					booking.getBookingDate()
							.atTime(booking.getStartTime())
							.atOffset(ZoneOffset.UTC)
			);
			if (booking.getStatus() == MassageBookingStatus.CANCELLED) {
				cancelledCount++;
				return;
			}

			scheduledCount++;
			grossAmount = grossAmount.add(booking.getAmount());
			if (booking.isPaid()) {
				paidCount++;
				paidAmount = paidAmount.add(booking.getAmount());
				return;
			}
			pendingCount++;
			pendingAmount = pendingAmount.add(booking.getAmount());
		}

		private MassageProviderSummaryDto toSummaryDto() {
			OffsetDateTime lastBookingAt = null;
			for (final OffsetDateTime bookingInstant : bookingInstants) {
				if (lastBookingAt == null || bookingInstant.isAfter(lastBookingAt)) {
					lastBookingAt = bookingInstant;
				}
			}
			return new MassageProviderSummaryDto(
					provider.getId(),
					provider.getName(),
					provider.isActive(),
					provider.getTherapists().size(),
					scheduledCount,
					cancelledCount,
					scheduledCount,
					paidCount,
					pendingCount,
					grossAmount,
					paidAmount,
					pendingAmount,
					lastBookingAt
			);
		}
	}
}
