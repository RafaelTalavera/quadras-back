package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockLot;
import com.axioma.quadras.repository.projection.LaundryStockAgingView;
import com.axioma.quadras.repository.projection.LaundryStockLotTotalView;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryStockLotRepository extends JpaRepository<LaundryStockLot, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select lot from LaundryStockLot lot
			where lot.item.id = :itemId
			  and lot.bucket = :bucket
			  and lot.providerScope = :providerScope
			  and lot.remainingQuantity > 0
			order by lot.effectiveAt asc, lot.id asc
			""")
	List<LaundryStockLot> findOpenForUpdate(
			@Param("itemId") Long itemId,
			@Param("bucket") LaundryStockBucket bucket,
			@Param("providerScope") long providerScope
	);

	@Query("""
			select lot.item.id as itemId,
			       lot.bucket as bucket,
			       lot.providerScope as providerScope,
			       min(lot.effectiveAt) as oldestEffectiveAt
			from LaundryStockLot lot
			where lot.remainingQuantity > 0
			group by lot.item.id, lot.bucket, lot.providerScope
			""")
	List<LaundryStockAgingView> findAging();

	@Query("""
			select lot.item.id as itemId,
			       lot.bucket as bucket,
			       lot.providerScope as providerScope,
			       sum(lot.remainingQuantity) as quantity
			from LaundryStockLot lot
			group by lot.item.id, lot.bucket, lot.providerScope
			""")
	List<LaundryStockLotTotalView> findTotals();

	@Query("""
			select coalesce(sum(lot.remainingQuantity), 0)
			from LaundryStockLot lot
			where lot.item.id = :itemId
			  and lot.bucket = :bucket
			  and lot.providerScope = :providerScope
			""")
	BigDecimal sumRemaining(
			@Param("itemId") Long itemId,
			@Param("bucket") LaundryStockBucket bucket,
			@Param("providerScope") long providerScope
	);
}
