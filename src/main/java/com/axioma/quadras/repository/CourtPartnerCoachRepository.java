package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtPartnerCoach;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtPartnerCoachRepository extends JpaRepository<CourtPartnerCoach, Long> {

	List<CourtPartnerCoachListItemView> findAllByActiveTrueOrderByNameAsc();

	List<CourtPartnerCoachListItemView> findAllByOrderByNameAsc();

	boolean existsByNameIgnoreCaseAndActiveTrue(String name);

	Optional<CourtPartnerCoach> findByNameIgnoreCaseAndActiveTrue(String name);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
