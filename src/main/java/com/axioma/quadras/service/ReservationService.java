package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateReservationDto;
import com.axioma.quadras.domain.dto.ReservationDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.Reservation;
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
		final Reservation saved = reservationRepository.save(reservation);
		return ReservationDto.from(saved);
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
		final Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Reservation " + reservationId + " not found"
				));
		return ReservationDto.from(reservation);
	}
}
