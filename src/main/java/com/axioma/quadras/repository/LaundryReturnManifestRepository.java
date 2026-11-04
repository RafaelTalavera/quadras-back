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
import com.axioma.quadras.repository.projection.LaundryReturnClassificationView;

public interface LaundryReturnManifestRepository extends JpaRepository<LaundryReturnManifest, Long> {
	@Query("""
			select count(manifest) from LaundryReturnManifest manifest
			where manifest.status = com.axioma.quadras.domain.model.LaundryReturnStatus.SUBMITTED
			  and (:providerId is null or manifest.provider.id = :providerId)
			""")
	long countAwaitingHotel(@Param("providerId") Long providerId);

	@Query("""
			select coalesce(sum(line.returnedDirtyQuantity), 0)
			         + coalesce(sum(line.retainedQuantity), 0) as rewashQuantity,
			       coalesce(sum(line.acceptedCleanQuantity), 0)
			         + coalesce(sum(line.returnedDirtyQuantity), 0)
			         + coalesce(sum(line.retainedQuantity), 0)
			         + coalesce(sum(line.missingQuantity), 0)
			         + coalesce(sum(line.damagedQuantity), 0) as classifiedQuantity
			from LaundryReturnManifestLine line
			where line.manifest.status in (
			  com.axioma.quadras.domain.model.LaundryReturnStatus.CONFIRMED,
			  com.axioma.quadras.domain.model.LaundryReturnStatus.CONFIRMED_BY_HOTEL)
			  and (:providerId is null or line.manifest.provider.id = :providerId)
			""")
	LaundryReturnClassificationView classification(@Param("providerId") Long providerId);
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

	@Query(value = """
			select manifest from LaundryReturnManifest manifest
			join fetch manifest.provider provider
			where (:providerId is null or provider.id = :providerId)
			  and (:status is null or manifest.status = :status)
			order by manifest.effectiveAt desc, manifest.id desc
			""", countQuery = """
			select count(manifest) from LaundryReturnManifest manifest
			where (:providerId is null or manifest.provider.id = :providerId)
			  and (:status is null or manifest.status = :status)
			""")
	Page<LaundryReturnManifest> search(@Param("providerId") Long providerId,
			@Param("status") com.axioma.quadras.domain.model.LaundryReturnStatus status,
			Pageable pageable);
}
