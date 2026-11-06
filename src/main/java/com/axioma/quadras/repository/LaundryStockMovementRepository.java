package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockMovement;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryStockMovementRepository extends JpaRepository<LaundryStockMovement, Long> {

	@Query("""
			select coalesce(sum(movement.quantity), 0)
			from LaundryStockMovement movement
			where movement.toBucket = :bucket
			  and movement.item.id = :itemId
			  and (:providerScope is null or movement.providerScope = :providerScope)
			""")
	BigDecimal sumIncoming(
			@Param("itemId") Long itemId,
			@Param("bucket") LaundryStockBucket bucket,
			@Param("providerScope") Long providerScope
	);

	@Query("""
			select coalesce(sum(movement.quantity), 0)
			from LaundryStockMovement movement
			where movement.fromBucket = :bucket
			  and movement.item.id = :itemId
			  and (:providerScope is null or movement.providerScope = :providerScope)
			""")
	BigDecimal sumOutgoing(
			@Param("itemId") Long itemId,
			@Param("bucket") LaundryStockBucket bucket,
			@Param("providerScope") Long providerScope
	);

	@Query("select coalesce(sum(movement.quantity), 0) from LaundryStockMovement movement where movement.toBucket is not null")
	BigDecimal sumAllIncoming();

	@Query("select coalesce(sum(movement.quantity), 0) from LaundryStockMovement movement where movement.fromBucket is not null")
	BigDecimal sumAllOutgoing();

	List<LaundryStockMovement> findByCorrelationIdOrderByIdAsc(String correlationId);

	@Query("""
			select movement
			from LaundryStockMovement movement
			join fetch movement.item
			where movement.effectiveAt >= :from
			  and movement.effectiveAt < :to
			order by movement.effectiveAt asc, movement.id asc
			""")
	List<LaundryStockMovement> findTraceabilityMovements(
			@Param("from") OffsetDateTime from,
			@Param("to") OffsetDateTime to
	);

	@Query("""
			select movement
			from LaundryStockMovement movement
			join fetch movement.item
			where movement.effectiveAt >= :from
			  and movement.effectiveAt < :to
			  and (:providerId is null or movement.provider.id = :providerId)
			  and movement.documentType in (
			    com.axioma.quadras.domain.model.LaundryStockDocumentType.DISPATCH,
			    com.axioma.quadras.domain.model.LaundryStockDocumentType.REWASH_DISPATCH,
			    com.axioma.quadras.domain.model.LaundryStockDocumentType.RETURN)
			order by movement.effectiveAt desc, movement.id desc
			""")
	List<LaundryStockMovement> findOperationalMovements(
			@Param("providerId") Long providerId,
			@Param("from") OffsetDateTime from,
			@Param("to") OffsetDateTime to
	);
}
