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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "maintenance_orders")
public class MaintenanceOrder {

	private static final int MAX_LOCATION_CODE_LENGTH = 60;
	private static final int MAX_LOCATION_LABEL_LENGTH = 160;
	private static final int MAX_PROVIDER_NAME_LENGTH = 120;
	private static final int MAX_SERVICE_LABEL_LENGTH = 120;
	private static final int MAX_TITLE_LENGTH = 160;
	private static final int MAX_DESCRIPTION_LENGTH = 1500;
	private static final int MAX_PAYMENT_NOTES_LENGTH = 500;
	private static final int MAX_RESOLUTION_NOTES_LENGTH = 1500;
	private static final int MAX_CANCELLATION_NOTES_LENGTH = 500;
	private static final int MAX_USERNAME_LENGTH = 120;
	private static final int MAX_ROLE_LENGTH = 60;
	private static final int MAX_GUEST_NAME_LENGTH = 160;
	private static final int MAX_GUEST_REFERENCE_LENGTH = 80;
	private static final int MAX_ESTIMATED_EXECUTION_MINUTES = 10080;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private MaintenanceProvider provider;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type_snapshot", length = 20)
	private MaintenanceProviderType providerTypeSnapshot;

	@Column(name = "provider_name_snapshot", length = MAX_PROVIDER_NAME_LENGTH)
	private String providerNameSnapshot;

	@Column(name = "service_label_snapshot", length = MAX_SERVICE_LABEL_LENGTH)
	private String serviceLabelSnapshot;

	@Column(name = "title", nullable = false, length = MAX_TITLE_LENGTH)
	private String title;

