package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceLocationRepository extends JpaRepository<MaintenanceLocation, Long> {

	boolean existsByLocationTypeAndCodeIgnoreCase(MaintenanceLocationType locationType, String code);

	boolean existsByLocationTypeAndCodeIgnoreCaseAndIdNot(
			MaintenanceLocationType locationType,
			String code,
			Long id
	);

	@Modifying
	@Query("""
			delete from MaintenanceLocation l
			where lower(l.createdBy) like lower(concat(:prefix, '%'))
			""")
	int deleteInBulkByCreatedByPrefix(@Param("prefix") String prefix);

	List<MaintenanceLocation> findAllByOrderByLocationTypeAscCodeAsc();

	List<MaintenanceLocationListItemView> findAllProjectedByOrderByLocationTypeAscCodeAsc();
}
