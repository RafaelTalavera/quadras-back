package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourBooking;
import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourServiceType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {

	@Query(
			value = """
					select
					    coalesce(sum(case when tb.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when tb.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when tb.status = 'SCHEDULED' and tb.paid = true then 1 else 0 end), 0) as paidCount,
					    coalesce(sum(case when tb.status = 'SCHEDULED' and tb.paid = false then 1 else 0 end), 0) as pendingCount,
					    coalesce(sum(case when tb.status = 'SCHEDULED' then timestampdiff(MINUTE, tb.start_at, tb.end_at) else 0 end), 0) as totalMinutes,
					    coalesce(sum(case when tb.status = 'SCHEDULED' then tb.amount else 0 end), 0) as grossAmount,
					    coalesce(sum(case when tb.status = 'SCHEDULED' and tb.paid = true then tb.amount else 0 end), 0) as paidAmount,
					    coalesce(sum(case when tb.status = 'SCHEDULED' and tb.paid = false then tb.amount else 0 end), 0) as pendingAmount,
					    coalesce(sum(case when tb.status = 'SCHEDULED' then tb.commission_amount else 0 end), 0) as commissionAmount
					from tour_bookings tb
					where (:startAtFrom is null or tb.start_at >= :startAtFrom)
					  and (:startAtTo is null or tb.start_at <= :startAtTo)
					""",
			nativeQuery = true
	)
	TourSummaryAggregateView findSummaryAggregate(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo
	);

	@Query(
			value = """
					select
					    cast(tp.id as varchar) as code,
					    tp.name as label,
					    tp.active as active,
					    count(*) as scheduledCount,
					    coalesce(sum(case when tb.paid = true then 1 else 0 end), 0) as paidCount,
					    coalesce(sum(case when tb.paid = false then 1 else 0 end), 0) as pendingCount,
					    coalesce(sum(timestampdiff(MINUTE, tb.start_at, tb.end_at)), 0) as totalMinutes,
					    coalesce(sum(tb.amount), 0) as grossAmount,
					    coalesce(sum(case when tb.paid = true then tb.amount else 0 end), 0) as paidAmount,
					    coalesce(sum(case when tb.paid = false then tb.amount else 0 end), 0) as pendingAmount,
					    coalesce(sum(tb.commission_amount), 0) as commissionAmount
					from tour_bookings tb
					join tour_providers tp on tp.id = tb.provider_id
					where tb.status = 'SCHEDULED'
					  and (:startAtFrom is null or tb.start_at >= :startAtFrom)
					  and (:startAtTo is null or tb.start_at <= :startAtTo)
					group by tp.id, tp.name, tp.active
					order by lower(tp.name), tp.id
					""",
			nativeQuery = true
	)
	List<TourSummaryBreakdownView> findProviderSummaryBreakdown(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo
	);

	@Query(
			value = """
					select
					    tb.service_type as code,
					    tb.service_type as label,
					    null as active,
					    count(*) as scheduledCount,
					    coalesce(sum(case when tb.paid = true then 1 else 0 end), 0) as paidCount,
					    coalesce(sum(case when tb.paid = false then 1 else 0 end), 0) as pendingCount,
					    coalesce(sum(timestampdiff(MINUTE, tb.start_at, tb.end_at)), 0) as totalMinutes,
					    coalesce(sum(tb.amount), 0) as grossAmount,
					    coalesce(sum(case when tb.paid = true then tb.amount else 0 end), 0) as paidAmount,
					    coalesce(sum(case when tb.paid = false then tb.amount else 0 end), 0) as pendingAmount,
					    coalesce(sum(tb.commission_amount), 0) as commissionAmount
					from tour_bookings tb
					where tb.status = 'SCHEDULED'
					  and (:startAtFrom is null or tb.start_at >= :startAtFrom)
					  and (:startAtTo is null or tb.start_at <= :startAtTo)
					group by tb.service_type
					""",
			nativeQuery = true
	)
	List<TourSummaryBreakdownView> findServiceTypeSummaryBreakdown(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo
	);

	@Query(
			value = """
					select
					    tb.payment_method as code,
					    tb.payment_method as label,
					    null as active,
					    count(*) as scheduledCount,
					    count(*) as paidCount,
					    0 as pendingCount,
					    coalesce(sum(timestampdiff(MINUTE, tb.start_at, tb.end_at)), 0) as totalMinutes,
					    coalesce(sum(tb.amount), 0) as grossAmount,
					    coalesce(sum(tb.amount), 0) as paidAmount,
					    0 as pendingAmount,
					    coalesce(sum(tb.commission_amount), 0) as commissionAmount
					from tour_bookings tb
					where tb.status = 'SCHEDULED'
					  and tb.paid = true
					  and tb.payment_method is not null
					  and (:startAtFrom is null or tb.start_at >= :startAtFrom)
					  and (:startAtTo is null or tb.start_at <= :startAtTo)
					group by tb.payment_method
					""",
			nativeQuery = true
	)
	List<TourSummaryBreakdownView> findPaymentMethodSummaryBreakdown(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo
	);

	@Query(
			value = """
					select
					    tb.id as bookingId,
					    tb.start_at as startAt,
					    tb.end_at as endAt,
					    tb.service_type as serviceType,
					    tb.provider_id as providerId,
					    tp.name as providerName,
					    tb.provider_offering_id as providerOfferingId,
					    tb.provider_offering_name as providerOfferingName,
					    tb.client_name as clientName,
					    tb.guest_reference as guestReference,
					    tb.amount as amount,
					    tb.commission_amount as commissionAmount,
					    tb.paid as paid,
					    tb.payment_method as paymentMethod,
					    tb.payment_date as paymentDate,
					    tb.status as status,
					    tb.description as description
					from tour_bookings tb
					join tour_providers tp on tp.id = tb.provider_id
					where tb.status = 'SCHEDULED'
					  and (:startAtFrom is null or tb.start_at >= :startAtFrom)
					  and (:startAtTo is null or tb.start_at <= :startAtTo)
					  and (:providerId is null or tb.provider_id = :providerId)
					  and (:serviceType is null or tb.service_type = :serviceType)
					  and (:paymentMethod is null or tb.payment_method = :paymentMethod)
					order by tb.start_at, tb.id
					""",
			nativeQuery = true
	)
	List<TourSummaryDetailItemView> findSummaryDetailItems(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo,
			@Param("providerId") Long providerId,
			@Param("serviceType") String serviceType,
			@Param("paymentMethod") String paymentMethod
	);

	@Query("""
			select
			    p.id as providerId,
			    p.name as providerName,
			    p.active as providerActive,
			    sum(case when b.status = :scheduledStatus then 1 else 0 end) as scheduledCount,
			    sum(case when b.status = :cancelledStatus then 1 else 0 end) as cancelledCount,
			    sum(case when b.status = :scheduledStatus and b.paid = true then 1 else 0 end) as paidCount,
			    sum(case when b.status = :scheduledStatus and b.paid = false then 1 else 0 end) as pendingCount,
			    coalesce(sum(case when b.status = :scheduledStatus then b.amount else 0 end), 0) as grossAmount,
			    coalesce(sum(case when b.status = :scheduledStatus and b.paid = true then b.amount else 0 end), 0) as paidAmount,
			    coalesce(sum(case when b.status = :scheduledStatus and b.paid = false then b.amount else 0 end), 0) as pendingAmount,
			    coalesce(sum(case when b.status = :scheduledStatus then b.commissionAmount else 0 end), 0) as commissionAmount,
			    max(b.startAt) as lastBookingAt
			from TourBooking b
			join b.provider p
			where (:startAtFrom is null or b.startAt >= :startAtFrom)
			  and (:startAtTo is null or b.startAt <= :startAtTo)
			group by p.id, p.name, p.active
			order by lower(p.name)
			""")
	List<TourProviderSummaryView> findProviderSummary(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo,
			@Param("scheduledStatus") TourBookingStatus scheduledStatus,
			@Param("cancelledStatus") TourBookingStatus cancelledStatus
	);

	@Query("""
			select
			    b.id as id,
			    b.serviceType as serviceType,
			    b.startAt as startAt,
			    b.endAt as endAt,
			    b.clientName as clientName,
			    b.guestReference as guestReference,
			    p.id as providerId,
			    p.name as providerName,
			    p.active as providerActive,
			    o.id as providerOfferingId,
			    b.providerOfferingName as providerOfferingName,
			    b.amount as amount,
			    b.commissionPercent as commissionPercent,
			    b.commissionAmount as commissionAmount,
			    b.description as description,
			    b.paid as paid,
			    b.paymentMethod as paymentMethod,
			    b.paymentDate as paymentDate,
			    b.paymentNotes as paymentNotes,
			    b.status as status,
			    b.cancellationNotes as cancellationNotes,
			    b.createdAt as createdAt,
			    b.updatedAt as updatedAt,
			    b.cancelledAt as cancelledAt,
			    b.createdBy as createdBy,
			    b.updatedBy as updatedBy,
			    b.cancelledBy as cancelledBy
			from TourBooking b
			join b.provider p
			left join b.providerOffering o
			where (:startAtFrom is null or b.startAt >= :startAtFrom)
			  and (:startAtTo is null or b.startAt <= :startAtTo)
			  and (:providerId is null or p.id = :providerId)
			  and (:paid is null or b.paid = :paid)
			  and (:serviceType is null or b.serviceType = :serviceType)
			order by b.startAt asc, b.id asc
			""")
	List<TourBookingListItemView> findListItems(
			@Param("startAtFrom") LocalDateTime startAtFrom,
			@Param("startAtTo") LocalDateTime startAtTo,
			@Param("providerId") Long providerId,
			@Param("paid") Boolean paid,
			@Param("serviceType") TourServiceType serviceType
	);
}
