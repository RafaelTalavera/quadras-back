package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceNotification;
import com.axioma.quadras.domain.model.MaintenanceNotificationStatus;
import com.axioma.quadras.domain.model.MaintenanceNotificationType;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceNotificationRepository extends JpaRepository<MaintenanceNotification, Long> {
	boolean existsByOrderIdAndNotificationTypeAndScheduleVersionAndRecipientUsernameAndOccurrenceKey(
			Long orderId,
			MaintenanceNotificationType notificationType,
			long scheduleVersion,
			String recipientUsername,
			String occurrenceKey
	);

	long countByRecipientUsernameAndStatus(String recipientUsername, MaintenanceNotificationStatus status);

	@Query("""
			select
				notification.id as id,
				notification.order.id as orderId,
				notification.notificationType as notificationType,
				notification.status as status,
				notification.title as title,
				notification.body as body,
				notification.dueAt as dueAt,
				notification.readAt as readAt,
				notification.resolvedAt as resolvedAt,
				notification.dismissedAt as dismissedAt,
				notification.createdAt as createdAt
			from MaintenanceNotification notification
			where notification.recipientUsername = :recipientUsername
			  and (:status is null or notification.status = :status)
			order by notification.createdAt desc, notification.id desc
			""")
	Slice<MaintenanceNotificationItemView> findInbox(
			@Param("recipientUsername") String recipientUsername,
			@Param("status") MaintenanceNotificationStatus status,
			Pageable pageable
	);

	boolean existsByIdAndRecipientUsername(Long id, String recipientUsername);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update MaintenanceNotification notification
			set notification.status = com.axioma.quadras.domain.model.MaintenanceNotificationStatus.READ,
				notification.readAt = :readAt,
				notification.updatedAt = :readAt
			where notification.id = :notificationId
			  and notification.recipientUsername = :recipientUsername
			  and notification.status = com.axioma.quadras.domain.model.MaintenanceNotificationStatus.UNREAD
			""")
	int markRead(
			@Param("notificationId") Long notificationId,
			@Param("recipientUsername") String recipientUsername,
			@Param("readAt") OffsetDateTime readAt
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update MaintenanceNotification notification
			set notification.status = com.axioma.quadras.domain.model.MaintenanceNotificationStatus.DISMISSED,
				notification.dismissedAt = :dismissedAt,
				notification.updatedAt = :dismissedAt
			where notification.id = :notificationId
			  and notification.recipientUsername = :recipientUsername
			  and notification.status in (
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.UNREAD,
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.READ
			  )
			""")
	int dismiss(
			@Param("notificationId") Long notificationId,
			@Param("recipientUsername") String recipientUsername,
			@Param("dismissedAt") OffsetDateTime dismissedAt
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update MaintenanceNotification notification
			set notification.status = com.axioma.quadras.domain.model.MaintenanceNotificationStatus.RESOLVED,
				notification.resolvedAt = :resolvedAt,
				notification.updatedAt = :resolvedAt
			where notification.order.id = :orderId
			  and notification.status in (
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.UNREAD,
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.READ
			  )
			""")
	int resolveActiveByOrderId(
			@Param("orderId") Long orderId,
			@Param("resolvedAt") OffsetDateTime resolvedAt
	);
}
