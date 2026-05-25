package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.Reservation;
import com.axioma.quadras.domain.model.ReservationStatus;
import com.axioma.quadras.repository.ReservationListItemView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record ReservationDto(
		Long id,
		String guestName,
		LocalDate reservationDate,
		LocalTime startTime,
		LocalTime endTime,
		ReservationStatus status,
		String notes,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		String createdBy,
		String updatedBy,
		OffsetDateTime cancelledAt,
		String cancelledBy
) {
	public static ReservationDto from(Reservation reservation) {
		return new ReservationDto(
				reservation.getId(),
				reservation.getGuestName(),
				reservation.getReservationDate(),
				reservation.getStartTime(),
				reservation.getEndTime(),
				reservation.getStatus(),
				reservation.getNotes(),
				reservation.getCreatedAt(),
				reservation.getUpdatedAt(),
				reservation.getCreatedBy(),
				reservation.getUpdatedBy(),
				reservation.getCancelledAt(),
				reservation.getCancelledBy()
		);
	}

	public static ReservationDto from(ReservationListItemView reservation) {
		return new ReservationDto(
				reservation.getId(),
				reservation.getGuestName(),
				reservation.getReservationDate(),
				reservation.getStartTime(),
				reservation.getEndTime(),
				reservation.getStatus(),
				reservation.getNotes(),
				reservation.getCreatedAt(),
				reservation.getUpdatedAt(),
				reservation.getCreatedBy(),
				reservation.getUpdatedBy(),
				reservation.getCancelledAt(),
				reservation.getCancelledBy()
		);
	}
}
