package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.PushSubscriptionDto;
import com.axioma.quadras.domain.dto.RegisterPushSubscriptionDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.PushSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/push/subscriptions")
public class PushSubscriptionController {

	private final PushSubscriptionService subscriptionService;

	public PushSubscriptionController(PushSubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@PostMapping
	public ResponseEntity<PushSubscriptionDto> register(
			@Valid @RequestBody RegisterPushSubscriptionDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(subscriptionService.register(principal.getUsername(), input));
	}

	@PatchMapping("/{installationId}/heartbeat")
	public ResponseEntity<Void> heartbeat(
			@PathVariable String installationId,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		subscriptionService.heartbeat(principal.getUsername(), installationId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{installationId}")
	public ResponseEntity<Void> unregister(
			@PathVariable String installationId,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		subscriptionService.unregister(principal.getUsername(), installationId);
		return ResponseEntity.noContent().build();
	}
}
