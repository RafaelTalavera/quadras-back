package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryDocument;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LaundryDocumentRepository extends JpaRepository<LaundryDocument, Long> {
	@Query("""
			select count(document) from LaundryDocument document
			where document.status = com.axioma.quadras.domain.model.LaundryDocumentStatus.OBSERVED
			  and (:providerId is null or document.provider.id = :providerId)
			""")
	long countObserved(@Param("providerId") Long providerId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	@Query("select document from LaundryDocument document where document.id = :id")
	Optional<LaundryDocument> findForUpdate(@Param("id") Long id);

	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	Optional<LaundryDocument> findById(Long id);

	@Query(value = """
			select document from LaundryDocument document
			join fetch document.provider provider
			where provider.id = :providerId
			  and (:status is null or document.status = :status)
			  and (:type is null or document.documentType = :type)
			order by document.effectiveAt desc, document.id desc
			""", countQuery = """
			select count(document) from LaundryDocument document
			where document.provider.id = :providerId
			  and (:status is null or document.status = :status)
			  and (:type is null or document.documentType = :type)
			""")
	Page<LaundryDocument> findProviderPage(@Param("providerId") Long providerId,
			@Param("status") com.axioma.quadras.domain.model.LaundryDocumentStatus status,
			@Param("type") com.axioma.quadras.domain.model.LaundryDocumentType type,
			Pageable pageable);
}
