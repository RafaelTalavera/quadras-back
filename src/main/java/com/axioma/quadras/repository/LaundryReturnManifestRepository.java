package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryReturnManifest;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryReturnManifestRepository extends JpaRepository<LaundryReturnManifest, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	@Query("select manifest from LaundryReturnManifest manifest where manifest.id = :id")
	Optional<LaundryReturnManifest> findForUpdate(@Param("id") Long id);

	@EntityGraph(attributePaths = { "provider", "lines", "lines.item" })
	Optional<LaundryReturnManifest> findById(Long id);
}
