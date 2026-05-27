package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateMaintenancePlanDto;
import com.axioma.quadras.domain.dto.GenerateMaintenancePlanOrderDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.MaintenancePlanDto;
import com.axioma.quadras.domain.dto.UpdateMaintenancePlanDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MaintenancePlanService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/maintenance/plans")
public class MaintenancePlanController {

	private final MaintenancePlanService maintenancePlanService;

	public MaintenancePlanController(MaintenancePlanService maintenancePlanService) {
		this.maintenancePlanService = maintenancePlanService;
	}

	@GetMapping
	public ResponseEntity<List<MaintenancePlanDto>> list() {
		return ResponseEntity.ok(maintenancePlanService.list());
	}

	@PostMapping
	public ResponseEntity<MaintenancePlanDto> create(
			@Valid @RequestBody CreateMaintenancePlanDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MaintenancePlanDto created = maintenancePlanService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{planId}")
	public ResponseEntity<MaintenancePlanDto> update(
			@PathVariable Long planId,
			@Valid @RequestBody UpdateMaintenancePlanDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(maintenancePlanService.update(planId, input, principal.getUsername()));
	}

	@PostMapping("/{planId}/generate-order")
	public ResponseEntity<MaintenanceOrderDto> generateOrder(
			@PathVariable Long planId,
			@RequestBody(required = false) GenerateMaintenancePlanOrderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenancePlanService.generateOrderFromPlan(planId, input, principal.getUsername(), principal.getRole().name())
		);
	}
}
