package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MassagePaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateMassageBookingDto(
		@NotNull(message = "bookingDate is required")
		LocalDate bookingDate,
		@NotNull(message = "startTime is required")
		LocalTime startTime,
		@NotBlank(message = "clientName is required")
		String clientName,
		@NotBlank(message = "guestReference is required")
		String guestReference,
		@NotBlank(message = "treatment is required")
		String treatment,
		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.01", message = "amount must be greater than zero")
		BigDecimal amount,
		@NotNull(message = "providerId is required")
		Long providerId,
		@NotNull(message = "therapistId is required")
		Long therapistId,
		boolean paid,
		MassagePaymentMethod paymentMethod,
		LocalDate paymentDate,
		String paymentNotes
) {
}
