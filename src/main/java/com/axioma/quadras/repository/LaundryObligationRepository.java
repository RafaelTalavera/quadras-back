package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryObligation;
import com.axioma.quadras.domain.model.LaundryObligationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface LaundryObligationRepository extends JpaRepository<LaundryObligation, Long> {
	List<LaundryObligation> findByProviderIdAndStatusOrderByEffectiveAtAscIdAsc(
			Long providerId,
			LaundryObligationStatus status
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select obligation from LaundryObligation obligation
			where obligation.provider.id = :providerId
			  and obligation.item.id = :itemId
			  and obligation.status = com.axioma.quadras.domain.model.LaundryObligationStatus.OPEN
			  and obligation.inRewashQuantity > 0
			order by obligation.effectiveAt asc, obligation.createdAt asc, obligation.id asc
			""")
	List<LaundryObligation> findRewashForUpdate(@Param("providerId") Long providerId,
			@Param("itemId") Long itemId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select obligation from LaundryObligation obligation
			where obligation.provider.id = :providerId
			  and obligation.item.id = :itemId
			  and obligation.status = com.axioma.quadras.domain.model.LaundryObligationStatus.OPEN
			order by obligation.effectiveAt asc, obligation.createdAt asc, obligation.id asc
			""")
	List<LaundryObligation> findOpenForUpdate(@Param("providerId") Long providerId,
			@Param("itemId") Long itemId);
}
