package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceLocationRepository extends JpaRepository<MaintenanceLocation, Long> {

	boolean existsByLocationTypeAndCodeIgnoreCase(MaintenanceLocationType locationType, String code);

	boolean existsByLocationTypeAndCodeIgnoreCaseAndIdNot(
			MaintenanceLocationType locationType,
			String code,
			Long id
	);

	List<MaintenanceLocation> findAllByOrderByLocationTypeAscCodeAsc();
}
