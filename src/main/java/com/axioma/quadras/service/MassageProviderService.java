package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateMassageProviderDto;
import com.axioma.quadras.domain.dto.CreateMassageTherapistDto;
import com.axioma.quadras.domain.dto.MassageProviderDto;
import com.axioma.quadras.domain.dto.MassageTherapistDto;
import com.axioma.quadras.domain.dto.UpdateMassageProviderDto;
import com.axioma.quadras.domain.dto.UpdateMassageTherapistDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.repository.MassageProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageProviderService {

	private final MassageProviderRepository massageProviderRepository;
	private final MassageTherapistRepository massageTherapistRepository;

	public MassageProviderService(
			MassageProviderRepository massageProviderRepository,
			MassageTherapistRepository massageTherapistRepository
	) {
		this.massageProviderRepository = massageProviderRepository;
		this.massageTherapistRepository = massageTherapistRepository;
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

	@Transactional
	public MassageTherapistDto createTherapist(Long providerId, CreateMassageTherapistDto input) {
		final MassageProvider provider = findProviderOrThrow(providerId);
		validateDuplicatedTherapistName(providerId, input.name(), null);
		final MassageTherapist saved = massageTherapistRepository.save(
				MassageTherapist.create(provider, input.name())
		);
		return MassageTherapistDto.from(saved);
	}

	@Transactional
	public MassageTherapistDto updateTherapist(
			Long providerId,
			Long therapistId,
			UpdateMassageTherapistDto input
	) {
		final MassageTherapist therapist = findTherapistOrThrow(providerId, therapistId);
		validateDuplicatedTherapistName(providerId, input.name(), therapistId);
		therapist.update(input.name(), input.active());
		return MassageTherapistDto.from(therapist);
	}

	public MassageProvider findProviderOrThrow(Long providerId) {
		return massageProviderRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Massage provider " + providerId + " not found"
				));
	}

	public MassageTherapist findTherapistOrThrow(Long providerId, Long therapistId) {
		final MassageTherapist therapist = massageTherapistRepository.findById(therapistId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Massage therapist " + therapistId + " not found"
				));
		if (!therapist.getProvider().getId().equals(providerId)) {
			throw new ApplicationException(
					HttpStatus.NOT_FOUND,
					"Massage therapist " + therapistId + " not found for provider " + providerId
			);
		}
		return therapist;
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

	private void validateDuplicatedTherapistName(Long providerId, String name, Long excludedId) {
		final boolean duplicated = excludedId == null
				? massageTherapistRepository.existsByProviderIdAndNameIgnoreCase(providerId, name)
				: massageTherapistRepository.existsByProviderIdAndNameIgnoreCaseAndIdNot(
						providerId,
						name,
						excludedId
				);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Massage therapist name already exists for provider."
			);
		}
	}
}
