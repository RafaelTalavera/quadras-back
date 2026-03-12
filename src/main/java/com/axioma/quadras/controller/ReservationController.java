package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CreateReservationDto;
import com.axioma.quadras.domain.dto.ReservationDto;
import com.axioma.quadras.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping
	public ResponseEntity<ReservationDto> create(@Valid @RequestBody CreateReservationDto input) {
		final ReservationDto created = reservationService.create(input);
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public ResponseEntity<List<ReservationDto>> list(
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate reservationDate
	) {
		return ResponseEntity.ok(reservationService.list(reservationDate));
	}

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationDto> findById(@PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationService.findById(reservationId));
	}
}
