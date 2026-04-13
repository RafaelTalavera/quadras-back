package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MassageProviderRepository extends JpaRepository<MassageProvider, Long> {

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	@Query("""
			select
			    p.id as id,
			    p.name as name,
			    p.specialty as specialty,
			    p.contact as contact,
			    p.active as active,
			    p.createdAt as createdAt,
			    p.updatedAt as updatedAt
			from MassageProvider p
			where (:activeOnly = false or p.active = true)
			order by lower(p.name) asc, p.id asc
			""")
	List<MassageProviderListItemView> findListItems(@Param("activeOnly") boolean activeOnly);
}
