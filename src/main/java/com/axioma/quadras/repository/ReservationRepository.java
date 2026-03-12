package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	boolean existsByReservationDateAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate reservationDate,
			LocalTime endTime,
			LocalTime startTime
	);
}
