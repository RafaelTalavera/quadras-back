package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelLaundryShipmentDto(@NotBlank @Size(max = 500) String cancellationNotes) { }
