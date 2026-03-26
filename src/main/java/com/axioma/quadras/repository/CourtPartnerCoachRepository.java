package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtPartnerCoach;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtPartnerCoachRepository extends JpaRepository<CourtPartnerCoach, Long> {

	List<CourtPartnerCoach> findAllByActiveTrueOrderByNameAsc();

	List<CourtPartnerCoach> findAllByOrderByNameAsc();

	boolean existsByNameIgnoreCaseAndActiveTrue(String name);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
