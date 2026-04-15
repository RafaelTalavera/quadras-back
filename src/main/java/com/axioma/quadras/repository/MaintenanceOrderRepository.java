package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {

	@Query("""
			select o from MaintenanceOrder o
			where o.location.id = :locationId
			  and o.scheduledStartAt is not null
			  and o.scheduledEndAt is not null
			  and o.status in :statuses
			  and (:excludeOrderId is null or o.id <> :excludeOrderId)
			  and o.scheduledStartAt < :scheduledEndAt
			  and o.scheduledEndAt > :scheduledStartAt
			order by o.scheduledStartAt asc
			""")
	List<MaintenanceOrder> findScheduleConflicts(
			@Param("locationId") Long locationId,
			@Param("scheduledStartAt") LocalDateTime scheduledStartAt,
			@Param("scheduledEndAt") LocalDateTime scheduledEndAt,
			@Param("excludeOrderId") Long excludeOrderId,
			@Param("statuses") Collection<MaintenanceOrderStatus> statuses
	);

	@Query("""
			select
				o.id as id,
				o.location.id as locationId,
				o.locationTypeSnapshot as locationTypeSnapshot,
				o.locationCodeSnapshot as locationCodeSnapshot,
				o.locationLabelSnapshot as locationLabelSnapshot,
				p.id as providerId,
				o.providerTypeSnapshot as providerTypeSnapshot,
				o.providerNameSnapshot as providerNameSnapshot,
				o.serviceLabelSnapshot as serviceLabelSnapshot,
				o.title as title,
				o.description as description,
				o.priority as priority,
				o.requestOrigin as requestOrigin,
				o.requestedForGuest as requestedForGuest,
				o.guestName as guestName,
				o.guestReference as guestReference,
				o.requestedByUsername as requestedByUsername,
				o.requestedByRole as requestedByRole,
				o.businessPriority as businessPriority,
				o.estimatedExecutionMinutes as estimatedExecutionMinutes,
				o.assignedUsername as assignedUsername,
				o.assignedAt as assignedAt,
				o.status as status,
				o.reportedAt as reportedAt,
				o.scheduledStartAt as scheduledStartAt,
				o.scheduledEndAt as scheduledEndAt,
				o.startedAt as startedAt,
				o.completedAt as completedAt,
				o.paid as paid,
				o.paymentMethod as paymentMethod,
				o.paymentDate as paymentDate,
				o.paymentNotes as paymentNotes,
				o.resolutionNotes as resolutionNotes,
				o.cancellationNotes as cancellationNotes,
				o.createdAt as createdAt,
				o.updatedAt as updatedAt,
				o.cancelledAt as cancelledAt,
				o.createdBy as createdBy,
				o.updatedBy as updatedBy,
				o.cancelledBy as cancelledBy
			from MaintenanceOrder o
			left join o.provider p
			where (:locationId is null or o.location.id = :locationId)
			  and (:providerId is null or p.id = :providerId)
			  and (:providerType is null or o.providerTypeSnapshot = :providerType)
			  and (:status is null or o.status = :status)
			  and (:priority is null or o.priority = :priority)
			  and (
			        (:scheduledFrom is null and :scheduledToExclusive is null
			             and :reportedFrom is null and :reportedToExclusive is null)
			        or (
			            o.scheduledStartAt is not null
			            and (:scheduledFrom is null or o.scheduledStartAt >= :scheduledFrom)
			            and (:scheduledToExclusive is null or o.scheduledStartAt < :scheduledToExclusive)
			        )
			        or (
			            o.scheduledStartAt is null
			            and (:reportedFrom is null or o.reportedAt >= :reportedFrom)
			            and (:reportedToExclusive is null or o.reportedAt < :reportedToExclusive)
			        )
			  )
			""")
	List<MaintenanceOrderHistoryItemView> findFilteredItems(
			@Param("locationId") Long locationId,
			@Param("providerId") Long providerId,
			@Param("providerType") MaintenanceProviderType providerType,
			@Param("status") MaintenanceOrderStatus status,
			@Param("priority") MaintenancePriority priority,
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query("""
			select
				o.id as id,
				o.location.id as locationId,
				o.locationTypeSnapshot as locationTypeSnapshot,
				o.locationCodeSnapshot as locationCodeSnapshot,
				o.locationLabelSnapshot as locationLabelSnapshot,
				p.id as providerId,
				o.providerTypeSnapshot as providerTypeSnapshot,
				o.providerNameSnapshot as providerNameSnapshot,
				o.serviceLabelSnapshot as serviceLabelSnapshot,
				o.title as title,
				o.priority as priority,
				o.estimatedExecutionMinutes as estimatedExecutionMinutes,
				o.status as status,
				o.reportedAt as reportedAt,
				o.scheduledStartAt as scheduledStartAt,
				o.scheduledEndAt as scheduledEndAt,
				o.startedAt as startedAt,
				o.completedAt as completedAt,
				o.paid as paid
			from MaintenanceOrder o
			left join o.provider p
			where (:locationId is null or o.location.id = :locationId)
			  and (:providerId is null or p.id = :providerId)
			  and (:providerType is null or o.providerTypeSnapshot = :providerType)
			  and (:status is null or o.status = :status)
			  and (:priority is null or o.priority = :priority)
			  and (
			        (:scheduledFrom is null and :scheduledToExclusive is null
			             and :reportedFrom is null and :reportedToExclusive is null)
			        or (
			            o.scheduledStartAt is not null
			            and (:scheduledFrom is null or o.scheduledStartAt >= :scheduledFrom)
			            and (:scheduledToExclusive is null or o.scheduledStartAt < :scheduledToExclusive)
			        )
			        or (
			            o.scheduledStartAt is null
			            and (:reportedFrom is null or o.reportedAt >= :reportedFrom)
			            and (:reportedToExclusive is null or o.reportedAt < :reportedToExclusive)
			        )
			  )
			order by
				case when o.scheduledStartAt is null then 1 else 0 end asc,
				o.scheduledStartAt asc,
				o.reportedAt asc,
				o.id asc
			""")
	Slice<MaintenanceOrderListItemView> findCompactItems(
			@Param("locationId") Long locationId,
			@Param("providerId") Long providerId,
			@Param("providerType") MaintenanceProviderType providerType,
			@Param("status") MaintenanceOrderStatus status,
			@Param("priority") MaintenancePriority priority,
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive,
			Pageable pageable
	);

	@Query("""
			select
				o.provider.id as providerId,
				o.providerNameSnapshot as providerNameSnapshot,
				o.providerTypeSnapshot as providerTypeSnapshot,
				o.locationTypeSnapshot as locationTypeSnapshot,
				o.status as status,
				o.priority as priority,
				o.businessPriority as businessPriority,
				o.reportedAt as reportedAt,
				o.scheduledStartAt as scheduledStartAt,
				o.startedAt as startedAt,
				o.completedAt as completedAt
			from MaintenanceOrder o
			where (
			        (:scheduledFrom is null and :scheduledToExclusive is null
			             and :reportedFrom is null and :reportedToExclusive is null)
			        or (
			            o.scheduledStartAt is not null
			            and (:scheduledFrom is null or o.scheduledStartAt >= :scheduledFrom)
			            and (:scheduledToExclusive is null or o.scheduledStartAt < :scheduledToExclusive)
			        )
			        or (
			            o.scheduledStartAt is null
			            and (:reportedFrom is null or o.reportedAt >= :reportedFrom)
			            and (:reportedToExclusive is null or o.reportedAt < :reportedToExclusive)
			        )
			  )
			""")
	List<MaintenanceSummarySnapshotView> findSummarySnapshots(
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query(
			value = """
					select
					    coalesce(sum(case when mo.status = 'OPEN' then 1 else 0 end), 0) as openCount,
					    coalesce(sum(case when mo.status = 'ASSIGNED' then 1 else 0 end), 0) as assignedCount,
					    coalesce(sum(case when mo.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when mo.status = 'IN_PROGRESS' then 1 else 0 end), 0) as inProgressCount,
					    coalesce(sum(case when mo.status = 'COMPLETED' then 1 else 0 end), 0) as completedCount,
					    coalesce(sum(case when mo.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when mo.provider_type_snapshot = 'INTERNAL' then 1 else 0 end), 0) as internalCount,
					    coalesce(sum(case when mo.provider_type_snapshot = 'EXTERNAL' then 1 else 0 end), 0) as externalCount,
					    coalesce(sum(case when mo.provider_type_snapshot is null then 1 else 0 end), 0) as unassignedCount,
					    coalesce(sum(case when mo.location_type_snapshot = 'ROOM' then 1 else 0 end), 0) as roomsCount,
					    coalesce(sum(case when mo.location_type_snapshot = 'COMMON_AREA' then 1 else 0 end), 0) as commonAreasCount,
					    coalesce(sum(case when mo.priority = 'URGENT' then 1 else 0 end), 0) as urgentCount,
					    coalesce(sum(case when mo.business_priority = 'GUEST_PRIORITY' then 1 else 0 end), 0) as guestPriorityCount,
					    avg(case
					            when mo.status = 'COMPLETED'
					             and mo.completed_at is not null
					             and timestampdiff(MINUTE, coalesce(mo.started_at, mo.reported_at), mo.completed_at) > 0
					            then timestampdiff(MINUTE, coalesce(mo.started_at, mo.reported_at), mo.completed_at)
					        end) as averageResolutionMinutes
					from maintenance_orders mo
					where (
					        (:scheduledFrom is null and :scheduledToExclusive is null
					             and :reportedFrom is null and :reportedToExclusive is null)
					        or (
					            mo.scheduled_start_at is not null
					            and (:scheduledFrom is null or mo.scheduled_start_at >= :scheduledFrom)
					            and (:scheduledToExclusive is null or mo.scheduled_start_at < :scheduledToExclusive)
					        )
					        or (
					            mo.scheduled_start_at is null
					            and (:reportedFrom is null or mo.reported_at >= :reportedFrom)
					            and (:reportedToExclusive is null or mo.reported_at < :reportedToExclusive)
					        )
					  )
					""",
			nativeQuery = true
	)
	MaintenanceSummaryAggregateView findSummaryAggregate(
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query(
			value = """
					select
					    coalesce(cast(mo.provider_id as char), 'UNASSIGNED') as code,
					    coalesce(mo.provider_name_snapshot, 'Sem responsavel') as label,
					    coalesce(sum(case when mo.status in ('OPEN', 'ASSIGNED') then 1 else 0 end), 0) as openCount,
					    coalesce(sum(case when mo.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when mo.status = 'IN_PROGRESS' then 1 else 0 end), 0) as inProgressCount,
					    coalesce(sum(case when mo.status = 'COMPLETED' then 1 else 0 end), 0) as completedCount,
					    coalesce(sum(case when mo.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when mo.priority = 'URGENT' then 1 else 0 end), 0) as urgentCount
					from maintenance_orders mo
					where (
					        (:scheduledFrom is null and :scheduledToExclusive is null
					             and :reportedFrom is null and :reportedToExclusive is null)
					        or (
					            mo.scheduled_start_at is not null
					            and (:scheduledFrom is null or mo.scheduled_start_at >= :scheduledFrom)
					            and (:scheduledToExclusive is null or mo.scheduled_start_at < :scheduledToExclusive)
					        )
					        or (
					            mo.scheduled_start_at is null
					            and (:reportedFrom is null or mo.reported_at >= :reportedFrom)
					            and (:reportedToExclusive is null or mo.reported_at < :reportedToExclusive)
					        )
					  )
					group by mo.provider_id, mo.provider_name_snapshot
					order by lower(coalesce(mo.provider_name_snapshot, 'Sem responsavel')), mo.provider_id
					""",
			nativeQuery = true
	)
	List<MaintenanceSummaryBreakdownView> findProviderSummaryBreakdown(
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query(
			value = """
					select
					    coalesce(mo.provider_type_snapshot, 'UNASSIGNED') as code,
					    case
					        when mo.provider_type_snapshot = 'INTERNAL' then 'Interno'
					        when mo.provider_type_snapshot = 'EXTERNAL' then 'Externo'
					        else 'Sem responsavel'
					    end as label,
					    coalesce(sum(case when mo.status in ('OPEN', 'ASSIGNED') then 1 else 0 end), 0) as openCount,
					    coalesce(sum(case when mo.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when mo.status = 'IN_PROGRESS' then 1 else 0 end), 0) as inProgressCount,
					    coalesce(sum(case when mo.status = 'COMPLETED' then 1 else 0 end), 0) as completedCount,
					    coalesce(sum(case when mo.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when mo.priority = 'URGENT' then 1 else 0 end), 0) as urgentCount
					from maintenance_orders mo
					where (
					        (:scheduledFrom is null and :scheduledToExclusive is null
					             and :reportedFrom is null and :reportedToExclusive is null)
					        or (
					            mo.scheduled_start_at is not null
					            and (:scheduledFrom is null or mo.scheduled_start_at >= :scheduledFrom)
					            and (:scheduledToExclusive is null or mo.scheduled_start_at < :scheduledToExclusive)
					        )
					        or (
					            mo.scheduled_start_at is null
					            and (:reportedFrom is null or mo.reported_at >= :reportedFrom)
					            and (:reportedToExclusive is null or mo.reported_at < :reportedToExclusive)
					        )
					  )
					group by mo.provider_type_snapshot
					""",
			nativeQuery = true
	)
	List<MaintenanceSummaryBreakdownView> findProviderTypeSummaryBreakdown(
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query(
			value = """
					select
					    mo.location_type_snapshot as code,
					    case
					        when mo.location_type_snapshot = 'ROOM' then 'Quarto'
					        when mo.location_type_snapshot = 'COMMON_AREA' then 'Area comum'
					    end as label,
					    coalesce(sum(case when mo.status in ('OPEN', 'ASSIGNED') then 1 else 0 end), 0) as openCount,
					    coalesce(sum(case when mo.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when mo.status = 'IN_PROGRESS' then 1 else 0 end), 0) as inProgressCount,
					    coalesce(sum(case when mo.status = 'COMPLETED' then 1 else 0 end), 0) as completedCount,
					    coalesce(sum(case when mo.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when mo.priority = 'URGENT' then 1 else 0 end), 0) as urgentCount
					from maintenance_orders mo
					where (
					        (:scheduledFrom is null and :scheduledToExclusive is null
					             and :reportedFrom is null and :reportedToExclusive is null)
					        or (
					            mo.scheduled_start_at is not null
					            and (:scheduledFrom is null or mo.scheduled_start_at >= :scheduledFrom)
					            and (:scheduledToExclusive is null or mo.scheduled_start_at < :scheduledToExclusive)
					        )
					        or (
					            mo.scheduled_start_at is null
					            and (:reportedFrom is null or mo.reported_at >= :reportedFrom)
					            and (:reportedToExclusive is null or mo.reported_at < :reportedToExclusive)
					        )
					  )
					group by mo.location_type_snapshot
					""",
			nativeQuery = true
	)
	List<MaintenanceSummaryBreakdownView> findLocationTypeSummaryBreakdown(
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query(
			value = """
					select
					    mo.status as code,
					    case
					        when mo.status = 'OPEN' then 'Aberta'
					        when mo.status = 'ASSIGNED' then 'Atribuida'
					        when mo.status = 'SCHEDULED' then 'Agendada'
					        when mo.status = 'IN_PROGRESS' then 'Em andamento'
					        when mo.status = 'COMPLETED' then 'Concluida'
					        when mo.status = 'CANCELLED' then 'Cancelada'
					    end as label,
					    coalesce(sum(case when mo.status in ('OPEN', 'ASSIGNED') then 1 else 0 end), 0) as openCount,
					    coalesce(sum(case when mo.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when mo.status = 'IN_PROGRESS' then 1 else 0 end), 0) as inProgressCount,
					    coalesce(sum(case when mo.status = 'COMPLETED' then 1 else 0 end), 0) as completedCount,
					    coalesce(sum(case when mo.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when mo.priority = 'URGENT' then 1 else 0 end), 0) as urgentCount
					from maintenance_orders mo
					where (
					        (:scheduledFrom is null and :scheduledToExclusive is null
					             and :reportedFrom is null and :reportedToExclusive is null)
					        or (
					            mo.scheduled_start_at is not null
					            and (:scheduledFrom is null or mo.scheduled_start_at >= :scheduledFrom)
					            and (:scheduledToExclusive is null or mo.scheduled_start_at < :scheduledToExclusive)
					        )
					        or (
					            mo.scheduled_start_at is null
					            and (:reportedFrom is null or mo.reported_at >= :reportedFrom)
					            and (:reportedToExclusive is null or mo.reported_at < :reportedToExclusive)
					        )
					  )
					group by mo.status
					""",
			nativeQuery = true
	)
	List<MaintenanceSummaryBreakdownView> findStatusSummaryBreakdown(
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Query("""
			select
				o.id as orderId,
				o.locationTypeSnapshot as locationTypeSnapshot,
				o.locationLabelSnapshot as locationLabelSnapshot,
				o.providerTypeSnapshot as providerTypeSnapshot,
				o.providerNameSnapshot as providerNameSnapshot,
				o.serviceLabelSnapshot as serviceLabelSnapshot,
				o.title as title,
				o.priority as priority,
				o.businessPriority as businessPriority,
				o.requestOrigin as requestOrigin,
				o.requestedForGuest as requestedForGuest,
				o.assignedUsername as assignedUsername,
				o.estimatedExecutionMinutes as estimatedExecutionMinutes,
				o.status as status,
				o.reportedAt as reportedAt,
				o.scheduledStartAt as scheduledStartAt,
				o.scheduledEndAt as scheduledEndAt,
				o.startedAt as startedAt,
				o.completedAt as completedAt
			from MaintenanceOrder o
			left join o.provider p
			where (:providerId is null or p.id = :providerId)
			  and (:providerUnassigned = false or p is null)
			  and (:providerType is null or o.providerTypeSnapshot = :providerType)
			  and (:providerTypeUnassigned = false or o.providerTypeSnapshot is null)
			  and (:locationType is null or o.locationTypeSnapshot = :locationType)
			  and (:status is null or o.status = :status)
			  and (
			        (:scheduledFrom is null and :scheduledToExclusive is null
			             and :reportedFrom is null and :reportedToExclusive is null)
			        or (
			            o.scheduledStartAt is not null
			            and (:scheduledFrom is null or o.scheduledStartAt >= :scheduledFrom)
			            and (:scheduledToExclusive is null or o.scheduledStartAt < :scheduledToExclusive)
			        )
			        or (
			            o.scheduledStartAt is null
			            and (:reportedFrom is null or o.reportedAt >= :reportedFrom)
			            and (:reportedToExclusive is null or o.reportedAt < :reportedToExclusive)
			        )
			  )
			""")
	List<MaintenanceSummaryDetailItemView> findSummaryDetailItems(
			@Param("providerId") Long providerId,
			@Param("providerUnassigned") boolean providerUnassigned,
			@Param("providerType") MaintenanceProviderType providerType,
			@Param("providerTypeUnassigned") boolean providerTypeUnassigned,
			@Param("locationType") MaintenanceLocationType locationType,
			@Param("status") MaintenanceOrderStatus status,
			@Param("scheduledFrom") LocalDateTime scheduledFrom,
			@Param("scheduledToExclusive") LocalDateTime scheduledToExclusive,
			@Param("reportedFrom") OffsetDateTime reportedFrom,
			@Param("reportedToExclusive") OffsetDateTime reportedToExclusive
	);

	@Modifying
	@Query("""
			delete from MaintenanceOrder o
			where lower(o.createdBy) like lower(concat(:prefix, '%'))
			""")
	int deleteInBulkByCreatedByPrefix(@Param("prefix") String prefix);

	List<MaintenanceOrder> findByLocationIdOrderByReportedAtDescIdDesc(Long locationId);

	@Query("""
			select
				o.id as id,
				o.location.id as locationId,
				o.locationTypeSnapshot as locationTypeSnapshot,
				o.locationCodeSnapshot as locationCodeSnapshot,
				o.locationLabelSnapshot as locationLabelSnapshot,
				p.id as providerId,
				o.providerTypeSnapshot as providerTypeSnapshot,
				o.providerNameSnapshot as providerNameSnapshot,
				o.serviceLabelSnapshot as serviceLabelSnapshot,
				o.title as title,
				o.description as description,
				o.priority as priority,
				o.requestOrigin as requestOrigin,
				o.requestedForGuest as requestedForGuest,
				o.guestName as guestName,
				o.guestReference as guestReference,
				o.requestedByUsername as requestedByUsername,
				o.requestedByRole as requestedByRole,
				o.businessPriority as businessPriority,
				o.estimatedExecutionMinutes as estimatedExecutionMinutes,
				o.assignedUsername as assignedUsername,
				o.assignedAt as assignedAt,
				o.status as status,
				o.reportedAt as reportedAt,
				o.scheduledStartAt as scheduledStartAt,
				o.scheduledEndAt as scheduledEndAt,
				o.startedAt as startedAt,
				o.completedAt as completedAt,
				o.paid as paid,
				o.paymentMethod as paymentMethod,
				o.paymentDate as paymentDate,
				o.paymentNotes as paymentNotes,
				o.resolutionNotes as resolutionNotes,
				o.cancellationNotes as cancellationNotes,
				o.createdAt as createdAt,
				o.updatedAt as updatedAt,
				o.cancelledAt as cancelledAt,
				o.createdBy as createdBy,
				o.updatedBy as updatedBy,
				o.cancelledBy as cancelledBy
			from MaintenanceOrder o
			left join o.provider p
			where o.location.id = :locationId
			order by o.reportedAt desc, o.id desc
			""")
	List<MaintenanceOrderHistoryItemView> findHistoryItemsByLocationIdOrderByReportedAtDescIdDesc(
			@Param("locationId") Long locationId
	);

	default List<MaintenanceOrder> findAllOrdered() {
		return findAll(Sort.by(
				Sort.Order.asc("scheduledStartAt"),
				Sort.Order.asc("reportedAt"),
				Sort.Order.asc("id")
		));
	}
}
