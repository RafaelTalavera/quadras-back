package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CreateReservationDto;
import com.axioma.quadras.domain.dto.ReservationDto;
import com.axioma.quadras.domain.dto.UpdateReservationDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.Reservation;
import com.axioma.quadras.domain.model.ReservationRules;
import com.axioma.quadras.domain.model.ReservationStatus;
import com.axioma.quadras.repository.ReservationListItemView;
import com.axioma.quadras.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final ScheduleLockService scheduleLockService;
	private final ScheduleSyncEventPublisher scheduleSyncEventPublisher;
	private final AuditTrailService auditTrailService;

	public ReservationService(
			ReservationRepository reservationRepository,
			ScheduleLockService scheduleLockService,
			ScheduleSyncEventPublisher scheduleSyncEventPublisher,
			AuditTrailService auditTrailService
	) {
		this.reservationRepository = reservationRepository;
		this.scheduleLockService = scheduleLockService;
		this.scheduleSyncEventPublisher = scheduleSyncEventPublisher;
		this.auditTrailService = auditTrailService;
	}

	@Transactional
	public ReservationDto create(CreateReservationDto input, String actorUsername) {
		final Reservation reservation = Reservation.schedule(
				input.guestName(),
				input.reservationDate(),
				input.startTime(),
				input.endTime(),
				input.notes(),
				actorUsername
		);
		validateBusinessRules(reservation);
		scheduleLockService.acquireReservationDates(List.of(reservation.getReservationDate()));
		validateOverlapping(reservation, null);
		final Reservation saved = reservationRepository.save(reservation);
		auditTrailService.record(
				"reservations",
				"reservation",
				saved.getId(),
				"CREATED",
				"Reserva creada",
				List.of(),
				null,
				snapshot(saved)
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.RESERVATIONS,
				"created",
				saved.getId(),
				saved.getReservationDate(),
				saved.getReservationDate()
		);
		return ReservationDto.from(saved);
	}

	@Transactional
	public ReservationDto update(Long reservationId, UpdateReservationDto input, String actorUsername) {
		final Reservation reservation = findReservationOrThrow(reservationId);
		final Map<String, Object> beforeState = snapshot(reservation);
		final LocalDate previousReservationDate = reservation.getReservationDate();
		validateCanEdit(reservation);
		reservation.reschedule(
				input.guestName(),
				input.reservationDate(),
				input.startTime(),
				input.endTime(),
				input.notes(),
				actorUsername
		);
		validateBusinessRules(reservation);
		scheduleLockService.acquireReservationDates(List.of(
				previousReservationDate,
				reservation.getReservationDate()
		));
		validateOverlapping(reservation, reservationId);
		auditTrailService.record(
				"reservations",
				"reservation",
				reservation.getId(),
				"UPDATED",
				"Reserva actualizada",
				diff(beforeState, snapshot(reservation)),
				beforeState,
				snapshot(reservation)
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.RESERVATIONS,
				"updated",
				reservation.getId(),
				minDate(previousReservationDate, reservation.getReservationDate()),
				maxDate(previousReservationDate, reservation.getReservationDate())
		);
		return ReservationDto.from(reservation);
	}

	@Transactional
	public ReservationDto cancel(Long reservationId, String actorUsername) {
		final Reservation reservation = findReservationOrThrow(reservationId);
		final Map<String, Object> beforeState = snapshot(reservation);
		if (reservation.getStatus() == ReservationStatus.COMPLETED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Completed reservations cannot be cancelled."
			);
		}
		if (reservation.getStatus() == ReservationStatus.CANCELLED) {
			return ReservationDto.from(reservation);
		}
		reservation.markCancelled(actorUsername);
		auditTrailService.record(
				"reservations",
				"reservation",
				reservation.getId(),
				"CANCELLED",
				"Reserva cancelada",
				diff(beforeState, snapshot(reservation)),
				beforeState,
				snapshot(reservation)
		);
		scheduleSyncEventPublisher.publish(
				ScheduleSyncDomain.RESERVATIONS,
				"cancelled",
				reservation.getId(),
				reservation.getReservationDate(),
				reservation.getReservationDate()
		);
		return ReservationDto.from(reservation);
	}

	public List<ReservationDto> list(LocalDate reservationDate) {
		final List<ReservationListItemView> reservations;
		if (reservationDate == null) {
			reservations = reservationRepository.findAllByOrderByReservationDateAscStartTimeAsc();
		} else {
			reservations = reservationRepository.findAllByReservationDateOrderByStartTimeAsc(reservationDate);
		}
		return reservations.stream().map(ReservationDto::from).toList();
	}

	public ReservationDto findById(Long reservationId) {
		final Reservation reservation = findReservationOrThrow(reservationId);
		return ReservationDto.from(reservation);
	}

	public List<AuditEventDto> audit(Long reservationId) {
		findReservationOrThrow(reservationId);
		return auditTrailService.findByEntity("reservation", reservationId);
	}

	private Reservation findReservationOrThrow(Long reservationId) {
		return reservationRepository.findById(reservationId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Reservation " + reservationId + " not found"
				));
	}

	private void validateBusinessRules(Reservation reservation) {
		if (!ReservationRules.isWithinOperatingHours(
				reservation.getStartTime(),
				reservation.getEndTime()
		)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Reservation must be within operating hours 07:00 to 23:00."
			);
		}

		final long durationInMinutes = reservation.durationInMinutes();
		if (!ReservationRules.isAllowedDuration(durationInMinutes)) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Reservation duration must be 60, 90 or 120 minutes."
			);
		}
	}

	private void validateCanEdit(Reservation reservation) {
		if (reservation.getStatus() == ReservationStatus.CANCELLED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Cancelled reservations cannot be edited."
			);
		}
		if (reservation.getStatus() == ReservationStatus.COMPLETED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Completed reservations cannot be edited."
			);
		}
	}

	private void validateOverlapping(Reservation reservation, Long excludedReservationId) {
		final boolean overlaps;
		if (excludedReservationId == null) {
			overlaps = reservationRepository.existsByReservationDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
					reservation.getReservationDate(),
					ReservationStatus.CANCELLED,
					reservation.getEndTime(),
					reservation.getStartTime()
			);
		} else {
			overlaps = reservationRepository.existsByReservationDateAndStatusNotAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
					reservation.getReservationDate(),
					ReservationStatus.CANCELLED,
					excludedReservationId,
					reservation.getEndTime(),
					reservation.getStartTime()
			);
		}

		if (overlaps) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Reservation overlaps with an existing booking."
			);
		}
	}

	private LocalDate minDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? right : left;
	}

	private LocalDate maxDate(LocalDate left, LocalDate right) {
		return left.isAfter(right) ? left : right;
	}

	private Map<String, Object> snapshot(Reservation reservation) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", reservation.getId());
		snapshot.put("guestName", reservation.getGuestName());
		snapshot.put("reservationDate", toValue(reservation.getReservationDate()));
		snapshot.put("startTime", toValue(reservation.getStartTime()));
		snapshot.put("endTime", toValue(reservation.getEndTime()));
		snapshot.put("status", reservation.getStatus() == null ? null : reservation.getStatus().name());
		snapshot.put("notes", reservation.getNotes());
		snapshot.put("createdAt", toValue(reservation.getCreatedAt()));
		snapshot.put("updatedAt", toValue(reservation.getUpdatedAt()));
		snapshot.put("createdBy", reservation.getCreatedBy());
		snapshot.put("updatedBy", reservation.getUpdatedBy());
		snapshot.put("cancelledAt", toValue(reservation.getCancelledAt()));
		snapshot.put("cancelledBy", reservation.getCancelledBy());
		return snapshot;
	}

	private List<Map<String, Object>> diff(Map<String, Object> before, Map<String, Object> after) {
		return before.keySet().stream()
				.filter(field -> !equalsValue(before.get(field), after.get(field)))
				.map(field -> {
					final Map<String, Object> change = new LinkedHashMap<>();
					change.put("field", field);
					change.put("before", before.get(field));
					change.put("after", after.get(field));
					return change;
				})
				.toList();
	}

	private boolean equalsValue(Object left, Object right) {
		return java.util.Objects.equals(left, right);
	}

	private String toValue(Object value) {
		return value == null ? null : value.toString();
	}
}
