package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageTherapist;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MassageTherapistRepository extends JpaRepository<MassageTherapist, Long> {

	boolean existsByProviderIdAndNameIgnoreCase(Long providerId, String name);

	boolean existsByProviderIdAndNameIgnoreCaseAndIdNot(Long providerId, String name, Long id);

	List<MassageTherapist> findAllByProviderIdOrderByNameAsc(Long providerId);

	@Query("""
			select
			    t.id as id,
			    t.provider.id as providerId,
			    t.name as name,
			    t.active as active,
			    t.createdAt as createdAt,
			    t.updatedAt as updatedAt
			from MassageTherapist t
			where t.provider.id in :providerIds
			order by t.provider.id asc, lower(t.name) asc, t.id asc
			""")
	List<MassageTherapistListItemView> findListItemsByProviderIdInOrderByProviderIdAscNameAsc(
			@Param("providerIds") Collection<Long> providerIds
	);

	@Query("""
			select
			    t.provider.id as providerId,
			    count(t) as therapistsCount
			from MassageTherapist t
			where t.provider.id in :providerIds
			group by t.provider.id
			""")
	List<MassageTherapistCountView> countByProviderIdIn(
			@Param("providerIds") Collection<Long> providerIds
	);
}
