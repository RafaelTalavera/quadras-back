package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.ScheduleLock;
import com.axioma.quadras.domain.model.ScheduleLockType;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleLockRepository extends JpaRepository<ScheduleLock, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select l from ScheduleLock l
			where l.lockType = :lockType
			  and l.scopeKey = :scopeKey
			  and l.bookingDate = :bookingDate
			""")
	Optional<ScheduleLock> findByKeyForUpdate(
			@Param("lockType") ScheduleLockType lockType,
			@Param("scopeKey") String scopeKey,
			@Param("bookingDate") LocalDate bookingDate
	);
}
