package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AppUserDto;
import com.axioma.quadras.domain.dto.ChangeOwnPasswordDto;
import com.axioma.quadras.domain.dto.CreateAppUserDto;
import com.axioma.quadras.domain.dto.ResetAppUserPasswordDto;
import com.axioma.quadras.domain.dto.UpdateAppUserDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.repository.AppUserRepository;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserManagementService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditTrailService auditTrailService;

	public UserManagementService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			AuditTrailService auditTrailService
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditTrailService = auditTrailService;
	}

	public List<AppUserDto> listUsers() {
		return appUserRepository.findAllByOrderByUsernameAsc()
				.stream()
				.map(AppUserDto::from)
				.toList();
	}

	@Transactional
	public AppUserDto createUser(CreateAppUserDto input) {
		PasswordPolicy.validatePlaintext(input.password());
		final String normalizedUsername = AppUser.normalizeUsername(input.username());
		if (appUserRepository.existsByUsername(normalizedUsername)) {
			throw new ApplicationException(HttpStatus.CONFLICT, "A user with that username already exists.");
		}

		final boolean enabled = input.enabled() == null || input.enabled().booleanValue();
		final AppUser appUser = AppUser.create(
				normalizedUsername,
				passwordEncoder.encode(input.password()),
				input.role(),
				enabled
		);
		final AppUser savedUser = appUserRepository.save(appUser);
		auditTrailService.record(
				"security",
				"APP_USER",
				savedUser.getId(),
				"USER_CREATED",
				"User " + savedUser.getUsername() + " was created.",
				Map.of("username", savedUser.getUsername(), "role", savedUser.getRole().name(), "enabled", savedUser.isEnabled()),
				null,
				auditSnapshot(savedUser)
		);
		return AppUserDto.from(savedUser);
	}

	@Transactional
	public AppUserDto updateUser(Long userId, AuthenticatedUserPrincipal principal, UpdateAppUserDto input) {
		final AppUser appUser = requireUser(userId);
		final AppUserDto before = AppUserDto.from(appUser);
		final boolean selfTarget = principal.getUsername().equals(appUser.getUsername());

		if (selfTarget && !input.enabled().booleanValue()) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot disable your own account.");
		}
		if (selfTarget && appUser.getRole() != input.role()) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot change your own role.");
		}
		ensureSupervisorStillExists(appUser, input.role(), input.enabled().booleanValue());

		appUser.updateRoleAndStatus(input.role(), input.enabled().booleanValue());
		final AppUser savedUser = appUserRepository.save(appUser);
		auditTrailService.record(
				"security",
				"APP_USER",
				savedUser.getId(),
				"USER_UPDATED",
				"User " + savedUser.getUsername() + " was updated.",
				Map.of("role", savedUser.getRole().name(), "enabled", savedUser.isEnabled()),
				auditSnapshot(before),
				auditSnapshot(savedUser)
		);
		return AppUserDto.from(savedUser);
	}

	@Transactional
	public void resetPassword(
			Long userId,
			AuthenticatedUserPrincipal principal,
			ResetAppUserPasswordDto input
	) {
		final AppUser appUser = requireUser(userId);
		PasswordPolicy.validatePlaintext(input.newPassword());
		appUser.changePasswordHash(passwordEncoder.encode(input.newPassword()));
		final AppUser savedUser = appUserRepository.save(appUser);
		auditTrailService.record(
				"security",
				"APP_USER",
				savedUser.getId(),
				"USER_PASSWORD_RESET",
				"Password reset for user " + savedUser.getUsername() + ".",
				Map.of("targetUsername", savedUser.getUsername(), "requestedBy", principal.getUsername()),
				null,
				null
		);
	}

	@Transactional
	public void changeOwnPassword(
			AuthenticatedUserPrincipal principal,
			ChangeOwnPasswordDto input
	) {
		final AppUser appUser = requireByUsername(principal.getUsername());
		if (!passwordEncoder.matches(input.currentPassword(), appUser.getPasswordHash())) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Current password is invalid.");
		}
		if (passwordEncoder.matches(input.newPassword(), appUser.getPasswordHash())) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "New password must be different from the current password.");
		}
		PasswordPolicy.validatePlaintext(input.newPassword());
		appUser.changePasswordHash(passwordEncoder.encode(input.newPassword()));
		final AppUser savedUser = appUserRepository.save(appUser);
		auditTrailService.record(
				"security",
				"APP_USER",
				savedUser.getId(),
				"USER_PASSWORD_CHANGED",
				"User " + savedUser.getUsername() + " changed their password.",
				Map.of("username", savedUser.getUsername()),
				null,
				null
		);
	}

	private AppUser requireUser(Long userId) {
		return appUserRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));
	}

	private AppUser requireByUsername(String username) {
		return appUserRepository.findByUsername(AppUser.normalizeUsername(username))
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));
	}

	private void ensureSupervisorStillExists(AppUser targetUser, AppUserRole newRole, boolean enabled) {
		final boolean removingEnabledSupervisor = targetUser.getRole() == AppUserRole.SUPERVISOR
				&& targetUser.isEnabled()
				&& (newRole != AppUserRole.SUPERVISOR || !enabled);
		if (!removingEnabledSupervisor) {
			return;
		}
		final long enabledSupervisors = appUserRepository.countByRoleAndEnabledTrue(AppUserRole.SUPERVISOR);
		if (enabledSupervisors <= 1L) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"At least one enabled supervisor must remain in the system."
			);
		}
	}

	private Map<String, Object> auditSnapshot(AppUser appUser) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", appUser.getId());
		snapshot.put("username", appUser.getUsername());
		snapshot.put("role", appUser.getRole().name());
		snapshot.put("enabled", appUser.isEnabled());
		snapshot.put("createdAt", appUser.getCreatedAt() == null ? null : appUser.getCreatedAt().toString());
		snapshot.put("updatedAt", appUser.getUpdatedAt() == null ? null : appUser.getUpdatedAt().toString());
		return snapshot;
	}

	private Map<String, Object> auditSnapshot(AppUserDto appUser) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", appUser.id());
		snapshot.put("username", appUser.username());
		snapshot.put("role", appUser.role());
		snapshot.put("enabled", appUser.enabled());
		snapshot.put("createdAt", appUser.createdAt() == null ? null : appUser.createdAt().toString());
		snapshot.put("updatedAt", appUser.updatedAt() == null ? null : appUser.updatedAt().toString());
		return snapshot;
	}
}
