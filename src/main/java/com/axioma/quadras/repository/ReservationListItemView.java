package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public interface ReservationListItemView {
	Long getId();

	String getGuestName();

	LocalDate getReservationDate();

	LocalTime getStartTime();

	LocalTime getEndTime();

	ReservationStatus getStatus();

	String getNotes();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();
}
