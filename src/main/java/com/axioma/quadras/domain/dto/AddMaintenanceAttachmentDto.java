package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceAttachmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddMaintenanceAttachmentDto(
		@NotNull(message = "attachmentType is required")
		MaintenanceAttachmentType attachmentType,
		@NotBlank(message = "fileName is required")
		@Size(max = 255, message = "fileName must be <= 255 chars")
		String fileName,
		@NotBlank(message = "contentType is required")
		@Size(max = 120, message = "contentType must be <= 120 chars")
		String contentType,
		@NotBlank(message = "base64Content is required")
		String base64Content
) {
}
