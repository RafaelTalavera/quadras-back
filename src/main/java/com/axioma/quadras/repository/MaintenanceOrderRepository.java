package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {

	@Query("""
			select o from MaintenanceOrder o
			where o.location.id = :locationId
			  and o.scheduledStartAt is not null
			  and o.scheduledEndAt is not null
			  and o.status in :statuses
			  and (:excludeOrderId is null or o.id <> :excludeOrderId)
			  and o.scheduledStartAt < :scheduledEndAt
			  and o.scheduledEndAt > :scheduledStartAt
			order by o.scheduledStartAt asc
			""")
	List<MaintenanceOrder> findScheduleConflicts(
			@Param("locationId") Long locationId,
			@Param("scheduledStartAt") LocalDateTime scheduledStartAt,
			@Param("scheduledEndAt") LocalDateTime scheduledEndAt,
			@Param("excludeOrderId") Long excludeOrderId,
			@Param("statuses") Collection<MaintenanceOrderStatus> statuses
	);

	List<MaintenanceOrder> findByLocationIdOrderByReportedAtDescIdDesc(Long locationId);

	default List<MaintenanceOrder> findAllOrdered() {
		return findAll(Sort.by(
				Sort.Order.asc("scheduledStartAt"),
				Sort.Order.asc("reportedAt"),
				Sort.Order.asc("id")
		));
	}
}
