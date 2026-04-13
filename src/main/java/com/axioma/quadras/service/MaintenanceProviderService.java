package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateMaintenanceProviderDto;
import com.axioma.quadras.domain.dto.MaintenanceProviderDto;
import com.axioma.quadras.domain.dto.UpdateMaintenanceProviderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceProviderService {

	private final MaintenanceProviderRepository maintenanceProviderRepository;

	public MaintenanceProviderService(MaintenanceProviderRepository maintenanceProviderRepository) {
		this.maintenanceProviderRepository = maintenanceProviderRepository;
	}

	public List<MaintenanceProviderDto> list() {
		return maintenanceProviderRepository.findAllProjectedByOrderByProviderTypeAscNameAsc().stream()
				.map(MaintenanceProviderDto::from)
				.toList();
	}

	@Transactional
	public MaintenanceProviderDto create(CreateMaintenanceProviderDto input, String actorUsername) {
		validateUniqueName(input.providerType(), input.name(), null);
		final MaintenanceProvider provider = maintenanceProviderRepository.save(
				MaintenanceProvider.create(
						input.providerType(),
						input.specialty(),
						input.name(),
						input.serviceLabel(),
						input.scopeDescription(),
						input.contact(),
						input.active() == null || input.active(),
						actorUsername
				)
		);
		return MaintenanceProviderDto.from(provider);
	}

	@Transactional
	public MaintenanceProviderDto update(
			Long providerId,
			UpdateMaintenanceProviderDto input,
			String actorUsername
	) {
		final MaintenanceProvider provider = findOrThrow(providerId);
		validateUniqueName(input.providerType(), input.name(), providerId);
		provider.update(
				input.providerType(),
				input.specialty(),
				input.name(),
				input.serviceLabel(),
				input.scopeDescription(),
				input.contact(),
				input.active(),
				actorUsername
		);
		return MaintenanceProviderDto.from(provider);
	}

	public MaintenanceProvider findOrThrow(Long providerId) {
		return maintenanceProviderRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Maintenance provider " + providerId + " not found"
				));
	}

	private void validateUniqueName(
			com.axioma.quadras.domain.model.MaintenanceProviderType providerType,
			String name,
			Long providerId
	) {
		final boolean exists = providerId == null
				? maintenanceProviderRepository.existsByProviderTypeAndNameIgnoreCase(providerType, name)
				: maintenanceProviderRepository.existsByProviderTypeAndNameIgnoreCaseAndIdNot(
						providerType,
						name,
						providerId
				);
		if (exists) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Maintenance provider name already exists for that type."
			);
		}
	}
}
