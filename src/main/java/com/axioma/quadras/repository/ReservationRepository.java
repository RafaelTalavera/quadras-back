package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.Reservation;
import com.axioma.quadras.domain.model.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	boolean existsByReservationDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate reservationDate,
			ReservationStatus status,
			LocalTime endTime,
			LocalTime startTime
	);

	boolean existsByReservationDateAndStatusNotAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate reservationDate,
			ReservationStatus status,
			Long reservationId,
			LocalTime endTime,
			LocalTime startTime
	);

	List<ReservationListItemView> findAllByReservationDateOrderByStartTimeAsc(LocalDate reservationDate);

	List<ReservationListItemView> findAllByOrderByReservationDateAscStartTimeAsc();
}
