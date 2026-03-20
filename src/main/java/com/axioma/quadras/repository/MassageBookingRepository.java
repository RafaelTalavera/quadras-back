package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageBooking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MassageBookingRepository
		extends JpaRepository<MassageBooking, Long>, JpaSpecificationExecutor<MassageBooking> {

	boolean existsByProviderIdAndBookingDateAndStartTime(
			Long providerId,
			LocalDate bookingDate,
			LocalTime startTime
	);

	List<MassageBooking> findAllByBookingDateOrderByStartTimeAsc(LocalDate bookingDate);

	default List<MassageBooking> findAllOrderedByDateAndTime() {
		return findAll(Sort.by(
				Sort.Order.asc("bookingDate"),
				Sort.Order.asc("startTime")
		));
	}

	default List<MassageBooking> findAllOrderedByDateAndTime(
			org.springframework.data.jpa.domain.Specification<MassageBooking> specification
	) {
		return findAll(specification, Sort.by(
				Sort.Order.asc("bookingDate"),
				Sort.Order.asc("startTime")
		));
	}
}
