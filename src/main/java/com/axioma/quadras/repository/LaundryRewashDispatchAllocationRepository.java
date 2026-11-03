package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryRewashDispatchAllocation;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryRewashDispatchAllocationRepository
		extends JpaRepository<LaundryRewashDispatchAllocation, Long> {
	@Query("select coalesce(sum(allocation.quantity), 0) from LaundryRewashDispatchAllocation allocation where allocation.obligation.id = :obligationId")
	BigDecimal sumAllocated(@Param("obligationId") Long obligationId);
}
