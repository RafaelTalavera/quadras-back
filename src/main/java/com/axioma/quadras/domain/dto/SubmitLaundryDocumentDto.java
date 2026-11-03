package com.axioma.quadras.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitLaundryDocumentDto(@NotNull @Min(0) Long version) { }