	@Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 20)
	private MaintenancePriority priority;

	@Enumerated(EnumType.STRING)
	@Column(name = "request_origin", nullable = false, length = 30)
	private MaintenanceRequestOrigin requestOrigin;

	@Column(name = "requested_for_guest", nullable = false)
	private boolean requestedForGuest;

	@Column(name = "guest_name", length = MAX_GUEST_NAME_LENGTH)
	private String guestName;

	@Column(name = "guest_reference", length = MAX_GUEST_REFERENCE_LENGTH)
	private String guestReference;

	@Column(name = "requested_by_username", nullable = false, length = MAX_USERNAME_LENGTH)
	private String requestedByUsername;

	@Column(name = "requested_by_role", length = MAX_ROLE_LENGTH)
	private String requestedByRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "business_priority", nullable = false, length = 30)
	private MaintenanceBusinessPriority businessPriority;

	@Column(name = "estimated_execution_minutes")
	private Integer estimatedExecutionMinutes;

	@Column(name = "assigned_username", length = MAX_USERNAME_LENGTH)
	private String assignedUsername;

	@Column(name = "assigned_at")
	private OffsetDateTime assignedAt;

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

	@Column(name = "paid", nullable = false)
	private boolean paid;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 30)
	private MaintenancePaymentMethod paymentMethod;

	@Column(name = "payment_date")
	private LocalDate paymentDate;

	@Column(name = "payment_notes", length = MAX_PAYMENT_NOTES_LENGTH)
	private String paymentNotes;

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
			MaintenanceRequestOrigin requestOrigin,
			boolean requestedForGuest,
			String guestName,
			String guestReference,
			MaintenanceBusinessPriority businessPriority,
			Integer estimatedExecutionMinutes,
			String assignedUsername,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt,
			String actorUsername,
			String actorRole
	) {
		final MaintenanceOrder order = new MaintenanceOrder();
		order.createdBy = normalizeActor(actorUsername, "createdBy");
		order.updatedBy = order.createdBy;
		order.reportedAt = OffsetDateTime.now(ZoneOffset.UTC);
		order.requestedByUsername = order.createdBy;
		order.requestedByRole = normalizeOptional(actorRole, "requestedByRole", MAX_ROLE_LENGTH);
		order.applyCoreState(
				location,
				provider,
				title,
				description,
				priority,
				requestOrigin,
				requestedForGuest,
				guestName,
				guestReference,
				businessPriority,
				estimatedExecutionMinutes,
				assignedUsername,
				scheduledStartAt,
				scheduledEndAt
		);
		if (order.hasAssignee()) {
			order.assignedAt = OffsetDateTime.now(ZoneOffset.UTC);
		}
		order.applyPaymentStatus(false, null, null, null);
		return order;
	}

	public void update(
			MaintenanceLocation location,
			MaintenanceProvider provider,
			String title,
			String description,
			MaintenancePriority priority,
			MaintenanceRequestOrigin requestOrigin,
			boolean requestedForGuest,
			String guestName,
			String guestReference,
			MaintenanceBusinessPriority businessPriority,
			Integer estimatedExecutionMinutes,
			String assignedUsername,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt,
			String actorUsername
	) {
		if (status == MaintenanceOrderStatus.IN_PROGRESS
				|| status == MaintenanceOrderStatus.COMPLETED
				|| status == MaintenanceOrderStatus.CANCELLED) {
			throw new IllegalStateException("Only open, assigned or scheduled maintenance orders can be edited.");
		}
		final Long previousProviderId = this.provider == null ? null : this.provider.getId();
		final String previousAssignedUsername = this.assignedUsername;
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyCoreState(
				location,
				provider,
				title,
				description,
				priority,
				requestOrigin,
				requestedForGuest,
				guestName,
				guestReference,
				businessPriority,
				estimatedExecutionMinutes,
				assignedUsername,
				scheduledStartAt,
				scheduledEndAt
		);
		updateAssignmentAudit(previousProviderId, previousAssignedUsername);
	}

	public void start(OffsetDateTime startedAt, String actorUsername) {
		if (status == MaintenanceOrderStatus.CANCELLED || status == MaintenanceOrderStatus.COMPLETED) {
			throw new IllegalStateException("Completed or cancelled maintenance orders cannot be started.");
		}
		this.status = MaintenanceOrderStatus.IN_PROGRESS;
		this.startedAt = startedAt == null ? OffsetDateTime.now(ZoneOffset.UTC) : startedAt;
		if (this.assignedUsername == null) {
			this.assignedUsername = normalizeActor(actorUsername, "assignedUsername");
		}
		if (this.assignedAt == null) {
			this.assignedAt = OffsetDateTime.now(ZoneOffset.UTC);
		}
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
		if (this.assignedUsername == null) {
			this.assignedUsername = normalizeActor(actorUsername, "assignedUsername");
		}
		if (this.assignedAt == null) {
			this.assignedAt = OffsetDateTime.now(ZoneOffset.UTC);
		}
		this.resolutionNotes = normalize(resolutionNotes, "resolutionNotes", MAX_RESOLUTION_NOTES_LENGTH);
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
	}

	public void markPayment(
			MaintenancePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes,
			String actorUsername
	) {
		if (status == MaintenanceOrderStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled maintenance orders cannot be marked as paid.");
		}
		this.updatedBy = normalizeActor(actorUsername, "updatedBy");
		applyPaymentStatus(true, paymentMethod, paymentDate, paymentNotes);
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

	public MaintenanceRequestOrigin getRequestOrigin() {
		return requestOrigin;
	}

	public boolean isRequestedForGuest() {
		return requestedForGuest;
	}

	public String getGuestName() {
		return guestName;
	}

	public String getGuestReference() {
		return guestReference;
	}

	public String getRequestedByUsername() {
		return requestedByUsername;
	}

	public String getRequestedByRole() {
		return requestedByRole;
	}

	public MaintenanceBusinessPriority getBusinessPriority() {
		return businessPriority;
	}

	public Integer getEstimatedExecutionMinutes() {
		return estimatedExecutionMinutes;
	}

	public String getAssignedUsername() {
		return assignedUsername;
	}

	public OffsetDateTime getAssignedAt() {
		return assignedAt;
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

	public boolean isPaid() {
		return paid;
	}

	public MaintenancePaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public String getPaymentNotes() {
		return paymentNotes;
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
			MaintenanceRequestOrigin requestOrigin,
			boolean requestedForGuest,
			String guestName,
			String guestReference,
			MaintenanceBusinessPriority businessPriority,
			Integer estimatedExecutionMinutes,
			String assignedUsername,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt
	) {
		this.location = requireLocation(location);
		this.provider = provider;
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
		if (provider == null) {
			this.providerTypeSnapshot = null;
			this.providerNameSnapshot = null;
			this.serviceLabelSnapshot = null;
		} else {
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
		}
		this.title = normalize(title, "title", MAX_TITLE_LENGTH);
		this.description = normalizeOptional(description, "description", MAX_DESCRIPTION_LENGTH);
		if (priority == null) {
			throw new IllegalArgumentException("priority is required");
		}
		if (requestOrigin == null) {
			throw new IllegalArgumentException("requestOrigin is required");
		}
		if (businessPriority == null) {
			throw new IllegalArgumentException("businessPriority is required");
		}
		this.priority = priority;
		this.requestOrigin = requestOrigin;
		this.businessPriority = businessPriority;
		applyGuestContext(requestOrigin, requestedForGuest, guestName, guestReference);
		this.assignedUsername = normalizeOptionalActor(assignedUsername, "assignedUsername");
		applySchedule(scheduledStartAt, scheduledEndAt);
		this.estimatedExecutionMinutes = normalizeEstimatedMinutes(
				estimatedExecutionMinutes,
				scheduledStartAt,
				scheduledEndAt
		);
		if (this.status == null
				|| this.status == MaintenanceOrderStatus.OPEN
				|| this.status == MaintenanceOrderStatus.ASSIGNED
				|| this.status == MaintenanceOrderStatus.SCHEDULED) {
			this.status = deriveOperationalStatus();
		}
	}

	private void applyGuestContext(
			MaintenanceRequestOrigin requestOrigin,
			boolean requestedForGuest,
			String guestName,
			String guestReference
	) {
		final boolean guestContext = requestOrigin == MaintenanceRequestOrigin.GUEST_REQUEST || requestedForGuest;
		this.requestedForGuest = guestContext;
		if (!guestContext) {
			this.guestName = null;
			this.guestReference = null;
			return;
		}
		this.guestName = normalizeOptional(guestName, "guestName", MAX_GUEST_NAME_LENGTH);
		this.guestReference = normalizeOptional(
				guestReference,
				"guestReference",
				MAX_GUEST_REFERENCE_LENGTH
		);
		if (this.guestName == null && this.guestReference == null) {
			throw new IllegalArgumentException(
					"guestName or guestReference is required for guest maintenance requests"
			);
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

	private Integer normalizeEstimatedMinutes(
			Integer estimatedExecutionMinutes,
			LocalDateTime scheduledStartAt,
			LocalDateTime scheduledEndAt
	) {
		if (scheduledStartAt != null && scheduledEndAt != null) {
			return normalizePositiveMinutes((int) Duration.between(scheduledStartAt, scheduledEndAt).toMinutes());
		}
		if (estimatedExecutionMinutes == null) {
			return null;
		}
		return normalizePositiveMinutes(estimatedExecutionMinutes);
	}

	private Integer normalizePositiveMinutes(Integer value) {
		if (value == null) {
			return null;
		}
		if (value <= 0) {
			throw new IllegalArgumentException("estimatedExecutionMinutes must be positive");
		}
		if (value > MAX_ESTIMATED_EXECUTION_MINUTES) {
			throw new IllegalArgumentException(
					"estimatedExecutionMinutes must be <= " + MAX_ESTIMATED_EXECUTION_MINUTES
			);
		}
		return value;
	}

	private void updateAssignmentAudit(Long previousProviderId, String previousAssignedUsername) {
		if (!hasAssignee()) {
			this.assignedAt = null;
			return;
		}
		final Long currentProviderId = provider == null ? null : provider.getId();
		final boolean providerChanged = !Objects.equals(previousProviderId, currentProviderId);
		final boolean userChanged = !Objects.equals(previousAssignedUsername, assignedUsername);
		if (assignedAt == null || providerChanged || userChanged) {
			this.assignedAt = OffsetDateTime.now(ZoneOffset.UTC);
		}
	}

	private MaintenanceOrderStatus deriveOperationalStatus() {
		if (scheduledStartAt != null) {
			return MaintenanceOrderStatus.SCHEDULED;
		}
		if (hasAssignee()) {
			return MaintenanceOrderStatus.ASSIGNED;
		}
		return MaintenanceOrderStatus.OPEN;
	}

	private boolean hasAssignee() {
		return provider != null || assignedUsername != null;
	}

	private static MaintenanceLocation requireLocation(MaintenanceLocation value) {
		if (value == null) {
			throw new IllegalArgumentException("location is required");
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

	private static String normalizeOptionalActor(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return normalizeActor(value, fieldName);
	}

	private void applyPaymentStatus(
			boolean paid,
			MaintenancePaymentMethod paymentMethod,
			LocalDate paymentDate,
			String paymentNotes
	) {
		if (!paid) {
			if (paymentMethod != null || paymentDate != null || (paymentNotes != null && !paymentNotes.isBlank())) {
				throw new IllegalArgumentException(
						"paymentMethod, paymentDate and paymentNotes require paid=true"
				);
			}
			this.paid = false;
			this.paymentMethod = null;
			this.paymentDate = null;
			this.paymentNotes = null;
			return;
		}

		if (paymentMethod == null) {
			throw new IllegalArgumentException("paymentMethod is required when paid is true");
		}
		if (paymentDate == null) {
			throw new IllegalArgumentException("paymentDate is required when paid is true");
		}

		this.paid = true;
		this.paymentMethod = paymentMethod;
		this.paymentDate = paymentDate;
		this.paymentNotes = normalizeOptional(
				paymentNotes,
				"paymentNotes",
				MAX_PAYMENT_NOTES_LENGTH
		);
	}
}
