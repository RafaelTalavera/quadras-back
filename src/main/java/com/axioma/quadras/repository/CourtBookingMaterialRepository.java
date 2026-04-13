package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtBookingMaterial;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourtBookingMaterialRepository extends JpaRepository<CourtBookingMaterial, Long> {

	@Query("""
			select
				m.booking.id as bookingId,
				m.materialCode as materialCode,
				m.materialLabel as materialLabel,
				m.quantity as quantity,
				m.unitPrice as unitPrice,
				m.totalPrice as totalPrice
			from CourtBookingMaterial m
			where m.booking.id in :bookingIds
			order by m.booking.id asc, m.id asc
			""")
	List<CourtBookingMaterialListItemView> findListItemsByBookingIdIn(
			@Param("bookingIds") Collection<Long> bookingIds
	);
}
