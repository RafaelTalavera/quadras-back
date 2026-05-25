package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CreateMassageProviderDto;
import com.axioma.quadras.domain.dto.CreateMassageTherapistDto;
import com.axioma.quadras.domain.dto.MassageProviderDto;
import com.axioma.quadras.domain.dto.MassageTherapistDto;
import com.axioma.quadras.domain.dto.UpdateMassageProviderDto;
import com.axioma.quadras.domain.dto.UpdateMassageTherapistDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MassageProviderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/massages/providers")
public class MassageProviderController {

	private final MassageProviderService massageProviderService;

	public MassageProviderController(MassageProviderService massageProviderService) {
		this.massageProviderService = massageProviderService;
	}

	@PostMapping
	public ResponseEntity<MassageProviderDto> create(
			@Valid @RequestBody CreateMassageProviderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MassageProviderDto created = massageProviderService.create(input);
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{providerId}")
	public ResponseEntity<MassageProviderDto> update(
			@PathVariable Long providerId,
			@Valid @RequestBody UpdateMassageProviderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(massageProviderService.update(providerId, input));
	}

	@GetMapping
	public ResponseEntity<List<MassageProviderDto>> list(
			@RequestParam(defaultValue = "false") boolean activeOnly
	) {
		return ResponseEntity.ok(massageProviderService.list(activeOnly));
	}

	@PostMapping("/{providerId}/therapists")
	public ResponseEntity<MassageTherapistDto> createTherapist(
			@PathVariable Long providerId,
			@Valid @RequestBody CreateMassageTherapistDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MassageTherapistDto created = massageProviderService.createTherapist(
				providerId,
				input
		);
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{therapistId}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{providerId}/therapists/{therapistId}")
	public ResponseEntity<MassageTherapistDto> updateTherapist(
			@PathVariable Long providerId,
			@PathVariable Long therapistId,
			@Valid @RequestBody UpdateMassageTherapistDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				massageProviderService.updateTherapist(providerId, therapistId, input)
		);
	}

	@GetMapping("/{providerId}/audit")
	public ResponseEntity<List<AuditEventDto>> providerAudit(@PathVariable Long providerId) {
		return ResponseEntity.ok(massageProviderService.providerAudit(providerId));
	}

	@GetMapping("/{providerId}/therapists/{therapistId}/audit")
	public ResponseEntity<List<AuditEventDto>> therapistAudit(
			@PathVariable Long providerId,
			@PathVariable Long therapistId
	) {
		return ResponseEntity.ok(massageProviderService.therapistAudit(providerId, therapistId));
	}
}
