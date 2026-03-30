package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceOrderAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceOrderAttachmentRepository extends JpaRepository<MaintenanceOrderAttachment, Long> {

	List<MaintenanceOrderAttachment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
