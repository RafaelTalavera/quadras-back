package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.PushSubscription;
import com.axioma.quadras.domain.model.PushProviderType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

	Optional<PushSubscription> findByUsernameAndInstallationId(String username, String installationId);

	List<PushSubscription> findAllByUsernameAndProviderAndActiveTrueOrderById(
			String username,
			PushProviderType provider
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update PushSubscription subscription
			set subscription.active = false,
				subscription.encryptedToken = null,
				subscription.tokenHash = null,
				subscription.lastSeenAt = :now,
				subscription.updatedAt = :now
			where subscription.active = true
			  and (subscription.installationId = :installationId or subscription.tokenHash = :tokenHash)
			  and not (subscription.username = :username and subscription.installationId = :installationId)
			""")
	int deactivateConflicts(
			@Param("username") String username,
			@Param("installationId") String installationId,
			@Param("tokenHash") String tokenHash,
			@Param("now") OffsetDateTime now
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update PushSubscription subscription
			set subscription.active = false,
				subscription.encryptedToken = null,
				subscription.tokenHash = null,
				subscription.lastSeenAt = :now,
				subscription.updatedAt = :now
			where subscription.id = :id
			  and subscription.active = true
			""")
	int deactivateInvalidToken(@Param("id") Long id, @Param("now") OffsetDateTime now);
}
