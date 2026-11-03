package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ObserveLaundryDocumentDto(
		@PositiveOrZero(message = "version must be >= 0") long version,
		@NotBlank(message = "notes are required")
		@Size(max = 500, message = "notes must be <= 500 chars") String notes) { }
