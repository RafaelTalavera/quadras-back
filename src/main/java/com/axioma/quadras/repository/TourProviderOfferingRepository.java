package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourProviderOffering;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourProviderOfferingRepository extends JpaRepository<TourProviderOffering, Long> {

	List<TourProviderOffering> findAllByProviderIdOrderByNameAsc(Long providerId);

	List<TourProviderOffering> findAllByProviderIdInOrderByProviderIdAscNameAsc(Collection<Long> providerIds);

	void deleteAllByProviderId(Long providerId);
}
