package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CancelMassageBookingDto;
import com.axioma.quadras.domain.dto.CreateMassageBookingDto;
import com.axioma.quadras.domain.dto.MassageBookingDto;
import com.axioma.quadras.domain.dto.UpdateMassageBookingDto;
import com.axioma.quadras.domain.dto.UpdateMassagePaymentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.repository.MassageBookingListItemView;
import com.axioma.quadras.repository.MassageBookingRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageBookingService {

	private final MassageBookingRepository massageBookingRepository;
	private final MassageProviderService massageProviderService;
	private final ScheduleLockService scheduleLockService;

	public MassageBookingService(
			MassageBookingRepository massageBookingRepository,
			MassageProviderService massageProviderService,
			ScheduleLockService scheduleLockService
	) {
		this.massageBookingRepository = massageBookingRepository;
		this.massageProviderService = massageProviderService;
		this.scheduleLockService = scheduleLockService;
	}

	@Transactional
	public MassageBookingDto create(CreateMassageBookingDto input, String actorUsername) {
		final MassageProvider provider = massageProviderService.findProviderOrThrow(input.providerId());
		final MassageTherapist therapist = massageProviderService.findTherapistOrThrow(
				provider.getId(),
				input.therapistId()
		);
		if (!provider.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Inactive massage providers cannot receive bookings."
			);
		}
		if (!therapist.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Inactive massage therapists cannot receive bookings."
			);
		}
		scheduleLockService.acquireMassageTherapistDates(List.of(
				new ScheduleLockService.MassageTherapistScheduleKey(
						therapist.getId(),
						input.bookingDate()
				)
		));
		final boolean duplicated = massageBookingRepository.existsByTherapistIdAndBookingDateAndStartTimeAndStatus(
				therapist.getId(),
				input.bookingDate(),
				input.startTime(),
				MassageBookingStatus.SCHEDULED
		);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Massage therapist already has a booking for the selected date and time."
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
						therapist,
						input.paid(),
						input.paymentMethod(),
						input.paymentDate(),
						input.paymentNotes(),
						actorUsername
				)
		);
		return MassageBookingDto.from(saved);
	}

	@Transactional
	public MassageBookingDto update(
			Long bookingId,
			UpdateMassageBookingDto input,
			String actorUsername
	) {
		final MassageBooking booking = findBookingOrThrow(bookingId);
		final Long previousTherapistId = booking.getTherapist().getId();
		final LocalDate previousBookingDate = booking.getBookingDate();
		validateCanEdit(booking);
		final MassageProvider provider = massageProviderService.findProviderOrThrow(input.providerId());
		final MassageTherapist therapist = massageProviderService.findTherapistOrThrow(
				provider.getId(),
				input.therapistId()
		);
		if (!provider.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Inactive massage providers cannot receive bookings."
			);
		}
		if (!therapist.isActive()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Inactive massage therapists cannot receive bookings."
			);
		}
		scheduleLockService.acquireMassageTherapistDates(List.of(
				new ScheduleLockService.MassageTherapistScheduleKey(previousTherapistId, previousBookingDate),
				new ScheduleLockService.MassageTherapistScheduleKey(therapist.getId(), input.bookingDate())
		));
		final boolean duplicated =
				massageBookingRepository.existsByTherapistIdAndBookingDateAndStartTimeAndStatusAndIdNot(
						therapist.getId(),
						input.bookingDate(),
						input.startTime(),
						MassageBookingStatus.SCHEDULED,
						bookingId
				);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Massage therapist already has a booking for the selected date and time."
			);
		}

		booking.updateBooking(
				input.bookingDate(),
				input.startTime(),
				input.clientName(),
				input.guestReference(),
				input.treatment(),
				input.amount(),
				provider,
				therapist,
				input.paid(),
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				actorUsername
		);
		return MassageBookingDto.from(booking);
	}

	public List<MassageBookingDto> list(
			LocalDate bookingDate,
			String clientName,
			String guestReference,
			Long providerId,
			Boolean paid
	) {
		final List<MassageBookingListItemView> bookings = massageBookingRepository.findListItems(
				bookingDate,
				normalizeFilter(clientName),
				normalizeFilter(guestReference),
				providerId,
				paid
		);
		return bookings.stream().map(MassageBookingDto::from).toList();
	}

	@Transactional
	public MassageBookingDto updatePayment(
			Long bookingId,
			UpdateMassagePaymentDto input,
			String actorUsername
	) {
		final MassageBooking booking = findBookingOrThrow(bookingId);
		validateCanEdit(booking);
		booking.markPayment(
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				actorUsername
		);
		return MassageBookingDto.from(booking);
	}

	@Transactional
	public MassageBookingDto cancel(
			Long bookingId,
			CancelMassageBookingDto input,
			String actorUsername
	) {
		final MassageBooking booking = findBookingOrThrow(bookingId);
		if (booking.getStatus() == MassageBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled massage bookings cannot be cancelled again."
			);
		}
		booking.markCancelled(input.cancellationNotes(), actorUsername);
		return MassageBookingDto.from(booking);
	}

	private MassageBooking findBookingOrThrow(Long bookingId) {
		return massageBookingRepository.findById(bookingId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Massage booking " + bookingId + " not found"
				));
	}

	private void validateCanEdit(MassageBooking booking) {
		if (booking.getStatus() == MassageBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled massage bookings cannot be edited."
			);
		}
	}

	private String normalizeFilter(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim().toLowerCase();
	}
}
