package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.TourProviderSummaryDto;
import com.axioma.quadras.service.TourBookingService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tours/reports")
public class TourReportController {

	private final TourBookingService tourBookingService;

	public TourReportController(TourBookingService tourBookingService) {
		this.tourBookingService = tourBookingService;
	}

	@GetMapping("/providers/summary")
	public ResponseEntity<List<TourProviderSummaryDto>> providerSummary(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo
	) {
		return ResponseEntity.ok(tourBookingService.providerSummary(dateFrom, dateTo));
	}
}
