package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryItemRepository extends JpaRepository<LaundryItem, Long> {
	List<LaundryItem> findAllByOrderByNameAsc();
	List<LaundryItem> findAllByActiveTrueOrderByNameAsc();
	boolean existsByNameIgnoreCase(String name);
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
