package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.LoadMaintenanceSimulationDto;
import com.axioma.quadras.domain.dto.MaintenanceSimulationResultDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MaintenanceSimulationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maintenance/simulation")
public class MaintenanceSimulationController {

	private final MaintenanceSimulationService maintenanceSimulationService;

	public MaintenanceSimulationController(MaintenanceSimulationService maintenanceSimulationService) {
		this.maintenanceSimulationService = maintenanceSimulationService;
	}

	@PostMapping("/load")
	public ResponseEntity<MaintenanceSimulationResultDto> load(
			@Valid @RequestBody(required = false) LoadMaintenanceSimulationDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceSimulationService.load(input, principal.getUsername())
		);
	}
}
