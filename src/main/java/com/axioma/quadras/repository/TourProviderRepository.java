package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourProviderRepository extends JpaRepository<TourProvider, Long> {

	List<TourProvider> findAllByOrderByNameAsc();

	List<TourProvider> findAllByActiveTrueOrderByNameAsc();

	boolean existsByNameIgnoreCaseAndContactIgnoreCase(String name, String contact);

	boolean existsByNameIgnoreCaseAndContactIgnoreCaseAndIdNot(
			String name,
			String contact,
			Long excludedId
	);
}
