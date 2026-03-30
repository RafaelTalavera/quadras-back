package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.MassageProviderDetailReportDto;
import com.axioma.quadras.domain.dto.MassageProviderSummaryDto;
import com.axioma.quadras.service.MassageReportService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/massages/reports")
public class MassageReportController {

	private final MassageReportService massageReportService;

	public MassageReportController(MassageReportService massageReportService) {
		this.massageReportService = massageReportService;
	}

	@GetMapping("/providers/summary")
	public ResponseEntity<List<MassageProviderSummaryDto>> listProviderSummary(
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo
	) {
		return ResponseEntity.ok(
				massageReportService.listProviderSummary(dateFrom, dateTo)
		);
	}

	@GetMapping("/providers/{providerId}/details")
	public ResponseEntity<MassageProviderDetailReportDto> getProviderDetails(
			@PathVariable Long providerId,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo
	) {
		return ResponseEntity.ok(
				massageReportService.getProviderDetails(providerId, dateFrom, dateTo)
		);
	}
}
