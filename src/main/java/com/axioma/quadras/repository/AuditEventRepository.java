package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.AuditEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
	List<AuditEvent> findAllByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(String entityType, Long entityId);
}
