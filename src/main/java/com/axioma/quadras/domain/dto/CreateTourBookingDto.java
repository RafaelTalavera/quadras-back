package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.TourPaymentMethod;
import com.axioma.quadras.domain.model.TourServiceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateTourBookingDto(
		@NotNull(message = "serviceType is required")
		TourServiceType serviceType,
		@NotNull(message = "startAt is required")
		LocalDateTime startAt,
		@NotNull(message = "endAt is required")
		LocalDateTime endAt,
		@NotBlank(message = "clientName is required")
		@Size(max = 120, message = "clientName must be <= 120 chars")
		String clientName,
		@NotBlank(message = "guestReference is required")
		@Size(max = 120, message = "guestReference must be <= 120 chars")
		String guestReference,
		@NotNull(message = "providerId is required")
		Long providerId,
		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.00", message = "amount must be >= 0")
		BigDecimal amount,
		@NotNull(message = "commissionPercent is required")
		@DecimalMin(value = "0.00", message = "commissionPercent must be >= 0")
		@DecimalMax(value = "100.00", message = "commissionPercent must be <= 100")
		BigDecimal commissionPercent,
		@Size(max = 1000, message = "description must be <= 1000 chars")
		String description,
		@NotNull(message = "paid is required")
		Boolean paid,
		TourPaymentMethod paymentMethod,
		LocalDate paymentDate,
		@Size(max = 500, message = "paymentNotes must be <= 500 chars")
		String paymentNotes
) {
}
