package com.axioma.quadras.controller;

import com.axioma.quadras.domain.model.SystemStatus;
import com.axioma.quadras.service.SystemStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

	private final SystemStatusService systemStatusService;

	public SystemStatusController(SystemStatusService systemStatusService) {
		this.systemStatusService = systemStatusService;
	}

	@GetMapping("/health")
	public ResponseEntity<SystemStatus> health() {
		return ResponseEntity.ok(systemStatusService.getCurrentStatus());
	}
}
