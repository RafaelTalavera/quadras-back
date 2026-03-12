package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateReservationDto;
import com.axioma.quadras.domain.dto.ReservationDto;
import com.axioma.quadras.domain.dto.UpdateReservationDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.Reservation;
import com.axioma.quadras.domain.model.ReservationRules;
import com.axioma.quadras.domain.model.ReservationStatus;
import com.axioma.quadras.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

	private final ReservationRepository reservationRepository;

	public ReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Transactional
	public ReservationDto create(CreateReservationDto input) {
		final Reservation reservation = Reservation.schedule(
				input.guestName(),
				input.reservationDate(),
				input.startTime(),
				input.endTime(),
				input.notes()
		);
		validateBusinessRules(reservation);
		validateOverlapping(reservation, null);
		final Reservation saved = reservationRepository.save(reservation);
		return ReservationDto.from(saved);
	}

	@Transactional
	public ReservationDto update(Long reservationId, UpdateReservationDto input) {
		final Reservation reservation = findReservationOrThrow(reservationId);
		validateCanEdit(reservation);
		reservation.reschedule(
				input.guestName(),
				input.reservationDate(),
				input.startTime(),
				input.endTime(),
				input.notes()
		);
		validateBusinessRules(reservation);
		validateOverlapping(reservation, reservationId);
		return ReservationDto.from(reservation);
	}

	@Transactional
	public ReservationDto cancel(Long reservationId) {
		final Reservation reservation = findReservationOrThrow(reservationId);
		if (reservation.getStatus() == ReservationStatus.COMPLETED) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Completed reservations cannot be cancelled."
			);
		}
		if (reservation.getStatus() == ReservationStatus.CANCELLED) {
			return ReservationDto.from(reservation);
		}
		reservation.markCancelled();
		return ReservationDto.from(reservation);
	}

	public List<ReservationDto> list(LocalDate reservationDate) {
		final List<Reservation> reservations;
		if (reservationDate == null) {
			reservations = reservationRepository.findAllOrderedByDateAndStartTime();
		} else {
			reservations = reservationRepository.findAllByReservationDateOrderByStartTimeAsc(reservationDate);
		}
		return reservations.stream().map(ReservationDto::from).toList();
	}

	public ReservationDto findById(Long reservationId) {
		final Reservation reservation = findReservationOrThrow(reservationId);
		return ReservationDto.from(reservation);
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
}
