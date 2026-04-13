package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.MassageProviderDetailReportDto;
import com.axioma.quadras.domain.dto.MassageProviderReportItemDto;
import com.axioma.quadras.domain.dto.MassageProviderSummaryDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassagePaymentMethod;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.repository.MassageBookingRepository;
import com.axioma.quadras.repository.MassageProviderReportItemView;
import com.axioma.quadras.repository.MassageProviderSummaryView;
import com.axioma.quadras.repository.MassageTherapistCountView;
import com.axioma.quadras.repository.MassageTherapistRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageReportService {

	private static final int MAX_RANGE_DAYS = 93;

	private final MassageBookingRepository massageBookingRepository;
	private final MassageProviderService massageProviderService;
	private final MassageTherapistRepository massageTherapistRepository;

	public MassageReportService(
			MassageBookingRepository massageBookingRepository,
			MassageProviderService massageProviderService,
			MassageTherapistRepository massageTherapistRepository
	) {
		this.massageBookingRepository = massageBookingRepository;
		this.massageProviderService = massageProviderService;
		this.massageTherapistRepository = massageTherapistRepository;
	}

	public List<MassageProviderSummaryDto> listProviderSummary(LocalDate dateFrom, LocalDate dateTo) {
		validateRange(dateFrom, dateTo);
		return massageBookingRepository.findProviderSummary(dateFrom, dateTo, null).stream()
				.map(this::toSummaryDto)
				.toList();
	}

	public MassageProviderDetailReportDto getProviderDetails(
			Long providerId,
			LocalDate dateFrom,
			LocalDate dateTo
	) {
		validateRange(dateFrom, dateTo);
		final MassageProvider provider = massageProviderService.findProviderOrThrow(providerId);
		final MassageProviderSummaryDto summary = massageBookingRepository.findProviderSummary(
						dateFrom,
						dateTo,
						providerId
				).stream()
				.findFirst()
				.map(this::toSummaryDto)
				.orElseGet(() -> emptySummary(provider));

		final List<MassageProviderReportItemDto> items = massageBookingRepository.findProviderReportItems(
						providerId,
						dateFrom,
						dateTo
				).stream()
				.map(this::toReportItemDto)
				.toList();

		return new MassageProviderDetailReportDto(
				provider.getId(),
				provider.getName(),
				provider.isActive(),
				summary,
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

	private MassageProviderSummaryDto toSummaryDto(MassageProviderSummaryView view) {
		final OffsetDateTime lastBookingAt = view.getLastBookingDate() == null || view.getLastBookingTime() == null
				? null
				: view.getLastBookingDate().atTime(view.getLastBookingTime()).atOffset(ZoneOffset.UTC);
		return new MassageProviderSummaryDto(
				view.getProviderId(),
				view.getProviderName(),
				view.getProviderActive(),
				Math.toIntExact(view.getTherapistsCount()),
				Math.toIntExact(view.getScheduledCount()),
				Math.toIntExact(view.getCancelledCount()),
				Math.toIntExact(view.getScheduledCount()),
				Math.toIntExact(view.getPaidCount()),
				Math.toIntExact(view.getPendingCount()),
				safeAmount(view.getGrossAmount()),
				safeAmount(view.getPaidAmount()),
				safeAmount(view.getPendingAmount()),
				lastBookingAt
		);
	}

	private MassageProviderReportItemDto toReportItemDto(MassageProviderReportItemView view) {
		return new MassageProviderReportItemDto(
				view.getBookingId(),
				view.getBookingDate(),
				view.getStartTime(),
				view.getClientName(),
				view.getGuestReference(),
				view.getTreatment(),
				view.getTherapistId(),
				view.getTherapistName(),
				view.getAmount(),
				Boolean.TRUE.equals(view.getPaid()),
				view.getPaymentMethod() == null ? null : MassagePaymentMethod.valueOf(view.getPaymentMethod()),
				view.getPaymentDate(),
				view.getPaymentNotes(),
				MassageBookingStatus.valueOf(view.getStatus()),
				view.getCancellationNotes()
		);
	}

	private MassageProviderSummaryDto emptySummary(MassageProvider provider) {
		final int therapistsCount = massageTherapistRepository.countByProviderIdIn(List.of(provider.getId())).stream()
				.findFirst()
				.map(MassageTherapistCountView::getTherapistsCount)
				.map(Math::toIntExact)
				.orElse(0);
		return new MassageProviderSummaryDto(
				provider.getId(),
				provider.getName(),
				provider.isActive(),
				therapistsCount,
				0,
				0,
				0,
				0,
				0,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null
		);
	}

	private BigDecimal safeAmount(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
