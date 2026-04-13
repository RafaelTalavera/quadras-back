package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceAttachmentType;
import java.time.OffsetDateTime;

public interface MaintenanceOrderAttachmentMetadataView {

	Long getOrderId();

	Long getId();

	MaintenanceAttachmentType getAttachmentType();

	String getFileName();

	String getContentType();

	long getFileSize();

	OffsetDateTime getCreatedAt();

	String getCreatedBy();
}
