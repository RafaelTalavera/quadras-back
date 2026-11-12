package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceAttachmentType;
import java.time.OffsetDateTime;

public record ProviderMaintenanceAttachmentDto(
		Long id,
		MaintenanceAttachmentType attachmentType,
		String fileName,
		String contentType,
		long fileSize,
		OffsetDateTime createdAt
) {
	public static ProviderMaintenanceAttachmentDto from(MaintenanceOrderAttachmentDto attachment) {
		return new ProviderMaintenanceAttachmentDto(
				attachment.id(), attachment.attachmentType(), attachment.fileName(), attachment.contentType(),
				attachment.fileSize(), attachment.createdAt()
		);
	}
}
