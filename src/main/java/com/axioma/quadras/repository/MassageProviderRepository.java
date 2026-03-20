package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageProvider;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MassageProviderRepository extends JpaRepository<MassageProvider, Long> {

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	default List<MassageProvider> findAllOrderedByName() {
		return findAll(Sort.by(Sort.Order.asc("name")));
	}

	List<MassageProvider> findAllByActiveTrueOrderByNameAsc();
}
