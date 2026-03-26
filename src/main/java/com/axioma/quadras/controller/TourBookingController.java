package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CancelTourBookingDto;
import com.axioma.quadras.domain.dto.CreateTourBookingDto;
import com.axioma.quadras.domain.dto.TourBookingDto;
import com.axioma.quadras.domain.dto.UpdateTourBookingDto;
import com.axioma.quadras.domain.dto.UpdateTourPaymentDto;
import com.axioma.quadras.domain.model.TourServiceType;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.TourBookingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tours/bookings")
public class TourBookingController {

	private final TourBookingService tourBookingService;

	public TourBookingController(TourBookingService tourBookingService) {
		this.tourBookingService = tourBookingService;
	}

	@PostMapping
	public ResponseEntity<TourBookingDto> create(
			@Valid @RequestBody CreateTourBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final TourBookingDto created = tourBookingService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public ResponseEntity<List<TourBookingDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) Boolean paid,
			@RequestParam(required = false) TourServiceType serviceType
	) {
		return ResponseEntity.ok(
				tourBookingService.list(dateFrom, dateTo, providerId, paid, serviceType)
		);
	}

	@PutMapping("/{bookingId}")
	public ResponseEntity<TourBookingDto> update(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateTourBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(tourBookingService.update(bookingId, input, principal.getUsername()));
	}

	@PatchMapping("/{bookingId}/payment")
	public ResponseEntity<TourBookingDto> updatePayment(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateTourPaymentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				tourBookingService.updatePayment(bookingId, input, principal.getUsername())
		);
	}

	@PatchMapping("/{bookingId}/cancel")
	public ResponseEntity<TourBookingDto> cancel(
			@PathVariable Long bookingId,
			@Valid @RequestBody CancelTourBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(tourBookingService.cancel(bookingId, input, principal.getUsername()));
	}
}
