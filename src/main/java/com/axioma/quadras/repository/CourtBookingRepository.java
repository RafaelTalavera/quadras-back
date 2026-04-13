package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.domain.model.CourtCustomerType;
import java.lang.Boolean;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourtBookingRepository extends JpaRepository<CourtBooking, Long> {

	boolean existsByBookingDateAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate bookingDate,
			CourtBookingStatus status,
			LocalTime endTime,
			LocalTime startTime
	);

	boolean existsByBookingDateAndStatusAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
			LocalDate bookingDate,
			CourtBookingStatus status,
			Long id,
			LocalTime endTime,
			LocalTime startTime
	);

	@Query(
			value = """
					select
					    coalesce(sum(case when cb.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when cb.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then 1 else 0 end), 0) as paidCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = false then 1 else 0 end), 0) as pendingCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.duration_minutes else 0 end), 0) as totalMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.customer_type = 'GUEST' then cb.duration_minutes else 0 end), 0) as guestMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.customer_type = 'VIP' then cb.duration_minutes else 0 end), 0) as vipMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.customer_type = 'EXTERNAL' then cb.duration_minutes else 0 end), 0) as externalMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.customer_type = 'PARTNER_COACH' then cb.duration_minutes else 0 end), 0) as partnerCoachMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then cb.total_amount else 0 end), 0) as paidAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = false then cb.total_amount else 0 end), 0) as pendingAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.court_amount else 0 end), 0) as courtAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.materials_amount else 0 end), 0) as materialsAmount
					from court_bookings cb
					where (:dateFrom is null or cb.booking_date >= :dateFrom)
					  and (:dateTo is null or cb.booking_date <= :dateTo)
					""",
			nativeQuery = true
	)
	CourtSummaryAggregateView findSummaryAggregate(
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query(
			value = """
					select
					    cb.customer_type as code,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then 1 else 0 end), 0) as paidCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = false then 1 else 0 end), 0) as pendingCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.duration_minutes else 0 end), 0) as totalMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.court_amount else 0 end), 0) as courtAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.materials_amount else 0 end), 0) as materialsAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.total_amount else 0 end), 0) as totalAmount
					from court_bookings cb
					where (:dateFrom is null or cb.booking_date >= :dateFrom)
					  and (:dateTo is null or cb.booking_date <= :dateTo)
					group by cb.customer_type
					""",
			nativeQuery = true
	)
	List<CourtSummaryBreakdownView> findCustomerTypeSummaryBreakdown(
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query(
			value = """
					select
					    cb.pricing_period as code,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then 1 else 0 end), 0) as paidCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = false then 1 else 0 end), 0) as pendingCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.duration_minutes else 0 end), 0) as totalMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.court_amount else 0 end), 0) as courtAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.materials_amount else 0 end), 0) as materialsAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' then cb.total_amount else 0 end), 0) as totalAmount
					from court_bookings cb
					where (:dateFrom is null or cb.booking_date >= :dateFrom)
					  and (:dateTo is null or cb.booking_date <= :dateTo)
					group by cb.pricing_period
					""",
			nativeQuery = true
	)
	List<CourtSummaryBreakdownView> findPricingPeriodSummaryBreakdown(
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query(
			value = """
					select
					    cb.payment_method as code,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then 1 else 0 end), 0) as scheduledCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then 1 else 0 end), 0) as paidCount,
					    0 as pendingCount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then cb.duration_minutes else 0 end), 0) as totalMinutes,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then cb.court_amount else 0 end), 0) as courtAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then cb.materials_amount else 0 end), 0) as materialsAmount,
					    coalesce(sum(case when cb.status = 'SCHEDULED' and cb.paid = true then cb.total_amount else 0 end), 0) as totalAmount
					from court_bookings cb
					where cb.payment_method is not null
					  and (:dateFrom is null or cb.booking_date >= :dateFrom)
					  and (:dateTo is null or cb.booking_date <= :dateTo)
					group by cb.payment_method
					""",
			nativeQuery = true
	)
	List<CourtSummaryBreakdownView> findPaymentMethodSummaryBreakdown(
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query("""
			select
				b.id as id,
				b.bookingDate as bookingDate,
				b.startTime as startTime,
				b.endTime as endTime,
				b.durationMinutes as durationMinutes,
				b.customerName as customerName,
				b.customerReference as customerReference,
				b.customerType as customerType,
				b.pricingPeriod as pricingPeriod,
				b.sunriseEstimate as sunriseEstimate,
				b.sunsetEstimate as sunsetEstimate,
				b.courtAmount as courtAmount,
				b.materialsAmount as materialsAmount,
				b.totalAmount as totalAmount,
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
			from CourtBooking b
			where (:bookingDate is null or b.bookingDate = :bookingDate)
			  and (:customerType is null or b.customerType = :customerType)
			  and (:paid is null or b.paid = :paid)
			order by b.bookingDate asc, b.startTime asc, b.id asc
			""")
	List<CourtBookingListItemView> findListItems(
			@Param("bookingDate") LocalDate bookingDate,
			@Param("customerType") CourtCustomerType customerType,
			@Param("paid") Boolean paid
	);
}
