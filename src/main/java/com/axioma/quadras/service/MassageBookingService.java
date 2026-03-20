package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateMassageBookingDto;
import com.axioma.quadras.domain.dto.MassageBookingDto;
import com.axioma.quadras.domain.dto.UpdateMassagePaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.repository.MassageBookingRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageBookingService {

	private final MassageBookingRepository massageBookingRepository;
	private final MassageProviderService massageProviderService;

	public MassageBookingService(
			MassageBookingRepository massageBookingRepository,
			MassageProviderService massageProviderService
	) {
		this.massageBookingRepository = massageBookingRepository;
		this.massageProviderService = massageProviderService;
	}

	@Transactional
	public MassageBookingDto create(CreateMassageBookingDto input) {
		final MassageProvider provider = massageProviderService.findProviderOrThrow(input.providerId());
		if (!provider.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Inactive massage providers cannot receive bookings."
			);
		}
		final boolean duplicated = massageBookingRepository.existsByProviderIdAndBookingDateAndStartTime(
				provider.getId(),
				input.bookingDate(),
				input.startTime()
		);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Massage provider already has a booking for the selected date and time."
			);
		}

		final MassageBooking saved = massageBookingRepository.save(
				MassageBooking.schedule(
						input.bookingDate(),
						input.startTime(),
						input.clientName(),
						input.guestReference(),
						input.treatment(),
						input.amount(),
						provider,
						input.paid(),
						input.paymentMethod(),
						input.paymentDate(),
						input.paymentNotes()
				)
		);
		return MassageBookingDto.from(saved);
	}

	public List<MassageBookingDto> list(
			LocalDate bookingDate,
			String clientName,
			String guestReference,
			Long providerId,
			Boolean paid
	) {
		final Specification<MassageBooking> specification = withFilters(
				bookingDate,
				clientName,
				guestReference,
				providerId,
				paid
		);
		final List<MassageBooking> bookings = massageBookingRepository.findAllOrderedByDateAndTime(
				specification
		);
		return bookings.stream().map(MassageBookingDto::from).toList();
	}

	@Transactional
	public MassageBookingDto updatePayment(Long bookingId, UpdateMassagePaymentDto input) {
		final MassageBooking booking = massageBookingRepository.findById(bookingId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Massage booking " + bookingId + " not found"
				));
		booking.markPayment(
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes()
		);
		return MassageBookingDto.from(booking);
	}

	private Specification<MassageBooking> withFilters(
			LocalDate bookingDate,
			String clientName,
			String guestReference,
			Long providerId,
			Boolean paid
	) {
		return Specification
				.where(hasBookingDate(bookingDate))
				.and(containsIgnoreCase("clientName", clientName))
				.and(containsIgnoreCase("guestReference", guestReference))
				.and(hasProviderId(providerId))
				.and(hasPaid(paid));
	}

	private Specification<MassageBooking> hasBookingDate(LocalDate bookingDate) {
		return (root, query, builder) -> bookingDate == null
				? null
				: builder.equal(root.get("bookingDate"), bookingDate);
	}

	private Specification<MassageBooking> containsIgnoreCase(String field, String value) {
		return (root, query, builder) -> {
			if (value == null || value.isBlank()) {
				return null;
			}
			return builder.like(
					builder.lower(root.get(field)),
					"%" + value.trim().toLowerCase() + "%"
			);
		};
	}

	private Specification<MassageBooking> hasProviderId(Long providerId) {
		return (root, query, builder) -> providerId == null
				? null
				: builder.equal(root.get("provider").get("id"), providerId);
	}

	private Specification<MassageBooking> hasPaid(Boolean paid) {
		return (root, query, builder) -> paid == null
				? null
				: builder.equal(root.get("paid"), paid);
	}
}
