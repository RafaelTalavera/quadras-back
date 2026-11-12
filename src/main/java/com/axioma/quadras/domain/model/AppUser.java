package com.axioma.quadras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Entity
@Table(name = "app_users")
public class AppUser {

	private static final int MAX_USERNAME_LENGTH = 80;
	private static final int MAX_PASSWORD_HASH_LENGTH = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", nullable = false, length = MAX_USERNAME_LENGTH, unique = true)
	private String username;

	@Column(name = "password_hash", nullable = false, length = MAX_PASSWORD_HASH_LENGTH)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 40)
	private AppUserRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type", length = 20)
	private ProviderAccessType providerType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "court_partner_coach_id")
	private CourtPartnerCoach courtPartnerCoach;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "massage_therapist_id")
	private MassageTherapist massageTherapist;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "maintenance_provider_id")
	private MaintenanceProvider maintenanceProvider;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "laundry_provider_id")
	private LaundryProvider laundryProvider;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@Column(name = "security_version", nullable = false)
	private long securityVersion;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected AppUser() {
	}

	private AppUser(String username, String passwordHash, AppUserRole role,
			LaundryProvider laundryProvider, boolean enabled) {
		this(username, passwordHash, role,
				role == AppUserRole.PROVIDER ? ProviderAccessType.LAUNDRY : null,
				null, null, null, laundryProvider, enabled);
	}

	private AppUser(String username, String passwordHash, AppUserRole role,
			ProviderAccessType providerType, CourtPartnerCoach courtPartnerCoach,
			MassageTherapist massageTherapist, MaintenanceProvider maintenanceProvider,
			LaundryProvider laundryProvider, boolean enabled) {
		this.username = normalizeUsername(username);
		this.passwordHash = normalizePasswordHash(passwordHash);
		this.role = requireRole(role);
		applyProviderAssociation(role, providerType, courtPartnerCoach, massageTherapist,
				maintenanceProvider, laundryProvider);
		this.enabled = enabled;
		this.securityVersion = 1L;
	}

	public static AppUser create(String username, String passwordHash, AppUserRole role, boolean enabled) {
		return new AppUser(username, passwordHash, role, null, enabled);
	}

	public static AppUser create(String username, String passwordHash, AppUserRole role,
			LaundryProvider laundryProvider, boolean enabled) {
		return new AppUser(username, passwordHash, role, laundryProvider, enabled);
	}

	public static AppUser create(String username, String passwordHash, AppUserRole role,
			ProviderAccessType providerType, CourtPartnerCoach courtPartnerCoach,
			MassageTherapist massageTherapist, MaintenanceProvider maintenanceProvider,
			LaundryProvider laundryProvider, boolean enabled) {
		return new AppUser(username, passwordHash, role, providerType, courtPartnerCoach,
				massageTherapist, maintenanceProvider, laundryProvider, enabled);
	}

	public static String normalizeUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username is required");
		}
		final String normalized = username.trim().toLowerCase(Locale.ROOT);
		if (normalized.length() > MAX_USERNAME_LENGTH) {
			throw new IllegalArgumentException(
					"username must be <= " + MAX_USERNAME_LENGTH + " chars"
			);
		}
		return normalized;
	}

	public boolean updateRoleAndStatus(AppUserRole role, boolean enabled) {
		return updateAccess(role, null, enabled);
	}

	public boolean updateAccess(AppUserRole role, LaundryProvider laundryProvider, boolean enabled) {
		return updateAccess(role, role == AppUserRole.PROVIDER ? ProviderAccessType.LAUNDRY : null,
				null, null, null, laundryProvider, enabled);
	}

	public boolean updateAccess(AppUserRole role, ProviderAccessType providerType,
			CourtPartnerCoach courtPartnerCoach, MassageTherapist massageTherapist,
			MaintenanceProvider maintenanceProvider, LaundryProvider laundryProvider,
			boolean enabled) {
		final AppUserRole normalizedRole = requireRole(role);
		validateProviderAssociation(normalizedRole, providerType, courtPartnerCoach,
				massageTherapist, maintenanceProvider, laundryProvider);
		boolean changed = false;
		if (this.role != normalizedRole) {
			this.role = normalizedRole;
			changed = true;
		}
		if (this.providerType != providerType) {
			this.providerType = providerType;
			changed = true;
		}
		if (this.courtPartnerCoach != courtPartnerCoach) {
			this.courtPartnerCoach = courtPartnerCoach;
			changed = true;
		}
		if (this.massageTherapist != massageTherapist) {
			this.massageTherapist = massageTherapist;
			changed = true;
		}
		if (this.maintenanceProvider != maintenanceProvider) {
			this.maintenanceProvider = maintenanceProvider;
			changed = true;
		}
		if (this.laundryProvider != laundryProvider) {
			this.laundryProvider = laundryProvider;
			changed = true;
		}
		if (this.enabled != enabled) {
			this.enabled = enabled;
			changed = true;
		}
		if (changed) {
			this.securityVersion++;
		}
		return changed;
	}

	public boolean changePasswordHash(String passwordHash) {
		final String normalizedPasswordHash = normalizePasswordHash(passwordHash);
		if (this.passwordHash.equals(normalizedPasswordHash)) {
			return false;
		}
		this.passwordHash = normalizedPasswordHash;
		this.securityVersion++;
		return true;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public AppUserRole getRole() {
		return role;
	}

	public ProviderAccessType getProviderType() {
		return providerType;
	}

	public CourtPartnerCoach getCourtPartnerCoach() {
		return courtPartnerCoach;
	}

	public MassageTherapist getMassageTherapist() {
		return massageTherapist;
	}

	public MaintenanceProvider getMaintenanceProvider() {
		return maintenanceProvider;
	}

	public LaundryProvider getLaundryProvider() {
		return laundryProvider;
	}

	public ProviderAccess providerAccess() {
		if (role != AppUserRole.PROVIDER) {
			return null;
		}
		return switch (providerType) {
			case COURTS -> new ProviderAccess(providerType, courtPartnerCoach.getId(), courtPartnerCoach.getName());
			case MASSAGE -> new ProviderAccess(providerType, massageTherapist.getId(), massageTherapist.getName());
			case MAINTENANCE -> new ProviderAccess(providerType, maintenanceProvider.getId(), maintenanceProvider.getName());
			case LAUNDRY -> new ProviderAccess(providerType, laundryProvider.getId(), laundryProvider.getName());
		};
	}

	public boolean isEnabled() {
		return enabled;
	}

	public long getSecurityVersion() {
		return securityVersion;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	@PrePersist
	void onCreate() {
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}

	private static String normalizePasswordHash(String passwordHash) {
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new IllegalArgumentException("passwordHash is required");
		}
		final String normalized = passwordHash.trim();
		if (normalized.length() > MAX_PASSWORD_HASH_LENGTH) {
			throw new IllegalArgumentException(
					"passwordHash must be <= " + MAX_PASSWORD_HASH_LENGTH + " chars"
			);
		}
		return normalized;
	}

	private static AppUserRole requireRole(AppUserRole role) {
		if (role == null) {
			throw new IllegalArgumentException("role is required");
		}
		return role;
	}

	private void applyProviderAssociation(AppUserRole role, ProviderAccessType providerType,
			CourtPartnerCoach courtPartnerCoach, MassageTherapist massageTherapist,
			MaintenanceProvider maintenanceProvider, LaundryProvider laundryProvider) {
		validateProviderAssociation(role, providerType, courtPartnerCoach, massageTherapist,
				maintenanceProvider, laundryProvider);
		this.providerType = providerType;
		this.courtPartnerCoach = courtPartnerCoach;
		this.massageTherapist = massageTherapist;
		this.maintenanceProvider = maintenanceProvider;
		this.laundryProvider = laundryProvider;
	}

	private static void validateProviderAssociation(AppUserRole role, ProviderAccessType providerType,
			CourtPartnerCoach courtPartnerCoach, MassageTherapist massageTherapist,
			MaintenanceProvider maintenanceProvider, LaundryProvider laundryProvider) {
		final int associationCount = (courtPartnerCoach == null ? 0 : 1)
				+ (massageTherapist == null ? 0 : 1)
				+ (maintenanceProvider == null ? 0 : 1)
				+ (laundryProvider == null ? 0 : 1);
		if (role != AppUserRole.PROVIDER) {
			if (providerType != null || associationCount != 0) {
				throw new IllegalArgumentException("provider access is only allowed for PROVIDER role");
			}
			return;
		}
		if (providerType == null || associationCount != 1) {
			throw new IllegalArgumentException("exactly one provider access is required for PROVIDER role");
		}
		final boolean matchingAssociation = switch (providerType) {
			case COURTS -> courtPartnerCoach != null;
			case MASSAGE -> massageTherapist != null;
			case MAINTENANCE -> maintenanceProvider != null;
			case LAUNDRY -> laundryProvider != null;
		};
		if (!matchingAssociation) {
			throw new IllegalArgumentException("provider type does not match associated resource");
		}
	}
}
