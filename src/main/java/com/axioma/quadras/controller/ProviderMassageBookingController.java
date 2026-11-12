package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.ProviderMassageBookingDto;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.service.ProviderMassageBookingService;
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
@RequestMapping("/api/v1/provider/massages/bookings")
public class ProviderMassageBookingController {

	private final ProviderMassageBookingService providerMassageBookingService;

	public ProviderMassageBookingController(ProviderMassageBookingService providerMassageBookingService) {
		this.providerMassageBookingService = providerMassageBookingService;
	}

	@GetMapping
	public ResponseEntity<List<ProviderMassageBookingDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(required = false) MassageBookingStatus status
	) {
		return ResponseEntity.ok(providerMassageBookingService.list(
				bookingDate, dateFrom, dateTo, status
		));
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<ProviderMassageBookingDto> get(@PathVariable Long bookingId) {
		return ResponseEntity.ok(providerMassageBookingService.get(bookingId));
	}
}
