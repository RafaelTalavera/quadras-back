package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageBookingService {

	private final MassageBookingRepository massageBookingRepository;
	private final MassageProviderService massageProviderService;
	private final ScheduleLockService scheduleLockService;
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public MassageBookingService(
			MassageBookingRepository massageBookingRepository,
			MassageProviderService massageProviderService,
			ScheduleLockService scheduleLockService,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.massageBookingRepository = massageBookingRepository;
		this.massageProviderService = massageProviderService;
		this.scheduleLockService = scheduleLockService;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
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
		auditTrailService.record(
				"massages",
				"massage-booking",
				saved.getId(),
				"CREATED",
				"Atencion de masaje creada",
				List.of(),
				null,
				snapshot(saved)
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MASSAGES,
				"created",
				saved.getId(),
				saved.getBookingDate(),
				saved.getBookingDate()
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
		final Map<String, Object> beforeState = snapshot(booking);
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
		final Map<String, Object> afterState = snapshot(booking);
		auditTrailService.record(
				"massages",
				"massage-booking",
				booking.getId(),
				"UPDATED",
				"Atencion de masaje actualizada",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MASSAGES,
				"updated",
				booking.getId(),
				minDate(previousBookingDate, booking.getBookingDate()),
				maxDate(previousBookingDate, booking.getBookingDate())
		);
		return MassageBookingDto.from(booking);
	}

	public List<MassageBookingDto> list(
			LocalDate bookingDate,
			LocalDate dateFrom,
			LocalDate dateTo,
			String clientName,
			String guestReference,
			Long providerId,
			Boolean paid
	) {
		validateRange(dateFrom, dateTo);
		final LocalDate effectiveDateFrom = bookingDate != null ? bookingDate : dateFrom;
		final LocalDate effectiveDateTo = bookingDate != null ? bookingDate : dateTo;
		final List<MassageBookingListItemView> bookings = massageBookingRepository.findListItems(
				effectiveDateFrom,
				effectiveDateTo,
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
		final Map<String, Object> beforeState = snapshot(booking);
		validateCanEdit(booking);
		booking.markPayment(
				input.paymentMethod(),
				input.paymentDate(),
				input.paymentNotes(),
				actorUsername
		);
		final Map<String, Object> afterState = snapshot(booking);
		auditTrailService.record(
				"massages",
				"massage-booking",
				booking.getId(),
				"PAYMENT_UPDATED",
				"Pago de masaje actualizado",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MASSAGES,
				"payment-updated",
				booking.getId(),
				booking.getBookingDate(),
				booking.getBookingDate()
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
		final Map<String, Object> beforeState = snapshot(booking);
		if (booking.getStatus() == MassageBookingStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled massage bookings cannot be cancelled again."
			);
		}
		booking.markCancelled(input.cancellationNotes(), actorUsername);
		final Map<String, Object> afterState = snapshot(booking);
		auditTrailService.record(
				"massages",
				"massage-booking",
				booking.getId(),
				"CANCELLED",
				"Atencion de masaje cancelada",
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.MASSAGES,
				"cancelled",
				booking.getId(),
				booking.getBookingDate(),
				booking.getBookingDate()
		);
		return MassageBookingDto.from(booking);
	}

	public List<AuditEventDto> audit(Long bookingId) {
		findBookingOrThrow(bookingId);
		return auditTrailService.findByEntity("massage-booking", bookingId);
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

	private void validateRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"dateFrom must be before or equal to dateTo"
			);
		}
	}

	private LocalDate minDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? right : left;
	}

	private LocalDate maxDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? left : right;
	}

	private Map<String, Object> snapshot(MassageBooking booking) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", booking.getId());
		snapshot.put("bookingDate", toValue(booking.getBookingDate()));
		snapshot.put("startTime", toValue(booking.getStartTime()));
		snapshot.put("clientName", booking.getClientName());
		snapshot.put("guestReference", booking.getGuestReference());
		snapshot.put("treatment", booking.getTreatment());
		snapshot.put("amount", toValue(booking.getAmount()));
		snapshot.put("providerId", booking.getProvider() == null ? null : booking.getProvider().getId());
		snapshot.put("providerName", booking.getProvider() == null ? null : booking.getProvider().getName());
		snapshot.put("therapistId", booking.getTherapist() == null ? null : booking.getTherapist().getId());
		snapshot.put("therapistName", booking.getTherapist() == null ? null : booking.getTherapist().getName());
		snapshot.put("status", booking.getStatus() == null ? null : booking.getStatus().name());
		snapshot.put("paid", booking.isPaid());
		snapshot.put("paymentMethod", booking.getPaymentMethod() == null ? null : booking.getPaymentMethod().name());
		snapshot.put("paymentDate", toValue(booking.getPaymentDate()));
		snapshot.put("paymentNotes", booking.getPaymentNotes());
		snapshot.put("cancellationNotes", booking.getCancellationNotes());
		snapshot.put("createdAt", toValue(booking.getCreatedAt()));
		snapshot.put("updatedAt", toValue(booking.getUpdatedAt()));
		snapshot.put("cancelledAt", toValue(booking.getCancelledAt()));
		snapshot.put("createdBy", booking.getCreatedBy());
		snapshot.put("updatedBy", booking.getUpdatedBy());
		snapshot.put("cancelledBy", booking.getCancelledBy());
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
