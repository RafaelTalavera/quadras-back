package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record DispatchLaundryShipmentDto(
		@NotNull OffsetDateTime sentAt,
		@NotNull @PositiveOrZero Long version,
		@Size(max = 500) String notes
) { }
