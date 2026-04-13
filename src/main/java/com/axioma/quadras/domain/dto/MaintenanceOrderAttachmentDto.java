package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceAttachmentType;
import com.axioma.quadras.domain.model.MaintenanceOrderAttachment;
import com.axioma.quadras.repository.MaintenanceOrderAttachmentMetadataView;
import java.time.OffsetDateTime;

public record MaintenanceOrderAttachmentDto(
		Long id,
		MaintenanceAttachmentType attachmentType,
		String fileName,
		String contentType,
		Long fileSize,
		OffsetDateTime createdAt,
		String createdBy
) {
	public static MaintenanceOrderAttachmentDto from(MaintenanceOrderAttachment attachment) {
		return new MaintenanceOrderAttachmentDto(
				attachment.getId(),
				attachment.getAttachmentType(),
				attachment.getFileName(),
				attachment.getContentType(),
				attachment.getFileSize(),
				attachment.getCreatedAt(),
				attachment.getCreatedBy()
		);
	}

	public static MaintenanceOrderAttachmentDto from(MaintenanceOrderAttachmentMetadataView attachment) {
		return new MaintenanceOrderAttachmentDto(
				attachment.getId(),
				attachment.getAttachmentType(),
				attachment.getFileName(),
				attachment.getContentType(),
				attachment.getFileSize(),
				attachment.getCreatedAt(),
				attachment.getCreatedBy()
		);
	}
}
