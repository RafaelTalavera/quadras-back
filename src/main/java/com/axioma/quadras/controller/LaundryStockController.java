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
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.IdempotentResult;
import com.axioma.quadras.service.LaundryStockService;
import com.axioma.quadras.service.LaundryDispatchService;
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

	public LaundryStockController(LaundryStockService laundryStockService,
			LaundryDispatchService laundryDispatchService) {
		this.laundryStockService = laundryStockService;
		this.laundryDispatchService = laundryDispatchService;
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
