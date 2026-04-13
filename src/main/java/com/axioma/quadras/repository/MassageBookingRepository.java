package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MassageBookingRepository extends JpaRepository<MassageBooking, Long> {

	boolean existsByTherapistIdAndBookingDateAndStartTimeAndStatus(
			Long therapistId,
			LocalDate bookingDate,
			LocalTime startTime,
			MassageBookingStatus status
	);

	boolean existsByTherapistIdAndBookingDateAndStartTimeAndStatusAndIdNot(
			Long therapistId,
			LocalDate bookingDate,
			LocalTime startTime,
			MassageBookingStatus status,
			Long id
	);

	@Query(
			value = """
					WITH booking_agg AS (
					    SELECT
					        mb.provider_id AS providerId,
					        mp.name AS providerName,
					        mp.active AS providerActive,
					        COALESCE(tc.therapistsCount, 0) AS therapistsCount,
					        SUM(CASE WHEN mb.status = 'SCHEDULED' THEN 1 ELSE 0 END) AS scheduledCount,
					        SUM(CASE WHEN mb.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelledCount,
					        SUM(CASE WHEN mb.status = 'SCHEDULED' AND mb.paid = TRUE THEN 1 ELSE 0 END) AS paidCount,
					        SUM(CASE WHEN mb.status = 'SCHEDULED' AND mb.paid = FALSE THEN 1 ELSE 0 END) AS pendingCount,
					        COALESCE(SUM(CASE WHEN mb.status = 'SCHEDULED' THEN mb.amount ELSE 0 END), 0) AS grossAmount,
					        COALESCE(SUM(CASE WHEN mb.status = 'SCHEDULED' AND mb.paid = TRUE THEN mb.amount ELSE 0 END), 0) AS paidAmount,
					        COALESCE(SUM(CASE WHEN mb.status = 'SCHEDULED' AND mb.paid = FALSE THEN mb.amount ELSE 0 END), 0) AS pendingAmount
					    FROM massage_bookings mb
					    JOIN massage_providers mp ON mp.id = mb.provider_id
					    LEFT JOIN (
					        SELECT provider_id, COUNT(*) AS therapistsCount
					        FROM massage_therapists
					        GROUP BY provider_id
					    ) tc ON tc.provider_id = mb.provider_id
					    WHERE mb.booking_date >= :dateFrom
					      AND mb.booking_date <= :dateTo
					      AND (:providerId IS NULL OR mb.provider_id = :providerId)
					    GROUP BY mb.provider_id, mp.name, mp.active, tc.therapistsCount
					),
					last_booking AS (
					    SELECT providerId, booking_date AS lastBookingDate, start_time AS lastBookingTime
					    FROM (
					        SELECT
					            mb.provider_id AS providerId,
					            mb.booking_date,
					            mb.start_time,
					            ROW_NUMBER() OVER (
					                PARTITION BY mb.provider_id
					                ORDER BY mb.booking_date DESC, mb.start_time DESC
					            ) AS rn
					        FROM massage_bookings mb
					        WHERE mb.booking_date >= :dateFrom
					          AND mb.booking_date <= :dateTo
					          AND (:providerId IS NULL OR mb.provider_id = :providerId)
					    ) ranked
					    WHERE rn = 1
					)
					SELECT
					    booking_agg.providerId AS providerId,
					    booking_agg.providerName AS providerName,
					    booking_agg.providerActive AS providerActive,
					    booking_agg.therapistsCount AS therapistsCount,
					    booking_agg.scheduledCount AS scheduledCount,
					    booking_agg.cancelledCount AS cancelledCount,
					    booking_agg.paidCount AS paidCount,
					    booking_agg.pendingCount AS pendingCount,
					    booking_agg.grossAmount AS grossAmount,
					    booking_agg.paidAmount AS paidAmount,
					    booking_agg.pendingAmount AS pendingAmount,
					    last_booking.lastBookingDate AS lastBookingDate,
					    last_booking.lastBookingTime AS lastBookingTime
					FROM booking_agg
					LEFT JOIN last_booking ON last_booking.providerId = booking_agg.providerId
					ORDER BY LOWER(booking_agg.providerName)
					""",
			nativeQuery = true
	)
	List<MassageProviderSummaryView> findProviderSummary(
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo,
			@Param("providerId") Long providerId
	);

	@Query(
			value = """
					select
					    mb.id as bookingId,
					    mb.booking_date as bookingDate,
					    mb.start_time as startTime,
					    mb.client_name as clientName,
					    mb.guest_reference as guestReference,
					    mb.treatment as treatment,
					    mb.therapist_id as therapistId,
					    mt.name as therapistName,
					    mb.amount as amount,
					    mb.paid as paid,
					    mb.payment_method as paymentMethod,
					    mb.payment_date as paymentDate,
					    mb.payment_notes as paymentNotes,
					    mb.status as status,
					    mb.cancellation_notes as cancellationNotes
					from massage_bookings mb
					join massage_therapists mt on mt.id = mb.therapist_id
					where mb.provider_id = :providerId
					  and mb.booking_date >= :dateFrom
					  and mb.booking_date <= :dateTo
					order by mb.booking_date, mb.start_time, mb.id
					""",
			nativeQuery = true
	)
	List<MassageProviderReportItemView> findProviderReportItems(
			@Param("providerId") Long providerId,
			@Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo
	);

	@Query("""
			select
			    b.id as id,
			    b.bookingDate as bookingDate,
			    b.startTime as startTime,
			    b.clientName as clientName,
			    b.guestReference as guestReference,
			    b.treatment as treatment,
			    b.amount as amount,
			    p.id as providerId,
			    p.name as providerName,
			    p.active as providerActive,
			    t.id as therapistId,
			    t.name as therapistName,
			    t.active as therapistActive,
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
			from MassageBooking b
			join b.provider p
			join b.therapist t
			where (:bookingDate is null or b.bookingDate = :bookingDate)
			  and (:clientName is null or lower(b.clientName) like concat('%', :clientName, '%'))
			  and (:guestReference is null or lower(b.guestReference) like concat('%', :guestReference, '%'))
			  and (:providerId is null or p.id = :providerId)
			  and (:paid is null or b.paid = :paid)
			order by b.bookingDate asc, b.startTime asc, b.id asc
			""")
	List<MassageBookingListItemView> findListItems(
			@Param("bookingDate") LocalDate bookingDate,
			@Param("clientName") String clientName,
			@Param("guestReference") String guestReference,
			@Param("providerId") Long providerId,
			@Param("paid") Boolean paid
	);
}
