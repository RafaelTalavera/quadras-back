package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryReturnAllocation;
import com.axioma.quadras.domain.model.LaundryReturnOutcome;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryReturnAllocationRepository extends JpaRepository<LaundryReturnAllocation, Long> {
	@Query("""
			select coalesce(sum(allocation.quantity), 0) from LaundryReturnAllocation allocation
			where allocation.obligation.id = :obligationId and allocation.outcome = :outcome
			""")
	BigDecimal sumQuantity(@Param("obligationId") Long obligationId,
			@Param("outcome") LaundryReturnOutcome outcome);
}
