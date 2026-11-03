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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "laundry_document_confirmations")
public class LaundryDocumentConfirmation {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "document_id", nullable = false)
	private LaundryDocument document;
	@Enumerated(EnumType.STRING) @Column(name = "action_type", nullable = false, length = 30)
	private LaundryDocumentConfirmationAction actionType;
	@Column(length = 500) private String notes;
	@Column(name = "actor_username", nullable = false, length = 120) private String actorUsername;
	@Column(name = "actor_role", nullable = false, length = 40) private String actorRole;
	@Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

	protected LaundryDocumentConfirmation() { }

	public static LaundryDocumentConfirmation create(LaundryDocument document,
			LaundryDocumentConfirmationAction actionType, String notes,
			String actorUsername, String actorRole) {
		LaundryDocumentConfirmation value = new LaundryDocumentConfirmation();
		value.document = document;
		value.actionType = actionType;
		value.notes = notes == null || notes.isBlank() ? null : notes.trim();
		value.actorUsername = actorUsername;
		value.actorRole = actorRole;
		value.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		return value;
	}

	public Long getId() { return id; }
	public LaundryDocumentConfirmationAction getActionType() { return actionType; }
	public String getNotes() { return notes; }
	public String getActorRole() { return actorRole; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
}
