package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourBooking;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TourBookingRepository
		extends JpaRepository<TourBooking, Long>, JpaSpecificationExecutor<TourBooking> {

	default List<TourBooking> findAllOrderedByStartAt(
			org.springframework.data.jpa.domain.Specification<TourBooking> specification
	) {
		return findAll(specification, Sort.by(
				Sort.Order.asc("startAt"),
				Sort.Order.asc("id")
		));
	}
}
