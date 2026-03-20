package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateMassageProviderDto;
import com.axioma.quadras.domain.dto.MassageProviderDto;
import com.axioma.quadras.domain.dto.UpdateMassageProviderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.repository.MassageProviderRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageProviderService {

	private final MassageProviderRepository massageProviderRepository;

	public MassageProviderService(MassageProviderRepository massageProviderRepository) {
		this.massageProviderRepository = massageProviderRepository;
	}

	@Transactional
	public MassageProviderDto create(CreateMassageProviderDto input) {
		validateDuplicatedName(input.name(), null);
		final MassageProvider saved = massageProviderRepository.save(
				MassageProvider.create(input.name(), input.specialty(), input.contact())
		);
		return MassageProviderDto.from(saved);
	}

	@Transactional
	public MassageProviderDto update(Long providerId, UpdateMassageProviderDto input) {
		final MassageProvider provider = findProviderOrThrow(providerId);
		validateDuplicatedName(input.name(), providerId);
		provider.update(
				input.name(),
				input.specialty(),
				input.contact(),
				input.active()
		);
		return MassageProviderDto.from(provider);
	}

	public List<MassageProviderDto> list(boolean activeOnly) {
		final List<MassageProvider> providers = activeOnly
				? massageProviderRepository.findAllByActiveTrueOrderByNameAsc()
				: massageProviderRepository.findAllOrderedByName();
		return providers.stream().map(MassageProviderDto::from).toList();
	}

	public MassageProvider findProviderOrThrow(Long providerId) {
		return massageProviderRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Massage provider " + providerId + " not found"
				));
	}

	private void validateDuplicatedName(String name, Long excludedId) {
		final boolean duplicated = excludedId == null
				? massageProviderRepository.existsByNameIgnoreCase(name)
				: massageProviderRepository.existsByNameIgnoreCaseAndIdNot(name, excludedId);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Massage provider name already exists."
			);
		}
	}
}
