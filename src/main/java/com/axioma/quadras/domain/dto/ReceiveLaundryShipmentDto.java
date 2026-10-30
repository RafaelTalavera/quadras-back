package com.axioma.quadras.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public record ReceiveLaundryShipmentDto(
		@NotNull OffsetDateTime receivedAt,
		@NotNull Long version,
		@NotEmpty List<@Valid ReceiveLaundryLineDto> lines
) { }
