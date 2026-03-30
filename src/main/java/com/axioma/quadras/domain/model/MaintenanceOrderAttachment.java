package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "maintenance_order_attachments")
public class MaintenanceOrderAttachment {

	private static final int MAX_FILE_NAME_LENGTH = 255;
	private static final int MAX_CONTENT_TYPE_LENGTH = 120;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "maintenance_order_id", nullable = false)
	private MaintenanceOrder order;

	@Enumerated(EnumType.STRING)
	@Column(name = "attachment_type", nullable = false, length = 20)
	private MaintenanceAttachmentType attachmentType;

	@Column(name = "file_name", nullable = false, length = MAX_FILE_NAME_LENGTH)
	private String fileName;

	@Column(name = "content_type", nullable = false, length = MAX_CONTENT_TYPE_LENGTH)
	private String contentType;

	@Column(name = "file_size", nullable = false)
	private long fileSize;

	@Lob
	@Column(name = "file_content", nullable = false, columnDefinition = "BLOB")
	private byte[] fileContent;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "created_by", length = MAX_USERNAME_LENGTH, updatable = false)
	private String createdBy;

	protected MaintenanceOrderAttachment() {
	}

	public static MaintenanceOrderAttachment create(
			MaintenanceAttachmentType attachmentType,
			String fileName,
			String contentType,
			byte[] fileContent,
			String actorUsername
	) {
		final MaintenanceOrderAttachment attachment = new MaintenanceOrderAttachment();
		attachment.attachmentType = requireType(attachmentType);
		attachment.fileName = normalize(fileName, "fileName", MAX_FILE_NAME_LENGTH);
		attachment.contentType = normalize(contentType, "contentType", MAX_CONTENT_TYPE_LENGTH);
		attachment.fileContent = requireContent(fileContent);
		attachment.fileSize = fileContent.length;
		attachment.createdBy = normalizeActor(actorUsername, "createdBy");
		return attachment;
	}

	void attachTo(MaintenanceOrder order) {
		this.order = order;
	}

	public Long getId() {
		return id;
	}

	public MaintenanceOrder getOrder() {
		return order;
	}

	public MaintenanceAttachmentType getAttachmentType() {
		return attachmentType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	private static MaintenanceAttachmentType requireType(MaintenanceAttachmentType value) {
		if (value == null) {
			throw new IllegalArgumentException("attachmentType is required");
		}
		return value;
	}

	private static String normalize(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " must be <= " + maxLength + " chars");
		}
		return normalized;
	}

	private static byte[] requireContent(byte[] value) {
		if (value == null || value.length == 0) {
			throw new IllegalArgumentException("fileContent is required");
		}
		return value;
	}

	private static String normalizeActor(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		final String normalized = value.trim().toLowerCase();
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException(fieldName + " must be <= " + MAX_USERNAME_LENGTH + " chars");
		}
		return normalized;
	}
}
