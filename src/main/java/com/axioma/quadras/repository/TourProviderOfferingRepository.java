package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourProviderOffering;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourProviderOfferingRepository extends JpaRepository<TourProviderOffering, Long> {

	List<TourProviderOffering> findAllByProviderIdOrderByNameAsc(Long providerId);

	@Query("""
			select
			    o.id as id,
			    o.provider.id as providerId,
			    o.serviceType as serviceType,
			    o.name as name,
			    o.amount as amount,
			    o.description as description,
			    o.active as active,
			    o.updatedAt as updatedAt,
			    o.updatedBy as updatedBy
			from TourProviderOffering o
			where o.provider.id in :providerIds
			order by o.provider.id asc, lower(o.name) asc, o.id asc
			""")
	List<TourProviderOfferingListItemView> findListItemsByProviderIdInOrderByProviderIdAscNameAsc(
			@Param("providerIds") Collection<Long> providerIds
	);

	@Modifying
	@Query("delete from TourProviderOffering o where o.provider.id = :providerId")
	void deleteInBulkByProviderId(@Param("providerId") Long providerId);
}
