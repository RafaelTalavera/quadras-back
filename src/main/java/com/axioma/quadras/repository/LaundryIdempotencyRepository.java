package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryIdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryIdempotencyRepository extends JpaRepository<LaundryIdempotencyRecord, Long> {
	Optional<LaundryIdempotencyRecord> findByActorUsernameAndIdempotencyKey(
			String actorUsername,
			String idempotencyKey
	);

	@Modifying
	@Query("""
			update LaundryIdempotencyRecord record
			set record.completed = true,
				record.responseStatus = :responseStatus,
				record.responseBody = :responseBody
			where record.actorUsername = :actorUsername
				and record.idempotencyKey = :idempotencyKey
				and record.completed = false
			""")
	int complete(
			@Param("actorUsername") String actorUsername,
			@Param("idempotencyKey") String idempotencyKey,
			@Param("responseStatus") int responseStatus,
			@Param("responseBody") String responseBody
	);

	void deleteByActorUsernameAndIdempotencyKey(String actorUsername, String idempotencyKey);
}
