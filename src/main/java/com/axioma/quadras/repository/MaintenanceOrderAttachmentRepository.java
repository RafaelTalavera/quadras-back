package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceOrderAttachment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceOrderAttachmentRepository extends JpaRepository<MaintenanceOrderAttachment, Long> {

	List<MaintenanceOrderAttachment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

	@Query("""
			select
			    a.order.id as orderId,
			    a.id as id,
			    a.attachmentType as attachmentType,
			    a.fileName as fileName,
			    a.contentType as contentType,
			    a.fileSize as fileSize,
			    a.createdAt as createdAt,
			    a.createdBy as createdBy
			from MaintenanceOrderAttachment a
			where a.order.id = :orderId
			order by a.createdAt desc, a.id desc
			""")
	List<MaintenanceOrderAttachmentMetadataView> findMetadataByOrderIdOrderByCreatedAtDesc(
			@Param("orderId") Long orderId
	);

	@Query("""
			select
			    a.order.id as orderId,
			    a.id as id,
			    a.attachmentType as attachmentType,
			    a.fileName as fileName,
			    a.contentType as contentType,
			    a.fileSize as fileSize,
			    a.createdAt as createdAt,
			    a.createdBy as createdBy
			from MaintenanceOrderAttachment a
			where a.order.id in :orderIds
			order by a.order.id asc, a.createdAt desc, a.id desc
			""")
	List<MaintenanceOrderAttachmentMetadataView> findMetadataByOrderIdInOrderByCreatedAtDesc(
			@Param("orderIds") Collection<Long> orderIds
	);
}
