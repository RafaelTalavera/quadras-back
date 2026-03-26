package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CourtMaterialSettingDto;
import com.axioma.quadras.domain.dto.CourtPartnerCoachDto;
import com.axioma.quadras.domain.dto.CourtRateDto;
import com.axioma.quadras.domain.dto.CreateCourtPartnerCoachDto;
import com.axioma.quadras.domain.dto.UpdateCourtMaterialSettingDto;
import com.axioma.quadras.domain.dto.UpdateCourtPartnerCoachDto;
import com.axioma.quadras.domain.dto.UpdateCourtRateDto;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.CourtConfigurationService;
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
@RequestMapping("/api/v1/courts")
public class CourtConfigurationController {

	private final CourtConfigurationService courtConfigurationService;

	public CourtConfigurationController(CourtConfigurationService courtConfigurationService) {
		this.courtConfigurationService = courtConfigurationService;
	}

	@GetMapping("/rates")
	public ResponseEntity<List<CourtRateDto>> listRates() {
		return ResponseEntity.ok(courtConfigurationService.listRates());
	}

	@PutMapping("/rates/{rateId}")
	public ResponseEntity<CourtRateDto> updateRate(
			@PathVariable Long rateId,
			@Valid @RequestBody UpdateCourtRateDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(courtConfigurationService.updateRate(rateId, input, principal.getUsername()));
	}

	@GetMapping("/materials")
	public ResponseEntity<List<CourtMaterialSettingDto>> listMaterials() {
		return ResponseEntity.ok(courtConfigurationService.listMaterials());
	}

	@GetMapping("/partner-coaches")
	public ResponseEntity<List<CourtPartnerCoachDto>> listPartnerCoaches(
			@RequestParam(defaultValue = "true") boolean activeOnly
	) {
		return ResponseEntity.ok(courtConfigurationService.listPartnerCoaches(activeOnly));
	}

	@PostMapping("/partner-coaches")
	public ResponseEntity<CourtPartnerCoachDto> createPartnerCoach(
			@Valid @RequestBody CreateCourtPartnerCoachDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final CourtPartnerCoachDto created = courtConfigurationService.createPartnerCoach(
				input,
				principal.getUsername()
		);
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/partner-coaches/{partnerCoachId}")
	public ResponseEntity<CourtPartnerCoachDto> updatePartnerCoach(
			@PathVariable Long partnerCoachId,
			@Valid @RequestBody UpdateCourtPartnerCoachDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				courtConfigurationService.updatePartnerCoach(
						partnerCoachId,
						input,
						principal.getUsername()
				)
		);
	}

	@PutMapping("/materials/{materialId}")
	public ResponseEntity<CourtMaterialSettingDto> updateMaterial(
			@PathVariable Long materialId,
			@Valid @RequestBody UpdateCourtMaterialSettingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				courtConfigurationService.updateMaterial(materialId, input, principal.getUsername())
		);
	}
}
