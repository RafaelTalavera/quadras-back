package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryOpeningStock;
import com.axioma.quadras.domain.model.LaundryOpeningStockStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryOpeningStockRepository extends JpaRepository<LaundryOpeningStock, Long> {
	boolean existsByStatus(LaundryOpeningStockStatus status);
}
