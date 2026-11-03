package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryIdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryIdempotencyRepository extends JpaRepository<LaundryIdempotencyRecord, Long> {
	Optional<LaundryIdempotencyRecord> findByActorUsernameAndIdempotencyKey(
			String actorUsername,
			String idempotencyKey
	);

	void deleteByActorUsernameAndIdempotencyKey(String actorUsername, String idempotencyKey);
}
