package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceProviderRepository extends JpaRepository<MaintenanceProvider, Long> {

	boolean existsByProviderTypeAndNameIgnoreCase(MaintenanceProviderType providerType, String name);

	boolean existsByProviderTypeAndNameIgnoreCaseAndIdNot(
			MaintenanceProviderType providerType,
			String name,
			Long id
	);

	@Modifying
	@Query("""
			delete from MaintenanceProvider p
			where lower(p.createdBy) like lower(concat(:prefix, '%'))
			""")
	int deleteInBulkByCreatedByPrefix(@Param("prefix") String prefix);

	List<MaintenanceProvider> findAllByOrderByProviderTypeAscNameAsc();

	List<MaintenanceProviderListItemView> findAllProjectedByOrderByProviderTypeAscNameAsc();
}
