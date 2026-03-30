package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceProviderRepository extends JpaRepository<MaintenanceProvider, Long> {

	boolean existsByProviderTypeAndNameIgnoreCase(MaintenanceProviderType providerType, String name);

	boolean existsByProviderTypeAndNameIgnoreCaseAndIdNot(
			MaintenanceProviderType providerType,
			String name,
			Long id
	);

	List<MaintenanceProvider> findAllByOrderByProviderTypeAscNameAsc();
}
