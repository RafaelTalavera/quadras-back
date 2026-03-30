package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateMaintenanceLocationDto;
import com.axioma.quadras.domain.dto.MaintenanceLocationDto;
import com.axioma.quadras.domain.dto.MaintenanceOrderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceLocationDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MaintenanceLocationService;
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
@RequestMapping("/api/v1/maintenance/locations")
public class MaintenanceLocationController {

	private final MaintenanceLocationService maintenanceLocationService;

	public MaintenanceLocationController(MaintenanceLocationService maintenanceLocationService) {
		this.maintenanceLocationService = maintenanceLocationService;
	}

	@GetMapping
	public ResponseEntity<List<MaintenanceLocationDto>> list() {
		return ResponseEntity.ok(maintenanceLocationService.list());
	}

	@PostMapping
	public ResponseEntity<MaintenanceLocationDto> create(
			@Valid @RequestBody CreateMaintenanceLocationDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MaintenanceLocationDto created = maintenanceLocationService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{locationId}")
	public ResponseEntity<MaintenanceLocationDto> update(
			@PathVariable Long locationId,
			@Valid @RequestBody UpdateMaintenanceLocationDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceLocationService.update(locationId, input, principal.getUsername())
		);
	}

	@GetMapping("/{locationId}/history")
	public ResponseEntity<List<MaintenanceOrderDto>> history(@PathVariable Long locationId) {
		return ResponseEntity.ok(maintenanceLocationService.history(locationId));
	}
}
