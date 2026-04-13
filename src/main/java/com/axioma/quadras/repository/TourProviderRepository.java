package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourProviderRepository extends JpaRepository<TourProvider, Long> {

	@Query("""
			select
			    p.id as id,
			    p.name as name,
			    p.contact as contact,
			    p.defaultCommissionPercent as defaultCommissionPercent,
			    p.active as active,
			    p.updatedAt as updatedAt,
			    p.updatedBy as updatedBy
			from TourProvider p
			where (:activeOnly = false or p.active = true)
			order by lower(p.name) asc, p.id asc
			""")
	List<TourProviderListItemView> findListItems(@Param("activeOnly") boolean activeOnly);

	boolean existsByNameIgnoreCaseAndContactIgnoreCase(String name, String contact);

	boolean existsByNameIgnoreCaseAndContactIgnoreCaseAndIdNot(
			String name,
			String contact,
			Long excludedId
	);
}
