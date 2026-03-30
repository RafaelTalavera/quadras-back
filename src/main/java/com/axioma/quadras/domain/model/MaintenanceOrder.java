package com.axioma.quadras.domain.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "maintenance_orders")
public class MaintenanceOrder {

	private static final int MAX_LOCATION_CODE_LENGTH = 60;
	private static final int MAX_LOCATION_LABEL_LENGTH = 160;
	private static final int MAX_PROVIDER_NAME_LENGTH = 120;
	private static final int MAX_SERVICE_LABEL_LENGTH = 120;
	private static final int MAX_TITLE_LENGTH = 160;
	private static final int MAX_DESCRIPTION_LENGTH = 1500;
	private static final int MAX_RESOLUTION_NOTES_LENGTH = 1500;
	private static final int MAX_CANCELLATION_NOTES_LENGTH = 500;
	private static final int MAX_USERNAME_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "location_id", nullable = false)
	private MaintenanceLocation location;

	@Enumerated(EnumType.STRING)
	@Column(name = "location_type_snapshot", nullable = false, length = 20)
	private MaintenanceLocationType locationTypeSnapshot;

	@Column(name = "location_code_snapshot", nullable = false, length = MAX_LOCATION_CODE_LENGTH)
	private String locationCodeSnapshot;

	@Column(name = "location_label_snapshot", nullable = false, length = MAX_LOCATION_LABEL_LENGTH)
	private String locationLabelSnapshot;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private MaintenanceProvider provider;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type_snapshot", nullable = false, length = 20)
	private MaintenanceProviderType providerTypeSnapshot;

	@Column(name = "provider_name_snapshot", nullable = false, length = MAX_PROVIDER_NAME_LENGTH)
	private String providerNameSnapshot;

	@Column(name = "service_label_snapshot", nullable = false, length = MAX_SERVICE_LABEL_LENGTH)
	private String serviceLabelSnapshot;

	@Column(name = "title", nullable = false, length = MAX_TITLE_LENGTH)
	private String title;

	@Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 20)
	private MaintenancePriority priority;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private MaintenanceOrderStatus status;

	@Column(name = "reported_at", nullable = false)
	private OffsetDateTime reportedAt;

	@Column(name = "scheduled_start_at")
	private LocalDateTime scheduledStartAt;

	@Column(name = "scheduled_end_at")
	private LocalDateTime scheduledEndAt;

	@Column(name = "started_at")
	private OffsetDateTime startedAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	@Column(name = "resolution_notes", length = MAX_RESOLUTION_NOTES_LENGTH)
	private String resolutionNotes;

	@Column(name = "cancellation_notes", length = MAX_CANCELLATION_NOTES_LENGTH)
	private String cancellationNotes;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private final List<MaintenanceOrderAttachment> attachments = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "cancelled_at")
	private OffsetDateTime cancelledAt;

	@Column(name = "created_by", length = MAX_USERNAME_LENGTH, updatable = false)
	private String createdBy;

	@Column(name = "updated_by", length = MAX_USERNAME_LENGTH)
	private String updatedBy;

	@Column(name = "cancelled_by", length = MAX_USERNAME_LENGTH)
	private String cancelledBy;

	protected MaintenanceOrder() {
	}

	public static MaintenanceOrder report(
			MaintenanceLocation location,
			MaintenanceProvider provider,
			String title,
			String description,
			MaintenancePriority priority,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt,
			String actorUsername
	) {
		final MaintenanceOrder order = new MaintenanceOrder();
		order.createdBy = normalizeActor(actorUsername, "createdBy");
		order.updatedBy = order.createdBy;
		order.reportedAt = OffsetDateTime.now(ZoneOffset.UTC);
		order.applyCoreState(location, provider, title, description, priority, scheduledStartAt, scheduledEndAt);
		return order;
	}

	public void update(
			MaintenanceLocation location,
			MaintenanceProvider provider,
			String title,
			String description,
			MaintenancePriority priority,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt,
			String actorUsername
	) {
		if (status == MaintenanceOrderStatus.IN_PROGRESS
				|| status == MaintenanceOrderStatus.COMPLETED
				|| status == MaintenanceOrderStatus.CANCELLED) {
			throw new IllegalStateException("Only open or scheduled maintenance orders can be edited.");
		}
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyCoreState(location, provider, title, description, priority, scheduledStartAt, scheduledEndAt);
	}

	public void start(OffsetDateTime startedAt, String actorUsername) {
		if (status == MaintenanceOrderStatus.CANCELLED || status == MaintenanceOrderStatus.COMPLETED) {
			throw new IllegalStateException("Completed or cancelled maintenance orders cannot be started.");
		}
		this.status = MaintenanceOrderStatus.IN_PROGRESS;
		this.startedAt = startedAt == null ? OffsetDateTime.now(ZoneOffset.UTC) : startedAt;
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
	}

	public void complete(OffsetDateTime completedAt, String resolutionNotes, String actorUsername) {
		if (status == MaintenanceOrderStatus.CANCELLED || status == MaintenanceOrderStatus.COMPLETED) {
			throw new IllegalStateException("Completed or cancelled maintenance orders cannot be completed.");
		}
		this.status = MaintenanceOrderStatus.COMPLETED;
		this.completedAt = completedAt == null ? OffsetDateTime.now(ZoneOffset.UTC) : completedAt;
		if (this.startedAt == null) {
			this.startedAt = this.reportedAt;
		}
		this.resolutionNotes = normalize(resolutionNotes, "resolutionNotes", MAX_RESOLUTION_NOTES_LENGTH);
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
	}

	public void cancel(String cancellationNotes, String actorUsername) {
		if (status == MaintenanceOrderStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled maintenance orders cannot be cancelled again.");
		}
		if (status == MaintenanceOrderStatus.COMPLETED) {
			throw new IllegalStateException("Completed maintenance orders cannot be cancelled.");
		}
		final String actor = normalizeActor(actorUsername, "cancelledBy");
		this.status = MaintenanceOrderStatus.CANCELLED;
		this.cancellationNotes = normalize(cancellationNotes, "cancellationNotes", MAX_CANCELLATION_NOTES_LENGTH);
		this.cancelledAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.cancelledBy = actor;
		this.updatedBy = actor;
	}

	public void addAttachment(MaintenanceOrderAttachment attachment) {
		if (attachment == null) {
			throw new IllegalArgumentException("attachment is required");
		}
		attachment.attachTo(this);
		this.attachments.add(attachment);
	}

	public void removeAttachment(MaintenanceOrderAttachment attachment) {
		if (attachment == null) {
			return;
		}
		this.attachments.remove(attachment);
	}

	public Long getId() {
		return id;
	}

	public MaintenanceLocation getLocation() {
		return location;
	}

	public MaintenanceLocationType getLocationTypeSnapshot() {
		return locationTypeSnapshot;
	}

	public String getLocationCodeSnapshot() {
		return locationCodeSnapshot;
	}

	public String getLocationLabelSnapshot() {
		return locationLabelSnapshot;
	}

	public MaintenanceProvider getProvider() {
		return provider;
	}

	public MaintenanceProviderType getProviderTypeSnapshot() {
		return providerTypeSnapshot;
	}

	public String getProviderNameSnapshot() {
		return providerNameSnapshot;
	}

	public String getServiceLabelSnapshot() {
		return serviceLabelSnapshot;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public MaintenancePriority getPriority() {
		return priority;
	}

	public MaintenanceOrderStatus getStatus() {
		return status;
	}

	public OffsetDateTime getReportedAt() {
		return reportedAt;
	}

	public LocalDateTime getScheduledStartAt() {
		return scheduledStartAt;
	}

	public LocalDateTime getScheduledEndAt() {
		return scheduledEndAt;
	}

	public OffsetDateTime getStartedAt() {
		return startedAt;
	}

	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}

	public String getResolutionNotes() {
		return resolutionNotes;
	}

	public String getCancellationNotes() {
		return cancellationNotes;
	}

	public List<MaintenanceOrderAttachment> getAttachments() {
		return attachments.stream()
				.sorted(Comparator.comparing(MaintenanceOrderAttachment::getCreatedAt).reversed())
				.toList();
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public OffsetDateTime getCancelledAt() {
		return cancelledAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public String getCancelledBy() {
		return cancelledBy;
	}

	@PrePersist
	void onCreate() {
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	private void applyCoreState(
			MaintenanceLocation location,
			MaintenanceProvider provider,
			String title,
			String description,
			MaintenancePriority priority,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt
	) {
		this.location = requireLocation(location);
		this.provider = requireProvider(provider);
		this.locationTypeSnapshot = location.getLocationType();
		this.locationCodeSnapshot = normalize(
				location.getCode(),
				"locationCodeSnapshot",
				MAX_LOCATION_CODE_LENGTH
		);
		this.locationLabelSnapshot = normalize(
				location.getLabel(),
				"locationLabelSnapshot",
				MAX_LOCATION_LABEL_LENGTH
		);
		this.providerTypeSnapshot = provider.getProviderType();
		this.providerNameSnapshot = normalize(
				provider.getName(),
				"providerNameSnapshot",
				MAX_PROVIDER_NAME_LENGTH
		);
		this.serviceLabelSnapshot = normalize(
				provider.getServiceLabel(),
				"serviceLabelSnapshot",
				MAX_SERVICE_LABEL_LENGTH
		);
		this.title = normalize(title, "title", MAX_TITLE_LENGTH);
		this.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		if (priority == null) {
			throw new IllegalArgumentException("priority is required");
		}
		this.priority = priority;
		applySchedule(scheduledStartAt, scheduledEndAt);
		if (this.status == null || this.status == MaintenanceOrderStatus.OPEN || this.status == MaintenanceOrderStatus.SCHEDULED) {
			this.status = this.scheduledStartAt == null
					? MaintenanceOrderStatus.OPEN
					: MaintenanceOrderStatus.SCHEDULED;
		}
	}

	private void applySchedule(LocalDateTime scheduledStartAt, LocalDateTime scheduledEndAt) {
		if (scheduledStartAt == null && scheduledEndAt == null) {
			this.scheduledStartAt = null;
			this.scheduledEndAt = null;
			return;
		}
		if (scheduledStartAt == null || scheduledEndAt == null) {
			throw new IllegalArgumentException(
					"scheduledStartAt and scheduledEndAt must both be informed"
			);
		}
		if (!scheduledStartAt.isBefore(scheduledEndAt)) {
			throw new IllegalArgumentException("scheduledStartAt must be before scheduledEndAt");
		}
		this.scheduledStartAt = scheduledStartAt;
		this.scheduledEndAt = scheduledEndAt;
	}

	private static MaintenanceLocation requireLocation(MaintenanceLocation value) {
		if (value == null) {
			throw new IllegalArgumentException("location is required");
		}
		return value;
	}

	private static MaintenanceProvider requireProvider(MaintenanceProvider value) {
		if (value == null) {
			throw new IllegalArgumentException("provider is required");
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

	private static String normalizeOptional(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new IllegalArgumentException(fieldName + " must be <= " + maxLength + " chars");
		}
		return normalized;
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
