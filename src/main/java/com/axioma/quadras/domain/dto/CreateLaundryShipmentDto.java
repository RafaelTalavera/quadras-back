package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryShipmentKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateLaundryShipmentDto(
		@NotNull Long providerId,
		LaundryShipmentKind kind,
		@NotNull OffsetDateTime sentAt,
		@Size(max = 500) String notes,
		@NotEmpty List<@Valid CreateLaundryShipmentLineDto> lines
) { }
