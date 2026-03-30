package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateMaintenanceProviderDto;
import com.axioma.quadras.domain.dto.MaintenanceProviderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceProviderDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MaintenanceProviderService;
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
@RequestMapping("/api/v1/maintenance/providers")
public class MaintenanceProviderController {

	private final MaintenanceProviderService maintenanceProviderService;

	public MaintenanceProviderController(MaintenanceProviderService maintenanceProviderService) {
		this.maintenanceProviderService = maintenanceProviderService;
	}

	@GetMapping
	public ResponseEntity<List<MaintenanceProviderDto>> list() {
		return ResponseEntity.ok(maintenanceProviderService.list());
	}

	@PostMapping
	public ResponseEntity<MaintenanceProviderDto> create(
			@Valid @RequestBody CreateMaintenanceProviderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MaintenanceProviderDto created = maintenanceProviderService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{providerId}")
	public ResponseEntity<MaintenanceProviderDto> update(
			@PathVariable Long providerId,
			@Valid @RequestBody UpdateMaintenanceProviderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				maintenanceProviderService.update(providerId, input, principal.getUsername())
		);
	}
}
