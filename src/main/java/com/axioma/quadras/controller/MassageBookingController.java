package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CancelMassageBookingDto;
import com.axioma.quadras.domain.dto.CreateMassageBookingDto;
import com.axioma.quadras.domain.dto.MassageBookingDto;
import com.axioma.quadras.domain.dto.UpdateMassageBookingDto;
import com.axioma.quadras.domain.dto.UpdateMassagePaymentDto;
import com.axioma.quadras.service.MassageBookingService;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/massages/bookings")
public class MassageBookingController {

	private final MassageBookingService massageBookingService;

	public MassageBookingController(MassageBookingService massageBookingService) {
		this.massageBookingService = massageBookingService;
	}

	@PostMapping
	public ResponseEntity<MassageBookingDto> create(
			@Valid @RequestBody CreateMassageBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final MassageBookingDto created = massageBookingService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public ResponseEntity<List<MassageBookingDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate bookingDate,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo,
			@RequestParam(required = false) String clientName,
			@RequestParam(required = false) String guestReference,
			@RequestParam(required = false) Long providerId,
			@RequestParam(required = false) Boolean paid
	) {
		return ResponseEntity.ok(
				massageBookingService.list(
						bookingDate,
						dateFrom,
						dateTo,
						clientName,
						guestReference,
						providerId,
						paid
				)
		);
	}

	@PutMapping("/{bookingId}")
	public ResponseEntity<MassageBookingDto> update(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateMassageBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				massageBookingService.update(bookingId, input, principal.getUsername())
		);
	}

	@PatchMapping("/{bookingId}/payment")
	public ResponseEntity<MassageBookingDto> updatePayment(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateMassagePaymentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(massageBookingService.updatePayment(bookingId, input, principal.getUsername()));
	}

	@PatchMapping("/{bookingId}/cancel")
	public ResponseEntity<MassageBookingDto> cancel(
			@PathVariable Long bookingId,
			@Valid @RequestBody CancelMassageBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(
				massageBookingService.cancel(bookingId, input, principal.getUsername())
		);
	}
}
