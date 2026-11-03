package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryObligation;
import com.axioma.quadras.domain.model.LaundryObligationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryObligationRepository extends JpaRepository<LaundryObligation, Long> {
	List<LaundryObligation> findByProviderIdAndStatusOrderByEffectiveAtAscIdAsc(
			Long providerId,
			LaundryObligationStatus status
	);
}
