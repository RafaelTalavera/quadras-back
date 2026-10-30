package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryProviderRepository extends JpaRepository<LaundryProvider, Long> {
	List<LaundryProvider> findAllByOrderByNameAsc();
	boolean existsByNameIgnoreCase(String name);
}
