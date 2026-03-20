package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateMassageProviderDto;
import com.axioma.quadras.domain.dto.MassageProviderDto;
import com.axioma.quadras.domain.dto.UpdateMassageProviderDto;
import com.axioma.quadras.service.MassageProviderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
			@Valid @RequestBody CreateMassageProviderDto input
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
			@Valid @RequestBody UpdateMassageProviderDto input
	) {
		return ResponseEntity.ok(massageProviderService.update(providerId, input));
	}

	@GetMapping
	public ResponseEntity<List<MassageProviderDto>> list(
			@RequestParam(defaultValue = "false") boolean activeOnly
	) {
		return ResponseEntity.ok(massageProviderService.list(activeOnly));
	}
}
