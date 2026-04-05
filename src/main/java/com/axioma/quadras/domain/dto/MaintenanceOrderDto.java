package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePaymentMethod;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public record MaintenanceOrderDto(
		Long id,
		Long locationId,
		MaintenanceLocationType locationTypeSnapshot,
		String locationCodeSnapshot,
		String locationLabelSnapshot,
		Long providerId,
		MaintenanceProviderType providerTypeSnapshot,
		String providerNameSnapshot,
		String serviceLabelSnapshot,
		String title,
		String description,
		MaintenancePriority priority,
		MaintenanceRequestOrigin requestOrigin,
		Boolean requestedForGuest,
		String guestName,
		String guestReference,
		String requestedByUsername,
		String requestedByRole,
		MaintenanceBusinessPriority businessPriority,
		Integer estimatedExecutionMinutes,
		String assignedUsername,
		OffsetDateTime assignedAt,
		MaintenanceOrderStatus status,
		OffsetDateTime reportedAt,
		LocalDateTime scheduledStartAt,
		LocalDateTime scheduledEndAt,
		OffsetDateTime startedAt,
		OffsetDateTime completedAt,
		boolean paid,
		MaintenancePaymentMethod paymentMethod,
		LocalDate paymentDate,
		String paymentNotes,
		String resolutionNotes,
		String cancellationNotes,
		List<MaintenanceOrderAttachmentDto> attachments,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime cancelledAt,
		String createdBy,
		String updatedBy,
		String cancelledBy
) {
	public static MaintenanceOrderDto from(MaintenanceOrder order) {
		return new MaintenanceOrderDto(
				order.getId(),
				order.getLocation().getId(),
				order.getLocationTypeSnapshot(),
				order.getLocationCodeSnapshot(),
				order.getLocationLabelSnapshot(),
				order.getProvider() == null ? null : order.getProvider().getId(),
				order.getProviderTypeSnapshot(),
				order.getProviderNameSnapshot(),
				order.getServiceLabelSnapshot(),
				order.getTitle(),
				order.getDescription(),
				order.getPriority(),
				order.getRequestOrigin(),
				order.isRequestedForGuest(),
				order.getGuestName(),
				order.getGuestReference(),
				order.getRequestedByUsername(),
				order.getRequestedByRole(),
				order.getBusinessPriority(),
				order.getEstimatedExecutionMinutes(),
				order.getAssignedUsername(),
				order.getAssignedAt(),
				order.getStatus(),
				order.getReportedAt(),
				order.getScheduledStartAt(),
				order.getScheduledEndAt(),
				order.getStartedAt(),
				order.getCompletedAt(),
				order.isPaid(),
				order.getPaymentMethod(),
				order.getPaymentDate(),
				order.getPaymentNotes(),
				order.getResolutionNotes(),
				order.getCancellationNotes(),
				order.getAttachments().stream().map(MaintenanceOrderAttachmentDto::from).toList(),
				order.getCreatedAt(),
				order.getUpdatedAt(),
				order.getCancelledAt(),
				order.getCreatedBy(),
				order.getUpdatedBy(),
				order.getCancelledBy()
		);
	}
}
