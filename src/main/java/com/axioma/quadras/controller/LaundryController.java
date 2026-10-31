package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.*;
import com.axioma.quadras.domain.model.LaundryShipmentStatus;
import com.axioma.quadras.domain.model.LaundryShipmentKind;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.LaundryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/laundry")
public class LaundryController {
	private final LaundryService laundryService;
	public LaundryController(LaundryService laundryService) { this.laundryService = laundryService; }

	@GetMapping("/providers")
	public ResponseEntity<List<LaundryProviderDto>> providers() { return ResponseEntity.ok(laundryService.providers()); }
	@PostMapping("/providers")
	public ResponseEntity<LaundryProviderDto> createProvider(@Valid @RequestBody CreateLaundryProviderDto input) {
		return ResponseEntity.status(201).body(laundryService.createProvider(input));
	}
	@GetMapping("/items")
	public ResponseEntity<List<LaundryItemDto>> items() { return ResponseEntity.ok(laundryService.items()); }
	@PostMapping("/items")
	public ResponseEntity<LaundryItemDto> createItem(@Valid @RequestBody CreateLaundryItemDto input) {
		return ResponseEntity.status(201).body(laundryService.createItem(input));
	}
	@PutMapping("/items/{id}")
	public ResponseEntity<LaundryItemDto> updateItem(
			@PathVariable Long id, @Valid @RequestBody UpdateLaundryItemDto input
	) { return ResponseEntity.ok(laundryService.updateItem(id, input)); }
	@PostMapping("/shipments")
	public ResponseEntity<LaundryShipmentDto> createShipment(
			@Valid @RequestBody CreateLaundryShipmentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		LaundryShipmentDto created = laundryService.createShipment(input, principal.getUsername());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
		return ResponseEntity.created(location).body(created);
	}
	@GetMapping("/shipments")
	public ResponseEntity<List<LaundryShipmentDto>> shipments(
			@RequestParam(required = false) LaundryShipmentStatus status,
			@RequestParam(required = false) LaundryShipmentKind kind,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
	) { return ResponseEntity.ok(laundryService.shipments(status, kind, providerId, dateFrom, dateTo)); }
	@GetMapping("/summary")
	public ResponseEntity<LaundrySummaryDto> summary(
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
	) { return ResponseEntity.ok(laundryService.summary(providerId, dateFrom, dateTo)); }
	@GetMapping("/shipments/{id}")
	public ResponseEntity<LaundryShipmentDto> shipment(@PathVariable Long id) { return ResponseEntity.ok(laundryService.shipment(id)); }
	@PostMapping("/shipments/{id}/receive")
	public ResponseEntity<LaundryReceiptResultDto> receive(
			@PathVariable Long id, @Valid @RequestBody ReceiveLaundryShipmentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) { return ResponseEntity.ok(laundryService.receive(id, input, principal.getUsername())); }
	@PostMapping("/shipments/{id}/dispatch")
	public ResponseEntity<LaundryShipmentDto> dispatch(
			@PathVariable Long id, @Valid @RequestBody DispatchLaundryShipmentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) { return ResponseEntity.ok(laundryService.dispatch(id, input, principal.getUsername())); }
	@PatchMapping("/shipments/{id}/cancel")
	public ResponseEntity<LaundryShipmentDto> cancel(
			@PathVariable Long id, @Valid @RequestBody CancelLaundryShipmentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) { return ResponseEntity.ok(laundryService.cancel(id, input, principal.getUsername())); }
	@GetMapping("/shipments/{id}/audit")
	public ResponseEntity<List<AuditEventDto>> audit(@PathVariable Long id) { return ResponseEntity.ok(laundryService.audit(id)); }
}
