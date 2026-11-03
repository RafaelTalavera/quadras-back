package com.axioma.quadras.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ConfirmLaundryReturnDto(
		@NotNull @Min(0) Long version,
		@Size(max = 500) String correctionNotes,
		@NotEmpty @Valid List<ConfirmLaundryReturnLineDto> lines
) { }
