package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.AddMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.CompleteMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.ProviderMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.ProviderMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.StartMaintenanceOrderDto;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.service.ProviderMaintenanceOrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/provider/maintenance/orders")
public class ProviderMaintenanceOrderController {

	private final ProviderMaintenanceOrderService providerMaintenanceOrderService;

	public ProviderMaintenanceOrderController(ProviderMaintenanceOrderService providerMaintenanceOrderService) {
		this.providerMaintenanceOrderService = providerMaintenanceOrderService;
	}

	@GetMapping
	public ResponseEntity<List<ProviderMaintenanceOrderDto>> list(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(required = false) MaintenanceOrderStatus status) {
		return ResponseEntity.ok(providerMaintenanceOrderService.list(dateFrom, dateTo, status));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<ProviderMaintenanceOrderDto> get(@PathVariable Long orderId) {
		return ResponseEntity.ok(providerMaintenanceOrderService.get(orderId));
	}

	@PatchMapping("/{orderId}/start")
	public ResponseEntity<ProviderMaintenanceOrderDto> start(@PathVariable Long orderId,
			@RequestBody(required = false) StartMaintenanceOrderDto input) {
		return ResponseEntity.ok(providerMaintenanceOrderService.start(orderId, input));
	}

	@PatchMapping("/{orderId}/complete")
	public ResponseEntity<ProviderMaintenanceOrderDto> complete(@PathVariable Long orderId,
			@Valid @RequestBody CompleteMaintenanceOrderDto input) {
		return ResponseEntity.ok(providerMaintenanceOrderService.complete(orderId, input));
	}

	@GetMapping("/{orderId}/attachments")
	public ResponseEntity<List<ProviderMaintenanceAttachmentDto>> listAttachments(@PathVariable Long orderId) {
		return ResponseEntity.ok(providerMaintenanceOrderService.listAttachments(orderId));
	}

	@PostMapping("/{orderId}/attachments")
	public ResponseEntity<ProviderMaintenanceAttachmentDto> addAttachment(@PathVariable Long orderId,
			@Valid @RequestBody AddMaintenanceAttachmentDto input) {
		return ResponseEntity.ok(providerMaintenanceOrderService.addAttachment(orderId, input));
	}
}
