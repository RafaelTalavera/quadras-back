package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryDocument;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryDocumentRepository extends JpaRepository<LaundryDocument, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	@Query("select document from LaundryDocument document where document.id = :id")
	Optional<LaundryDocument> findForUpdate(@Param("id") Long id);

	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	Optional<LaundryDocument> findById(Long id);
}
