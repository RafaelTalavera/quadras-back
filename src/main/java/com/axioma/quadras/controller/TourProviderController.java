package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateTourProviderDto;
import com.axioma.quadras.domain.dto.TourProviderDto;
import com.axioma.quadras.domain.dto.UpdateTourProviderDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.TourProviderService;
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
@RequestMapping("/api/v1/tours/providers")
public class TourProviderController {

	private final TourProviderService tourProviderService;

	public TourProviderController(TourProviderService tourProviderService) {
		this.tourProviderService = tourProviderService;
	}

	@GetMapping
	public ResponseEntity<List<TourProviderDto>> list(
			@RequestParam(defaultValue = "false") boolean activeOnly
	) {
		return ResponseEntity.ok(tourProviderService.list(activeOnly));
	}

	@PostMapping
	public ResponseEntity<TourProviderDto> create(
			@Valid @RequestBody CreateTourProviderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final TourProviderDto created = tourProviderService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{providerId}")
	public ResponseEntity<TourProviderDto> update(
			@PathVariable Long providerId,
			@Valid @RequestBody UpdateTourProviderDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(tourProviderService.update(providerId, input, principal.getUsername()));
	}
}
