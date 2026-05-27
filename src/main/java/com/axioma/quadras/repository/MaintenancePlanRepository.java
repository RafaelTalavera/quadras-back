package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePlan;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {

	@Query("""
			select p from MaintenancePlan p
			left join fetch p.location
			left join fetch p.provider
			order by p.active desc, p.nextDueDate asc, lower(p.title) asc, p.id asc
			""")
	List<MaintenancePlan> findAllDetailed();

	@Query("""
			select count(o) > 0 from MaintenanceOrder o
			where o.plan.id = :planId
			  and o.status in :statuses
			""")
	boolean existsActiveOrders(
			@Param("planId") Long planId,
			@Param("statuses") Collection<MaintenanceOrderStatus> statuses
	);
}
