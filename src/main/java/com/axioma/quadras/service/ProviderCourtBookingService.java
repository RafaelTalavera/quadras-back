package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.ProviderCourtBookingDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.CourtBookingRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderCourtBookingService {

	private static final long MAX_RANGE_DAYS = 31;

	private final CourtBookingRepository courtBookingRepository;
	private final ProviderAccessGuard providerAccessGuard;

	public ProviderCourtBookingService(CourtBookingRepository courtBookingRepository,
			ProviderAccessGuard providerAccessGuard) {
		this.courtBookingRepository = courtBookingRepository;
		this.providerAccessGuard = providerAccessGuard;
	}

	@Transactional(readOnly = true)
	public List<ProviderCourtBookingDto> list(LocalDate bookingDate, LocalDate dateFrom,
			LocalDate dateTo, CourtBookingStatus status) {
		final ProviderAccess access = providerAccessGuard.requireType(ProviderAccessType.COURTS);
		final DateRange range = resolveRange(bookingDate, dateFrom, dateTo);
		return courtBookingRepository.findProviderViews(
				access.resourceId(), range.dateFrom(), range.dateTo(), status
		).stream().map(ProviderCourtBookingDto::from).toList();
	}

	@Transactional(readOnly = true)
	public ProviderCourtBookingDto get(Long bookingId) {
		final ProviderAccess access = providerAccessGuard.requireType(ProviderAccessType.COURTS);
		return courtBookingRepository.findProviderViewByIdAndPartnerCoachId(
				bookingId, access.resourceId()
		).map(ProviderCourtBookingDto::from).orElseThrow(() -> new ApplicationException(
				HttpStatus.NOT_FOUND, "Court booking not found"
		));
	}

	private DateRange resolveRange(LocalDate bookingDate, LocalDate dateFrom, LocalDate dateTo) {
		if (bookingDate != null) {
			if (dateFrom != null || dateTo != null) {
				throw badRequest("bookingDate cannot be combined with dateFrom or dateTo");
			}
			return new DateRange(bookingDate, bookingDate);
		}
		if (dateFrom == null && dateTo == null) {
			final LocalDate today = LocalDate.now();
			return new DateRange(today, today);
		}
		if (dateFrom == null || dateTo == null) {
			throw badRequest("dateFrom and dateTo must be provided together");
		}
		if (dateFrom.isAfter(dateTo)) {
			throw badRequest("dateFrom must be before or equal to dateTo");
		}
		if (ChronoUnit.DAYS.between(dateFrom, dateTo) + 1 > MAX_RANGE_DAYS) {
			throw badRequest("Date range must not exceed 31 days");
		}
		return new DateRange(dateFrom, dateTo);
	}

	private ApplicationException badRequest(String message) {
		return new ApplicationException(HttpStatus.BAD_REQUEST, message);
	}

	private record DateRange(LocalDate dateFrom, LocalDate dateTo) {
	}
}
