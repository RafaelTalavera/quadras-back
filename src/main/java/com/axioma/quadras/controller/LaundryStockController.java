package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateLaundryOpeningStockDto;
import com.axioma.quadras.domain.dto.CreateLaundryDispatchDto;
import com.axioma.quadras.domain.dto.CreateLaundryStockAdjustmentDto;
import com.axioma.quadras.domain.dto.LaundryOpeningStockDto;
import com.axioma.quadras.domain.dto.LaundryStockAdjustmentDto;
import com.axioma.quadras.domain.dto.LaundryStockDashboardDto;
import com.axioma.quadras.domain.dto.LaundryStockPageDto;
import com.axioma.quadras.domain.dto.LaundryDocumentDto;
import com.axioma.quadras.domain.dto.SubmitLaundryDocumentDto;
import com.axioma.quadras.domain.dto.CreateLaundryReturnDto;
import com.axioma.quadras.domain.dto.ConfirmLaundryReturnDto;
import com.axioma.quadras.domain.dto.LaundryReturnDto;
import com.axioma.quadras.domain.dto.LaundryBillablePageDto;
import com.axioma.quadras.domain.dto.ManualLaundryReturnDto;
import com.axioma.quadras.domain.model.LaundryBillableExportStatus;
import java.time.OffsetDateTime;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.IdempotentResult;
import com.axioma.quadras.service.LaundryStockService;
import com.axioma.quadras.service.LaundryDispatchService;
import com.axioma.quadras.service.LaundryReturnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/laundry")
public class LaundryStockController {

	private final LaundryStockService laundryStockService;
	private final LaundryDispatchService laundryDispatchService;
	private final LaundryReturnService laundryReturnService;

	public LaundryStockController(LaundryStockService laundryStockService,
			LaundryDispatchService laundryDispatchService,
			LaundryReturnService laundryReturnService) {
		this.laundryStockService = laundryStockService;
		this.laundryDispatchService = laundryDispatchService;
		this.laundryReturnService = laundryReturnService;
	}

	@PostMapping("/returns")
	public ResponseEntity<LaundryReturnDto> createReturn(
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody CreateLaundryReturnDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return response(laundryReturnService.create(input, requirePrincipal(principal).getUsername(), key));
	}

	@PostMapping("/returns/manual")
	public ResponseEntity<LaundryReturnDto> createManualReturn(
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody ManualLaundryReturnDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		AuthenticatedUserPrincipal user = requirePrincipal(principal);
		return response(laundryReturnService.createManual(input, user.getUsername(),
				user.getRole().name(), key));
	}

	@PostMapping("/returns/{id}/submit")
	public ResponseEntity<LaundryReturnDto> submitReturn(
			@PathVariable Long id,
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody SubmitLaundryDocumentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return response(laundryReturnService.submit(id, input, requirePrincipal(principal).getUsername(), key));
	}

	@PostMapping("/returns/{id}/confirm")
	public ResponseEntity<LaundryReturnDto> confirmReturn(
			@PathVariable Long id,
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody ConfirmLaundryReturnDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		AuthenticatedUserPrincipal user = requirePrincipal(principal);
		return response(laundryReturnService.confirm(id, input, user.getUsername(), user.getRole().name(), key));
	}

	@GetMapping("/returns/{id}")
	public ResponseEntity<LaundryReturnDto> returnManifest(@PathVariable Long id) {
		return ResponseEntity.ok(laundryReturnService.get(id));
	}

	@GetMapping("/billable-events")
	public ResponseEntity<LaundryBillablePageDto> pendingBillableEvents(
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) OffsetDateTime from,
			@RequestParam(required = false) OffsetDateTime to,
			@RequestParam(required = false) LaundryBillableExportStatus exportStatus,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return ResponseEntity.ok(laundryReturnService.billable(providerId, from, to, exportStatus,
				page, size, requirePrincipal(principal).getRole().name()));
	}

	@GetMapping("/reports/missing")
	public ResponseEntity<LaundryStockPageDto> missingReport(
			@RequestParam(required = false) Long itemId,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return ResponseEntity.ok(laundryStockService.stock(itemId, LaundryStockBucket.MISSING,
				providerId, page, size));
	}

	@GetMapping("/reports/damaged")
	public ResponseEntity<LaundryStockPageDto> damagedReport(
			@RequestParam(required = false) Long itemId,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return ResponseEntity.ok(laundryStockService.stock(itemId, LaundryStockBucket.DAMAGED,
				providerId, page, size));
	}

	@PostMapping("/dispatches")
	public ResponseEntity<LaundryDocumentDto> createDispatch(
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody CreateLaundryDispatchDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return response(laundryDispatchService.create(input, LaundryDocumentType.STANDARD_DISPATCH,
				requirePrincipal(principal).getUsername(), key));
	}

	@PostMapping("/rewash-dispatches")
	public ResponseEntity<LaundryDocumentDto> createRewashDispatch(
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody CreateLaundryDispatchDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return response(laundryDispatchService.create(input, LaundryDocumentType.REWASH_DISPATCH,
				requirePrincipal(principal).getUsername(), key));
	}

	@PostMapping("/documents/{id}/submit")
	public ResponseEntity<LaundryDocumentDto> submitDocument(
			@PathVariable Long id,
			@RequestHeader(value = "Idempotency-Key", required = false) String key,
			@Valid @RequestBody SubmitLaundryDocumentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		AuthenticatedUserPrincipal user = requirePrincipal(principal);
		return response(laundryDispatchService.submit(id, input, user.getUsername(),
				user.getRole().name(), key));
	}

	@GetMapping("/documents/{id}")
	public ResponseEntity<LaundryDocumentDto> document(@PathVariable Long id) {
		return ResponseEntity.ok(laundryDispatchService.get(id));
	}

	@GetMapping("/dashboard")
	public ResponseEntity<LaundryStockDashboardDto> dashboard(
			@RequestParam(required = false) Long providerId
	) {
		return ResponseEntity.ok(laundryStockService.dashboard(providerId));
	}

	@GetMapping("/stock")
	public ResponseEntity<LaundryStockPageDto> stock(
			@RequestParam(required = false) Long itemId,
			@RequestParam(required = false) LaundryStockBucket bucket,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		return ResponseEntity.ok(laundryStockService.stock(itemId, bucket, providerId, page, size));
	}

	@PostMapping("/opening-stock")
	public ResponseEntity<LaundryOpeningStockDto> createOpeningStock(
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody CreateLaundryOpeningStockDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final IdempotentResult<LaundryOpeningStockDto> result = laundryStockService.createOpeningStock(
				input,
				requirePrincipal(principal).getUsername(),
				requirePrincipal(principal).getRole().name(),
				idempotencyKey
		);
		return response(result);
	}

	@PostMapping("/stock-adjustments")
	public ResponseEntity<LaundryStockAdjustmentDto> createAdjustment(
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody CreateLaundryStockAdjustmentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final IdempotentResult<LaundryStockAdjustmentDto> result = laundryStockService.createAdjustment(
				input,
				requirePrincipal(principal).getUsername(),
				requirePrincipal(principal).getRole().name(),
				idempotencyKey
		);
		return response(result);
	}

	private <T> ResponseEntity<T> response(IdempotentResult<T> result) {
		final ResponseEntity.BodyBuilder builder = ResponseEntity.status(result.status());
		if (result.replay()) {
			builder.header("Idempotent-Replay", "true");
		}
		return builder.body(result.body());
	}

	private AuthenticatedUserPrincipal requirePrincipal(AuthenticatedUserPrincipal principal) {
		if (principal == null) {
			throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Authentication is required");
		}
		return principal;
	}
}
