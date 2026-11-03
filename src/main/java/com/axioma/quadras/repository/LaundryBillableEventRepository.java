package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryBillableEvent;
import com.axioma.quadras.domain.model.LaundryBillableExportStatus;
import java.util.List;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryBillableEventRepository extends JpaRepository<LaundryBillableEvent, Long> {
	@EntityGraph(attributePaths = { "provider", "item", "manifest", "obligation" })
	List<LaundryBillableEvent> findByExportStatusOrderByAcceptedAtAscIdAsc(LaundryBillableExportStatus status);

	@EntityGraph(attributePaths = { "provider", "item", "manifest", "obligation" })
	@Query("""
			select event from LaundryBillableEvent event
			where (:providerId is null or event.provider.id = :providerId)
			  and (:from is null or event.acceptedAt >= :from)
			  and (:to is null or event.acceptedAt <= :to)
			  and (:status is null or event.exportStatus = :status)
			order by event.acceptedAt asc, event.id asc
			""")
	Page<LaundryBillableEvent> search(@Param("providerId") Long providerId,
			@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
			@Param("status") LaundryBillableExportStatus status, Pageable pageable);
}
