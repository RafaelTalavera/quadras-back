package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtBookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourtBookingRepository
		extends JpaRepository<CourtBooking, Long>, JpaSpecificationExecutor<CourtBooking> {

	boolean existsByBookingDateAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate bookingDate,
			CourtBookingStatus status,
			LocalTime endTime,
			LocalTime startTime
	);

	boolean existsByBookingDateAndStatusAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate bookingDate,
			CourtBookingStatus status,
			Long id,
			LocalTime endTime,
			LocalTime startTime
	);

	default List<CourtBooking> findAllOrderedByDateAndTime(
			org.springframework.data.jpa.domain.Specification<CourtBooking> specification
	) {
		return findAll(specification, Sort.by(
				Sort.Order.asc("bookingDate"),
				Sort.Order.asc("startTime")
		));
	}
}
