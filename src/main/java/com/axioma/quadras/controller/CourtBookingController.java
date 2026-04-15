package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CancelCourtBookingDto;
import com.axioma.quadras.domain.dto.CreateCourtBookingDto;
import com.axioma.quadras.domain.dto.CourtBookingDto;
import com.axioma.quadras.domain.dto.CourtSummaryReportDto;
import com.axioma.quadras.domain.dto.UpdateCourtBookingDto;
import com.axioma.quadras.domain.dto.UpdateCourtPaymentDto;
import com.axioma.quadras.domain.model.CourtCustomerType;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.CourtBookingService;
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
@RequestMapping("/api/v1/courts/bookings")
public class CourtBookingController {

	private final CourtBookingService courtBookingService;

	public CourtBookingController(CourtBookingService courtBookingService) {
		this.courtBookingService = courtBookingService;
	}

	@PostMapping
	public ResponseEntity<CourtBookingDto> create(
			@Valid @RequestBody CreateCourtBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		final CourtBookingDto created = courtBookingService.create(input, principal.getUsername());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public ResponseEntity<List<CourtBookingDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate bookingDate,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo,
			@RequestParam(required = false) CourtCustomerType customerType,
			@RequestParam(required = false) Boolean paid
	) {
		return ResponseEntity.ok(courtBookingService.list(bookingDate, dateFrom, dateTo, customerType, paid));
	}

	@PutMapping("/{bookingId}")
	public ResponseEntity<CourtBookingDto> update(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateCourtBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(courtBookingService.update(bookingId, input, principal.getUsername()));
	}

	@PatchMapping("/{bookingId}/payment")
	public ResponseEntity<CourtBookingDto> updatePayment(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateCourtPaymentDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(courtBookingService.updatePayment(bookingId, input, principal.getUsername()));
	}

	@PatchMapping("/{bookingId}/cancel")
	public ResponseEntity<CourtBookingDto> cancel(
			@PathVariable Long bookingId,
			@Valid @RequestBody CancelCourtBookingDto input,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(courtBookingService.cancel(bookingId, input, principal.getUsername()));
	}

	@GetMapping("/summary")
	public ResponseEntity<CourtSummaryReportDto> summary(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo
	) {
		return ResponseEntity.ok(courtBookingService.summary(dateFrom, dateTo));
	}
}
