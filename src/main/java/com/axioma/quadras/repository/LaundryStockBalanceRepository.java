package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryStockBalance;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryStockBalanceRepository extends JpaRepository<LaundryStockBalance, Long> {

	@EntityGraph(attributePaths = { "item", "provider" })
	@Query("""
			select balance from LaundryStockBalance balance
			where (:itemId is null or balance.item.id = :itemId)
			  and (:bucket is null or balance.bucket = :bucket)
			  and (:providerId is null or balance.provider.id = :providerId)
			order by balance.item.name asc, balance.bucket asc, balance.id asc
			""")
	Page<LaundryStockBalance> search(
			@Param("itemId") Long itemId,
			@Param("bucket") LaundryStockBucket bucket,
			@Param("providerId") Long providerId,
			Pageable pageable
	);

	@EntityGraph(attributePaths = { "item", "provider" })
	@Query("""
			select balance from LaundryStockBalance balance
			join fetch balance.item
			left join fetch balance.provider
			order by balance.item.name asc, balance.bucket asc, balance.id asc
			""")
	List<LaundryStockBalance> findAllWithItemAndProvider();

	Optional<LaundryStockBalance> findByItemIdAndBucketAndProviderScope(
			Long itemId,
			LaundryStockBucket bucket,
			long providerScope
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select balance from LaundryStockBalance balance
			where balance.item.id = :itemId
			  and balance.bucket = :bucket
			  and balance.providerScope = :providerScope
			""")
	Optional<LaundryStockBalance> findForUpdate(
			@Param("itemId") Long itemId,
			@Param("bucket") LaundryStockBucket bucket,
			@Param("providerScope") long providerScope
	);
}
