package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtBooking;
import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.domain.model.CourtPaymentMethod;
import com.axioma.quadras.domain.model.CourtPricingPeriod;
import com.axioma.quadras.repository.CourtBookingListItemView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public record CourtBookingDto(
		Long id,
		LocalDate bookingDate,
		LocalTime startTime,
		LocalTime endTime,
		Integer durationMinutes,
		String customerName,
		String customerReference,
		CourtCustomerType customerType,
		CourtPricingPeriod pricingPeriod,
		LocalTime sunriseEstimate,
		LocalTime sunsetEstimate,
		BigDecimal courtAmount,
		BigDecimal materialsAmount,
		BigDecimal totalAmount,
		Boolean paid,
		CourtPaymentMethod paymentMethod,
		LocalDate paymentDate,
		String paymentNotes,
		CourtBookingStatus status,
		String cancellationNotes,
		List<CourtBookingMaterialDto> materials,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime cancelledAt,
		String createdBy,
		String updatedBy,
		String cancelledBy
) {
	public static CourtBookingDto from(CourtBooking booking) {
		return new CourtBookingDto(
				booking.getId(),
				booking.getBookingDate(),
				booking.getStartTime(),
				booking.getEndTime(),
				booking.getDurationMinutes(),
				booking.getCustomerName(),
				booking.getCustomerReference(),
				booking.getCustomerType(),
				booking.getPricingPeriod(),
				booking.getSunriseEstimate(),
				booking.getSunsetEstimate(),
				booking.getCourtAmount(),
				booking.getMaterialsAmount(),
				booking.getTotalAmount(),
				booking.isPaid(),
				booking.getPaymentMethod(),
				booking.getPaymentDate(),
				booking.getPaymentNotes(),
				booking.getStatus(),
				booking.getCancellationNotes(),
				booking.getMaterials().stream().map(CourtBookingMaterialDto::from).toList(),
				booking.getCreatedAt(),
				booking.getUpdatedAt(),
				booking.getCancelledAt(),
				booking.getCreatedBy(),
				booking.getUpdatedBy(),
				booking.getCancelledBy()
		);
	}

	public static CourtBookingDto from(
			CourtBookingListItemView booking,
			List<CourtBookingMaterialDto> materials
	) {
		return new CourtBookingDto(
				booking.getId(),
				booking.getBookingDate(),
				booking.getStartTime(),
				booking.getEndTime(),
				booking.getDurationMinutes(),
				booking.getCustomerName(),
				booking.getCustomerReference(),
				booking.getCustomerType(),
				booking.getPricingPeriod(),
				booking.getSunriseEstimate(),
				booking.getSunsetEstimate(),
				booking.getCourtAmount(),
				booking.getMaterialsAmount(),
				booking.getTotalAmount(),
				Boolean.TRUE.equals(booking.getPaid()),
				booking.getPaymentMethod(),
				booking.getPaymentDate(),
				booking.getPaymentNotes(),
				booking.getStatus(),
				booking.getCancellationNotes(),
				List.copyOf(materials),
				booking.getCreatedAt(),
				booking.getUpdatedAt(),
				booking.getCancelledAt(),
				booking.getCreatedBy(),
				booking.getUpdatedBy(),
				booking.getCancelledBy()
		);
	}
}
