package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.CurrentUserDto;
import com.axioma.quadras.service.AuthService;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final AuthService authService;

	public UserController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/me")
	public ResponseEntity<CurrentUserDto> currentUser(
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(authService.currentUser(principal));
	}
}
