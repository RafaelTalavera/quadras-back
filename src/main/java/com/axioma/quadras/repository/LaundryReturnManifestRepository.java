package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryReturnManifest;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface LaundryReturnManifestRepository extends JpaRepository<LaundryReturnManifest, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	@Query("select manifest from LaundryReturnManifest manifest where manifest.id = :id")
	Optional<LaundryReturnManifest> findForUpdate(@Param("id") Long id);

	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	Optional<LaundryReturnManifest> findById(Long id);

	@Query(value = """
			select manifest from LaundryReturnManifest manifest
			join fetch manifest.provider provider
			where provider.id = :providerId
			  and (:status is null or manifest.status = :status)
			order by manifest.effectiveAt desc, manifest.id desc
			""", countQuery = """
			select count(manifest) from LaundryReturnManifest manifest
			where manifest.provider.id = :providerId
			  and (:status is null or manifest.status = :status)
			""")
	Page<LaundryReturnManifest> findProviderPage(@Param("providerId") Long providerId,
			@Param("status") com.axioma.quadras.domain.model.LaundryReturnStatus status,
			Pageable pageable);
}
