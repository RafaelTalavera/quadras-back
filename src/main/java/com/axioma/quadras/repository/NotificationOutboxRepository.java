package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.NotificationOutbox;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
	boolean existsByIdempotencyKey(String idempotencyKey);

	@Query("""
			select outbox.id
			from NotificationOutbox outbox
			where outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PENDING
			  and outbox.nextAttemptAt <= :now
			order by outbox.nextAttemptAt, outbox.id
			""")
	List<Long> findDispatchableIds(@Param("now") OffsetDateTime now, Pageable pageable);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox outbox
			set outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING,
				outbox.claimedAt = :claimedAt,
				outbox.attemptCount = outbox.attemptCount + 1
			where outbox.id = :id
			  and outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PENDING
			  and outbox.nextAttemptAt <= :claimedAt
			  and outbox.notification.status in (
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.UNREAD,
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.READ
			  )
			""")
	int claim(@Param("id") Long id, @Param("claimedAt") OffsetDateTime claimedAt);

	@Query("""
			select new com.axioma.quadras.repository.NotificationOutboxDispatchItem(
				outbox.id,
				outbox.logicalDestination,
				outbox.idempotencyKey,
				outbox.attemptCount,
				outbox.notification.order.id,
				outbox.notification.title,
				outbox.notification.body,
				outbox.notification.dueAt
			)
			from NotificationOutbox outbox
			where outbox.id = :id
			  and outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING
			""")
	Optional<NotificationOutboxDispatchItem> findClaimedItem(@Param("id") Long id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox outbox
			set outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.DELIVERED,
				outbox.deliveredAt = :deliveredAt,
				outbox.claimedAt = null,
				outbox.nextAttemptAt = null,
				outbox.lastError = null
			where outbox.id = :id
			  and outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING
			""")
	int markDelivered(@Param("id") Long id, @Param("deliveredAt") OffsetDateTime deliveredAt);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox outbox
			set outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PENDING,
				outbox.claimedAt = null,
				outbox.nextAttemptAt = :nextAttemptAt,
				outbox.lastError = :lastError
			where outbox.id = :id
			  and outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING
			""")
	int scheduleRetry(
			@Param("id") Long id,
			@Param("nextAttemptAt") OffsetDateTime nextAttemptAt,
			@Param("lastError") String lastError
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox outbox
			set outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.ABANDONED,
				outbox.abandonedAt = :abandonedAt,
				outbox.claimedAt = null,
				outbox.nextAttemptAt = null,
				outbox.lastError = :lastError
			where outbox.id = :id
			  and outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING
			""")
	int abandonClaim(
			@Param("id") Long id,
			@Param("abandonedAt") OffsetDateTime abandonedAt,
			@Param("lastError") String lastError
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox outbox
			set outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PENDING,
				outbox.claimedAt = null,
				outbox.nextAttemptAt = :now,
				outbox.lastError = 'Recovered expired delivery claim'
			where outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING
			  and outbox.claimedAt <= :expiredBefore
			""")
	int recoverExpiredClaims(
			@Param("expiredBefore") OffsetDateTime expiredBefore,
			@Param("now") OffsetDateTime now
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox outbox
			set outbox.deliveryStatus = com.axioma.quadras.domain.model.NotificationDeliveryStatus.ABANDONED,
				outbox.abandonedAt = :abandonedAt,
				outbox.claimedAt = null,
				outbox.nextAttemptAt = null,
				outbox.lastError = :reason
			where outbox.notification.order.id = :orderId
			  and outbox.deliveryStatus in (
				com.axioma.quadras.domain.model.NotificationDeliveryStatus.PENDING,
				com.axioma.quadras.domain.model.NotificationDeliveryStatus.PROCESSING
			  )
			  and outbox.notification.status in (
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.UNREAD,
				com.axioma.quadras.domain.model.MaintenanceNotificationStatus.READ
			  )
			""")
	int abandonPendingForActiveNotificationsByOrderId(
			@Param("orderId") Long orderId,
			@Param("abandonedAt") OffsetDateTime abandonedAt,
			@Param("reason") String reason
	);
}
