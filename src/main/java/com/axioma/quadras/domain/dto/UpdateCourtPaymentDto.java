package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtPaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateCourtPaymentDto(
		@NotNull(message = "paymentMethod is required")
		CourtPaymentMethod paymentMethod,
		@NotNull(message = "paymentDate is required")
		LocalDate paymentDate,
		@Size(max = 500, message = "paymentNotes must be <= 500 chars")
		String paymentNotes
) {
}
