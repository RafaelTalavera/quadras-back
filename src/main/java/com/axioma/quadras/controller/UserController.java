package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.AppUserDto;
import com.axioma.quadras.domain.dto.ChangeOwnPasswordDto;
import com.axioma.quadras.domain.dto.CreateAppUserDto;
import com.axioma.quadras.domain.dto.CurrentUserDto;
import com.axioma.quadras.domain.dto.ResetAppUserPasswordDto;
import com.axioma.quadras.domain.dto.UpdateAppUserDto;
import com.axioma.quadras.service.AuthService;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.UserManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final AuthService authService;
	private final UserManagementService userManagementService;

	public UserController(AuthService authService, UserManagementService userManagementService) {
		this.authService = authService;
		this.userManagementService = userManagementService;
	}

	@GetMapping("/me")
	public ResponseEntity<CurrentUserDto> currentUser(
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(authService.currentUser(principal));
	}

	@PatchMapping("/me/password")
	public ResponseEntity<Void> changeOwnPassword(
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody ChangeOwnPasswordDto input
	) {
		userManagementService.changeOwnPassword(principal, input);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<AppUserDto>> listUsers() {
		return ResponseEntity.ok(userManagementService.listUsers());
	}

	@PostMapping
	public ResponseEntity<AppUserDto> createUser(@Valid @RequestBody CreateAppUserDto input) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createUser(input));
	}

	@PutMapping("/{userId}")
	public ResponseEntity<AppUserDto> updateUser(
			@PathVariable Long userId,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody UpdateAppUserDto input
	) {
		return ResponseEntity.ok(userManagementService.updateUser(userId, principal, input));
	}

	@PatchMapping("/{userId}/password")
	public ResponseEntity<Void> resetPassword(
			@PathVariable Long userId,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody ResetAppUserPasswordDto input
	) {
		userManagementService.resetPassword(userId, principal, input);
		return ResponseEntity.noContent().build();
	}
}
