package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.Reservation;
import com.axioma.quadras.domain.model.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	boolean existsByReservationDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate reservationDate,
			ReservationStatus status,
			LocalTime endTime,
			LocalTime startTime
	);

	List<Reservation> findAllByReservationDateOrderByStartTimeAsc(LocalDate reservationDate);

	default List<Reservation> findAllOrderedByDateAndStartTime() {
		return findAll(Sort.by(
				Sort.Order.asc("reservationDate"),
				Sort.Order.asc("startTime")
		));
	}
}
