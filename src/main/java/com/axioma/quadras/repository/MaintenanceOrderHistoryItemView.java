package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePaymentMethod;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface MaintenanceOrderHistoryItemView {
	Long getId();

	Long getLocationId();

	MaintenanceLocationType getLocationTypeSnapshot();

	String getLocationCodeSnapshot();

	String getLocationLabelSnapshot();

	Long getProviderId();

	MaintenanceProviderType getProviderTypeSnapshot();

	String getProviderNameSnapshot();

	String getServiceLabelSnapshot();

	String getTitle();

	String getDescription();

	MaintenancePriority getPriority();

	MaintenanceRequestOrigin getRequestOrigin();

	Boolean getRequestedForGuest();

	String getGuestName();

	String getGuestReference();

	String getRequestedByUsername();

	String getRequestedByRole();

	MaintenanceBusinessPriority getBusinessPriority();

	Integer getEstimatedExecutionMinutes();

	String getAssignedUsername();

	OffsetDateTime getAssignedAt();

	MaintenanceOrderStatus getStatus();

	OffsetDateTime getReportedAt();

	LocalDateTime getScheduledStartAt();

	LocalDateTime getScheduledEndAt();

	OffsetDateTime getStartedAt();

	OffsetDateTime getCompletedAt();

	Boolean getPaid();

	MaintenancePaymentMethod getPaymentMethod();

	LocalDate getPaymentDate();

	String getPaymentNotes();

	String getResolutionNotes();

	String getCancellationNotes();

	OffsetDateTime getCreatedAt();

	OffsetDateTime getUpdatedAt();

	OffsetDateTime getCancelledAt();

	String getCreatedBy();

	String getUpdatedBy();

	String getCancelledBy();
}
