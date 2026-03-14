package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.AuthTokenDto;
import com.axioma.quadras.domain.dto.LoginRequestDto;
import com.axioma.quadras.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthTokenDto> login(@Valid @RequestBody LoginRequestDto input) {
		return ResponseEntity.ok(authService.login(input));
	}
}
