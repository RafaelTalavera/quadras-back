package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import com.axioma.quadras.domain.model.CourtRate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRateRepository extends JpaRepository<CourtRate, Long> {

	Optional<CourtRate> findByCustomerTypeAndPricingPeriod(
			CourtCustomerType customerType,
			CourtPricingPeriod pricingPeriod
	);

	List<CourtRateListItemView> findAllByOrderByCustomerTypeAscPricingPeriodAsc();
}
