package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdateCourtBookingDto(
		@NotNull(message = "bookingDate is required")
		LocalDate bookingDate,
		@NotNull(message = "startTime is required")
		LocalTime startTime,
		@NotNull(message = "endTime is required")
		LocalTime endTime,
		@NotBlank(message = "customerName is required")
		@Size(max = 120, message = "customerName must be <= 120 chars")
		String customerName,
		@NotBlank(message = "customerReference is required")
		@Size(max = 120, message = "customerReference must be <= 120 chars")
		String customerReference,
		@NotNull(message = "customerType is required")
		CourtCustomerType customerType,
		@NotNull(message = "paid is required")
		Boolean paid,
		CourtPaymentMethod paymentMethod,
		LocalDate paymentDate,
		@Size(max = 500, message = "paymentNotes must be <= 500 chars")
		String paymentNotes,
		@Valid
		List<CourtBookingMaterialInputDto> materials
) {
}
