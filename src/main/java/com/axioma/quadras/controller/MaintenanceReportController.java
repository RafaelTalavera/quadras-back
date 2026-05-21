package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.MaintenanceDailyExecutiveReportDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryDetailDto;
import com.axioma.quadras.domain.dto.MaintenanceSummaryReportDto;
import com.axioma.quadras.domain.model.MaintenanceSummaryGroupBy;
import com.axioma.quadras.service.MaintenanceReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maintenance/reports")
public class MaintenanceReportController {

	private final MaintenanceReportService maintenanceReportService;

	public MaintenanceReportController(MaintenanceReportService maintenanceReportService) {
		this.maintenanceReportService = maintenanceReportService;
	}

	@GetMapping("/summary")
	public ResponseEntity<MaintenanceSummaryReportDto> summary(
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo
	) {
		return ResponseEntity.ok(maintenanceReportService.summary(dateFrom, dateTo));
	}

	@GetMapping("/summary/daily-executive")
	public ResponseEntity<MaintenanceDailyExecutiveReportDto> dailyExecutive(
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate date
	) {
		return ResponseEntity.ok(maintenanceReportService.dailyExecutive(date));
	}

	@GetMapping("/summary/details")
	public ResponseEntity<MaintenanceSummaryDetailDto> summaryDetails(
			@RequestParam MaintenanceSummaryGroupBy groupBy,
			@RequestParam(required = false) String groupKey,
			// Legacy compatibility for older clients. New consumers must use groupKey.
			@RequestParam(required = false) String code,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo
	) {
		final String resolvedGroupKey = groupKey != null && !groupKey.isBlank() ? groupKey : code;
		return ResponseEntity.ok(
				maintenanceReportService.summaryDetails(groupBy, resolvedGroupKey, dateFrom, dateTo)
		);
	}
}
