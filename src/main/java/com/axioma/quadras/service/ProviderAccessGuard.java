package com.axioma.quadras.service;

import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ProviderAccessGuard {

	private final CurrentActorService currentActorService;
	private final CourtPartnerCoachRepository courtPartnerCoachRepository;
	private final MassageTherapistRepository massageTherapistRepository;
	private final MaintenanceProviderRepository maintenanceProviderRepository;
	private final LaundryProviderRepository laundryProviderRepository;

	public ProviderAccessGuard(CurrentActorService currentActorService,
			CourtPartnerCoachRepository courtPartnerCoachRepository,
			MassageTherapistRepository massageTherapistRepository,
			MaintenanceProviderRepository maintenanceProviderRepository,
			LaundryProviderRepository laundryProviderRepository) {
		this.currentActorService = currentActorService;
		this.courtPartnerCoachRepository = courtPartnerCoachRepository;
		this.massageTherapistRepository = massageTherapistRepository;
		this.maintenanceProviderRepository = maintenanceProviderRepository;
		this.laundryProviderRepository = laundryProviderRepository;
	}

	public ProviderAccess requireType(ProviderAccessType expectedType) {
		final AuthenticatedUserPrincipal principal = currentActorService.requirePrincipal();
		if (principal.getRole() != AppUserRole.PROVIDER || principal.getProviderAccess() == null
				|| principal.getProviderType() != expectedType) {
			throw forbidden("Provider access type is not allowed for this operation");
		}
		final ProviderAccess access = principal.getProviderAccess();
		if (!isOperational(access)) {
			throw forbidden("Provider access is inactive or no longer operational");
		}
		return access;
	}

	public ProviderAccess requireOwnership(ProviderAccessType expectedType, Long resourceId) {
		final ProviderAccess access = requireType(expectedType);
		if (resourceId == null || !access.resourceId().equals(resourceId)) {
			throw forbidden("Provider does not own the requested resource");
		}
		return access;
	}

	private boolean isOperational(ProviderAccess access) {
		return switch (access.type()) {
			case COURTS -> courtPartnerCoachRepository.findById(access.resourceId())
					.map(coach -> coach.isActive()).orElse(false);
			case MASSAGE -> massageTherapistRepository.findById(access.resourceId())
					.map(therapist -> therapist.isActive() && therapist.getProvider().isActive()).orElse(false);
			case MAINTENANCE -> maintenanceProviderRepository.findById(access.resourceId())
					.map(provider -> provider.isActive()
							&& provider.getProviderType() == MaintenanceProviderType.EXTERNAL).orElse(false);
			case LAUNDRY -> laundryProviderRepository.findById(access.resourceId())
					.map(provider -> provider.isActive()).orElse(false);
		};
	}

	private ApplicationException forbidden(String message) {
		return new ApplicationException(HttpStatus.FORBIDDEN, message);
	}
}
