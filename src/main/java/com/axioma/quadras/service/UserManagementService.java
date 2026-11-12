package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AppUserDto;
import com.axioma.quadras.domain.dto.ChangeOwnPasswordDto;
import com.axioma.quadras.domain.dto.CreateAppUserDto;
import com.axioma.quadras.domain.dto.ResetAppUserPasswordDto;
import com.axioma.quadras.domain.dto.UpdateAppUserDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.CourtPartnerCoach;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.AppUserRepository;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserManagementService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditTrailService auditTrailService;
	private final CourtPartnerCoachRepository courtPartnerCoachRepository;
	private final MassageTherapistRepository massageTherapistRepository;
	private final MaintenanceProviderRepository maintenanceProviderRepository;
	private final LaundryProviderRepository laundryProviderRepository;

	public UserManagementService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			AuditTrailService auditTrailService,
			CourtPartnerCoachRepository courtPartnerCoachRepository,
			MassageTherapistRepository massageTherapistRepository,
			MaintenanceProviderRepository maintenanceProviderRepository,
			LaundryProviderRepository laundryProviderRepository
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditTrailService = auditTrailService;
		this.courtPartnerCoachRepository = courtPartnerCoachRepository;
		this.massageTherapistRepository = massageTherapistRepository;
		this.maintenanceProviderRepository = maintenanceProviderRepository;
		this.laundryProviderRepository = laundryProviderRepository;
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
		final ResolvedProviderAccess access = resolveProviderAccess(input.role(), input.providerType(),
				input.providerResourceId(), input.laundryProviderId(), null);
		final AppUser appUser = AppUser.create(
				normalizedUsername,
				passwordEncoder.encode(input.password()),
				input.role(),
				access.type(), access.courtPartnerCoach(), access.massageTherapist(),
				access.maintenanceProvider(), access.laundryProvider(),
				enabled
		);
		final AppUser savedUser = saveUserAccess(appUser, input.role());
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
		final ResolvedProviderAccess access = resolveProviderAccess(input.role(), input.providerType(),
				input.providerResourceId(), input.laundryProviderId(), appUser.getId());

		appUser.updateAccess(input.role(), access.type(), access.courtPartnerCoach(),
				access.massageTherapist(), access.maintenanceProvider(), access.laundryProvider(),
				input.enabled().booleanValue());
		final AppUser savedUser = saveUserAccess(appUser, input.role());
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

	private AppUser saveUserAccess(AppUser appUser, AppUserRole role) {
		try {
			return appUserRepository.saveAndFlush(appUser);
		} catch (DataIntegrityViolationException exception) {
			if (role == AppUserRole.PROVIDER) {
				throw new ApplicationException(HttpStatus.CONFLICT,
						"The provider resource already has an access account.");
			}
			throw exception;
		}
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

	private ResolvedProviderAccess resolveProviderAccess(AppUserRole role, ProviderAccessType providerType,
			Long providerResourceId, Long legacyLaundryProviderId, Long currentUserId) {
		if (role != AppUserRole.PROVIDER) {
			if (providerType != null || providerResourceId != null || legacyLaundryProviderId != null) {
				throw new ApplicationException(HttpStatus.BAD_REQUEST,
						"Provider access is only allowed for PROVIDER role.");
			}
			return ResolvedProviderAccess.none();
		}

		ProviderAccessType resolvedType = providerType;
		Long resolvedResourceId = providerResourceId;
		if (resolvedType == null && resolvedResourceId == null && legacyLaundryProviderId != null) {
			resolvedType = ProviderAccessType.LAUNDRY;
			resolvedResourceId = legacyLaundryProviderId;
		}
		if (resolvedType == null || resolvedResourceId == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST,
					"providerType and providerResourceId are required for PROVIDER role.");
		}
		if (legacyLaundryProviderId != null
				&& (resolvedType != ProviderAccessType.LAUNDRY
				|| !legacyLaundryProviderId.equals(resolvedResourceId))) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST,
					"laundryProviderId must match LAUNDRY provider access when supplied.");
		}
		ensureProviderResourceIsAvailable(resolvedType, resolvedResourceId, currentUserId);

		return switch (resolvedType) {
			case COURTS -> resolveCourtAccess(resolvedResourceId);
			case MASSAGE -> resolveMassageAccess(resolvedResourceId);
			case MAINTENANCE -> resolveMaintenanceAccess(resolvedResourceId);
			case LAUNDRY -> resolveLaundryAccess(resolvedResourceId);
		};
	}

	private void ensureProviderResourceIsAvailable(
			ProviderAccessType providerType,
			Long providerResourceId,
			Long currentUserId
	) {
		final boolean assigned = switch (providerType) {
			case COURTS -> currentUserId == null
					? appUserRepository.existsByCourtPartnerCoachId(providerResourceId)
					: appUserRepository.existsByCourtPartnerCoachIdAndIdNot(providerResourceId, currentUserId);
			case MASSAGE -> currentUserId == null
					? appUserRepository.existsByMassageTherapistId(providerResourceId)
					: appUserRepository.existsByMassageTherapistIdAndIdNot(providerResourceId, currentUserId);
			case MAINTENANCE -> currentUserId == null
					? appUserRepository.existsByMaintenanceProviderId(providerResourceId)
					: appUserRepository.existsByMaintenanceProviderIdAndIdNot(providerResourceId, currentUserId);
			case LAUNDRY -> currentUserId == null
					? appUserRepository.existsByLaundryProviderId(providerResourceId)
					: appUserRepository.existsByLaundryProviderIdAndIdNot(providerResourceId, currentUserId);
		};
		if (assigned) {
			throw new ApplicationException(HttpStatus.CONFLICT,
					"The provider resource already has an access account.");
		}
	}

	private ResolvedProviderAccess resolveCourtAccess(Long resourceId) {
		final CourtPartnerCoach coach = courtPartnerCoachRepository.findById(resourceId)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Court partner coach not found."));
		if (!coach.isActive()) {
			throw inactiveProviderAccess("court partner coach");
		}
		return new ResolvedProviderAccess(ProviderAccessType.COURTS, coach, null, null, null);
	}

	private ResolvedProviderAccess resolveMassageAccess(Long resourceId) {
		final MassageTherapist therapist = massageTherapistRepository.findById(resourceId)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Massage therapist not found."));
		if (!therapist.isActive()) {
			throw inactiveProviderAccess("massage therapist");
		}
		return new ResolvedProviderAccess(ProviderAccessType.MASSAGE, null, therapist, null, null);
	}

	private ResolvedProviderAccess resolveMaintenanceAccess(Long resourceId) {
		final MaintenanceProvider provider = maintenanceProviderRepository.findById(resourceId)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Maintenance provider not found."));
		if (!provider.isActive() || provider.getProviderType() != MaintenanceProviderType.EXTERNAL) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST,
					"Maintenance provider access requires an active external provider.");
		}
		return new ResolvedProviderAccess(ProviderAccessType.MAINTENANCE, null, null, provider, null);
	}

	private ResolvedProviderAccess resolveLaundryAccess(Long resourceId) {
		final LaundryProvider provider = laundryProviderRepository.findById(resourceId)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found."));
		if (!provider.isActive()) {
			throw inactiveProviderAccess("laundry provider");
		}
		return new ResolvedProviderAccess(ProviderAccessType.LAUNDRY, null, null, null, provider);
	}

	private ApplicationException inactiveProviderAccess(String resourceName) {
		return new ApplicationException(HttpStatus.BAD_REQUEST,
				"Inactive " + resourceName + " cannot be assigned to a user.");
	}

	private Map<String, Object> auditSnapshot(AppUser appUser) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", appUser.getId());
		snapshot.put("username", appUser.getUsername());
		snapshot.put("role", appUser.getRole().name());
		final ProviderAccess providerAccess = appUser.providerAccess();
		snapshot.put("providerType", providerAccess == null ? null : providerAccess.type().name());
		snapshot.put("providerResourceId", providerAccess == null ? null : providerAccess.resourceId());
		snapshot.put("laundryProviderId", appUser.getLaundryProvider() == null
				? null : appUser.getLaundryProvider().getId());
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
		snapshot.put("providerType", appUser.providerAccess() == null
				? null : appUser.providerAccess().type().name());
		snapshot.put("providerResourceId", appUser.providerAccess() == null
				? null : appUser.providerAccess().resourceId());
		snapshot.put("laundryProviderId", appUser.laundryProviderId());
		snapshot.put("enabled", appUser.enabled());
		snapshot.put("createdAt", appUser.createdAt() == null ? null : appUser.createdAt().toString());
		snapshot.put("updatedAt", appUser.updatedAt() == null ? null : appUser.updatedAt().toString());
		return snapshot;
	}

	private record ResolvedProviderAccess(
			ProviderAccessType type,
			CourtPartnerCoach courtPartnerCoach,
			MassageTherapist massageTherapist,
			MaintenanceProvider maintenanceProvider,
			LaundryProvider laundryProvider
	) {
		private static ResolvedProviderAccess none() {
			return new ResolvedProviderAccess(null, null, null, null, null);
		}
	}
}
