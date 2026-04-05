package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.AddMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.CancelMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.CompleteMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.CreateMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.MaintenanceConflictDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderAttachmentDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.StartMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenancePaymentDto;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MaintenanceOrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/maintenance/orders")
public class MaintenanceOrderController {

	private final MaintenanceOrderService maintenanceOrderService;

	public MaintenanceOrderController(MaintenanceOrderService maintenanceOrderService) {
		this.maintenanceOrderService = maintenanceOrderService;
	}

	@GetMapping
	public ResponseEntity<List<MaintenanceOrderDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo,
			@RequestParam(required = false) Long locationId,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) MaintenanceProviderType providerType,
			@RequestParam(required = false) MaintenanceOrderStatus status,
			@RequestParam(required = false) MaintenancePriority priority
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.list(
						dateFrom,
						dateTo,
						locationId,
						providerId,
						providerType,
						status,
						priority
				)
		);
	}

	@GetMapping("/conflicts")
	public ResponseEntity<List<MaintenanceConflictDto>> conflicts(
			@RequestParam Long locationId,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			LocalDateTime scheduledStartAt,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			LocalDateTime scheduledEndAt,
			@RequestParam(required = false) Long excludeOrderId
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.findConflicts(
						locationId,
						scheduledStartAt,
						scheduledEndAt,
						excludeOrderId
				)
		);
	}

	@PostMapping
	public ResponseEntity<MaintenanceOrderDto> create(
			@Valid @RequestBody CreateMaintenanceOrderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MaintenanceOrderDto created = maintenanceOrderService.create(
				input,
				principal.getUsername(),
				principal.getRole().name()
		);
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{orderId}")
	public ResponseEntity<MaintenanceOrderDto> update(
			@PathVariable Long orderId,
			@Valid @RequestBody UpdateMaintenanceOrderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.update(orderId, input, principal.getUsername())
		);
	}

	@PatchMapping("/{orderId}/start")
	public ResponseEntity<MaintenanceOrderDto> start(
			@PathVariable Long orderId,
			@RequestBody(required = false) StartMaintenanceOrderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.start(orderId, input, principal.getUsername())
		);
	}

	@PatchMapping("/{orderId}/payment")
	public ResponseEntity<MaintenanceOrderDto> updatePayment(
			@PathVariable Long orderId,
			@Valid @RequestBody UpdateMaintenancePaymentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.updatePayment(orderId, input, principal.getUsername())
		);
	}

	@PatchMapping("/{orderId}/complete")
	public ResponseEntity<MaintenanceOrderDto> complete(
			@PathVariable Long orderId,
			@Valid @RequestBody CompleteMaintenanceOrderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.complete(orderId, input, principal.getUsername())
		);
	}

	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<MaintenanceOrderDto> cancel(
			@PathVariable Long orderId,
			@Valid @RequestBody CancelMaintenanceOrderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.cancel(orderId, input, principal.getUsername())
		);
	}

	@GetMapping("/{orderId}/attachments")
	public ResponseEntity<List<MaintenanceOrderAttachmentDto>> listAttachments(@PathVariable Long orderId) {
		return ResponseEntity.ok(maintenanceOrderService.listAttachments(orderId));
	}

	@PostMapping("/{orderId}/attachments")
	public ResponseEntity<MaintenanceOrderAttachmentDto> addAttachment(
			@PathVariable Long orderId,
			@Valid @RequestBody AddMaintenanceAttachmentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceOrderService.addAttachment(orderId, input, principal.getUsername())
		);
	}

	@DeleteMapping("/{orderId}/attachments/{attachmentId}")
	public ResponseEntity<Void> deleteAttachment(
			@PathVariable Long orderId,
			@PathVariable Long attachmentId
	) {
		maintenanceOrderService.deleteAttachment(orderId, attachmentId);
		return ResponseEntity.noContent().build();
	}
}
