package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageTherapist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MassageTherapistRepository extends JpaRepository<MassageTherapist, Long> {

	boolean existsByProviderIdAndNameIgnoreCase(Long providerId, String name);

	boolean existsByProviderIdAndNameIgnoreCaseAndIdNot(Long providerId, String name, Long id);

	List<MassageTherapist> findAllByProviderIdOrderByNameAsc(Long providerId);
}
