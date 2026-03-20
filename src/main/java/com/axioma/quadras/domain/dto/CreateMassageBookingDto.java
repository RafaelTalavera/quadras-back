package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassagePaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateMassageBookingDto(
		@NotNull(message = "bookingDate is required")
		LocalDate bookingDate,
		@NotNull(message = "startTime is required")
		LocalTime startTime,
		@NotBlank(message = "clientName is required")
		@Size(max = 120, message = "clientName must be <= 120 chars")
		String clientName,
		@NotBlank(message = "guestReference is required")
		@Size(max = 120, message = "guestReference must be <= 120 chars")
		String guestReference,
		@NotBlank(message = "treatment is required")
		@Size(max = 120, message = "treatment must be <= 120 chars")
		String treatment,
		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.01", message = "amount must be greater than zero")
		BigDecimal amount,
		@NotNull(message = "providerId is required")
		Long providerId,
		@NotNull(message = "paid is required")
		Boolean paid,
		MassagePaymentMethod paymentMethod,
		LocalDate paymentDate,
		@Size(max = 500, message = "paymentNotes must be <= 500 chars")
		String paymentNotes
) {
}
