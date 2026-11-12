package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.ProviderCourtBookingDto;
import com.axioma.quadras.domain.model.CourtBookingStatus;
import com.axioma.quadras.service.ProviderCourtBookingService;
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
@RequestMapping("/api/v1/provider/courts/bookings")
public class ProviderCourtBookingController {

	private final ProviderCourtBookingService providerCourtBookingService;

	public ProviderCourtBookingController(ProviderCourtBookingService providerCourtBookingService) {
		this.providerCourtBookingService = providerCourtBookingService;
	}

	@GetMapping
	public ResponseEntity<List<ProviderCourtBookingDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(required = false) CourtBookingStatus status
	) {
		return ResponseEntity.ok(providerCourtBookingService.list(
				bookingDate, dateFrom, dateTo, status
		));
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<ProviderCourtBookingDto> get(@PathVariable Long bookingId) {
		return ResponseEntity.ok(providerCourtBookingService.get(bookingId));
	}
}
