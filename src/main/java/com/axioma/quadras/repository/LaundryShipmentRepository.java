package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryShipment;
import com.axioma.quadras.domain.model.LaundryShipmentKind;
import com.axioma.quadras.domain.model.LaundryShipmentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryShipmentRepository extends JpaRepository<LaundryShipment, Long> {
	@Query("""
		select shipment.sentAt as occurredAt, sum(line.sentQuantity) as quantity
		from LaundryShipment shipment join shipment.lines line
		where shipment.kind = :kind
		  and shipment.status <> com.axioma.quadras.domain.model.LaundryShipmentStatus.CANCELLED
		  and (:providerId is null or shipment.provider.id = :providerId)
		  and (:dateFrom is null or shipment.sentAt >= :dateFrom)
		  and (:dateToExclusive is null or shipment.sentAt < :dateToExclusive)
		group by shipment.id, shipment.sentAt
		""")
	List<LaundryMovementSummaryView> summarizeSentMovements(
			@Param("kind") LaundryShipmentKind kind,
			@Param("providerId") Long providerId,
			@Param("dateFrom") OffsetDateTime dateFrom,
			@Param("dateToExclusive") OffsetDateTime dateToExclusive
	);

	@Query("""
		select shipment.receivedAt as occurredAt, sum(line.acceptedQuantity) as quantity
		from LaundryShipment shipment join shipment.lines line
		where shipment.kind = com.axioma.quadras.domain.model.LaundryShipmentKind.RECEIPT
		  and shipment.status <> com.axioma.quadras.domain.model.LaundryShipmentStatus.CANCELLED
		  and (:providerId is null or shipment.provider.id = :providerId)
		  and (:dateFrom is null or shipment.receivedAt >= :dateFrom)
		  and (:dateToExclusive is null or shipment.receivedAt < :dateToExclusive)
		group by shipment.id, shipment.receivedAt
		""")
	List<LaundryMovementSummaryView> summarizeReceivedMovements(
			@Param("providerId") Long providerId,
			@Param("dateFrom") OffsetDateTime dateFrom,
			@Param("dateToExclusive") OffsetDateTime dateToExclusive
	);

	@EntityGraph(attributePaths = {"provider", "lines", "lines.item", "lines.originLine"})
	@Query("""
		select distinct shipment from LaundryShipment shipment
		where (:status is null or shipment.status = :status)
		  and (:kind is null or shipment.kind = :kind)
		  and (:providerId is null or shipment.provider.id = :providerId)
		  and (:dateFrom is null or shipment.sentAt >= :dateFrom)
		  and (:dateToExclusive is null or shipment.sentAt < :dateToExclusive)
		order by case when shipment.sentAt is null then 1 else 0 end,
		         shipment.sentAt desc, shipment.createdAt desc, shipment.id desc
		""")
	List<LaundryShipment> search(
			@Param("status") LaundryShipmentStatus status,
			@Param("kind") LaundryShipmentKind kind,
			@Param("providerId") Long providerId,
			@Param("dateFrom") OffsetDateTime dateFrom,
			@Param("dateToExclusive") OffsetDateTime dateToExclusive
	);

	@EntityGraph(attributePaths = {"provider", "lines", "lines.item", "lines.originLine"})
	@Query("select shipment from LaundryShipment shipment where shipment.id = :id")
	java.util.Optional<LaundryShipment> findDetailedById(@Param("id") Long id);
}
