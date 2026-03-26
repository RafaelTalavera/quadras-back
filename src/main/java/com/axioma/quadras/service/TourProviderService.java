package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateTourProviderDto;
import com.axioma.quadras.domain.dto.TourProviderDto;
import com.axioma.quadras.domain.dto.UpdateTourProviderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.TourProvider;
import com.axioma.quadras.repository.TourProviderRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TourProviderService {

	private final TourProviderRepository tourProviderRepository;

	public TourProviderService(TourProviderRepository tourProviderRepository) {
		this.tourProviderRepository = tourProviderRepository;
	}

	public List<TourProviderDto> list(boolean activeOnly) {
		final List<TourProvider> providers = activeOnly
				? tourProviderRepository.findAllByActiveTrueOrderByNameAsc()
				: tourProviderRepository.findAllByOrderByNameAsc();
		return providers.stream().map(TourProviderDto::from).toList();
	}

	@Transactional
	public TourProviderDto create(CreateTourProviderDto input, String actorUsername) {
		validateDuplicatedProvider(input.name(), input.contact(), null);
		final TourProvider saved = tourProviderRepository.save(
				TourProvider.create(
						input.name(),
						input.contact(),
						input.defaultCommissionPercent(),
						actorUsername
				)
		);
		return TourProviderDto.from(saved);
	}

	@Transactional
	public TourProviderDto update(Long providerId, UpdateTourProviderDto input, String actorUsername) {
		final TourProvider provider = findProviderOrThrow(providerId);
		validateDuplicatedProvider(input.name(), input.contact(), providerId);
		provider.update(
				input.name(),
				input.contact(),
				input.defaultCommissionPercent(),
				input.active(),
				actorUsername
		);
		return TourProviderDto.from(provider);
	}

	public TourProvider findProviderOrThrow(Long providerId) {
		return tourProviderRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Tour provider " + providerId + " not found"
				));
	}

	private void validateDuplicatedProvider(String name, String contact, Long excludedId) {
		final boolean duplicated = excludedId == null
				? tourProviderRepository.existsByNameIgnoreCaseAndContactIgnoreCase(name, contact)
				: tourProviderRepository.existsByNameIgnoreCaseAndContactIgnoreCaseAndIdNot(
						name,
						contact,
						excludedId
				);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Tour provider name and contact already exist."
			);
		}
	}
}
