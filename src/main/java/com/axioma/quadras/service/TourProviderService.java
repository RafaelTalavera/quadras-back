package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateTourProviderDto;
import com.axioma.quadras.domain.dto.TourProviderDto;
import com.axioma.quadras.domain.dto.TourProviderOfferingDto;
import com.axioma.quadras.domain.dto.TourProviderOfferingInputDto;
import com.axioma.quadras.domain.dto.UpdateTourProviderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.TourProvider;
import com.axioma.quadras.domain.model.TourProviderOffering;
import com.axioma.quadras.repository.TourProviderRepository;
import com.axioma.quadras.repository.TourProviderOfferingRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TourProviderService {

	private final TourProviderRepository tourProviderRepository;
	private final TourProviderOfferingRepository tourProviderOfferingRepository;

	public TourProviderService(
			TourProviderRepository tourProviderRepository,
			TourProviderOfferingRepository tourProviderOfferingRepository
	) {
		this.tourProviderRepository = tourProviderRepository;
		this.tourProviderOfferingRepository = tourProviderOfferingRepository;
	}

	public List<TourProviderDto> list(boolean activeOnly) {
		final List<TourProvider> providers = activeOnly
				? tourProviderRepository.findAllByActiveTrueOrderByNameAsc()
				: tourProviderRepository.findAllByOrderByNameAsc();
		final Map<Long, List<TourProviderOfferingDto>> offeringsByProvider = loadOfferingsByProvider(providers);
		return providers.stream()
				.map(provider -> toDto(provider, offeringsByProvider.get(provider.getId())))
				.toList();
	}

	@Transactional
	public TourProviderDto create(CreateTourProviderDto input, String actorUsername) {
		validateDuplicatedProvider(input.name(), input.contact(), null);
		validateOfferingNames(input.offerings());
		final TourProvider saved = tourProviderRepository.save(
				TourProvider.create(
						input.name(),
						input.contact(),
						input.defaultCommissionPercent(),
						actorUsername
				)
		);
		final List<TourProviderOfferingDto> offerings = replaceOfferings(saved, input.offerings(), actorUsername);
		return toDto(saved, offerings);
	}

	@Transactional
	public TourProviderDto update(Long providerId, UpdateTourProviderDto input, String actorUsername) {
		final TourProvider provider = findProviderOrThrow(providerId);
		validateDuplicatedProvider(input.name(), input.contact(), providerId);
		validateOfferingNames(input.offerings());
		provider.update(
				input.name(),
				input.contact(),
				input.defaultCommissionPercent(),
				input.active(),
				actorUsername
		);
		final List<TourProviderOfferingDto> offerings = replaceOfferings(provider, input.offerings(), actorUsername);
		return toDto(provider, offerings);
	}

	public TourProvider findProviderOrThrow(Long providerId) {
		return tourProviderRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Tour provider " + providerId + " not found"
				));
	}

	public TourProviderOffering findOfferingOrThrow(Long providerOfferingId) {
		return tourProviderOfferingRepository.findById(providerOfferingId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Tour provider offering " + providerOfferingId + " not found"
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

	private List<TourProviderOfferingDto> replaceOfferings(
			TourProvider provider,
			List<TourProviderOfferingInputDto> offerings,
			String actorUsername
	) {
		tourProviderOfferingRepository.deleteAllByProviderId(provider.getId());
		final List<TourProviderOfferingInputDto> safeOfferings = offerings == null ? List.of() : offerings;
		if (safeOfferings.isEmpty()) {
			return List.of();
		}
		final List<TourProviderOffering> saved = tourProviderOfferingRepository.saveAll(
				safeOfferings.stream()
						.map(item -> TourProviderOffering.create(
								provider,
								item.serviceType(),
								item.name(),
								item.amount(),
								item.description(),
								Boolean.TRUE.equals(item.active()),
								actorUsername
						))
						.toList()
		);
		return saved.stream()
				.sorted(Comparator.comparing(item -> item.getName().toLowerCase()))
				.map(TourProviderOfferingDto::from)
				.toList();
	}

	private Map<Long, List<TourProviderOfferingDto>> loadOfferingsByProvider(List<TourProvider> providers) {
		if (providers.isEmpty()) {
			return Map.of();
		}
		final Set<Long> providerIds = providers.stream().map(TourProvider::getId).collect(Collectors.toSet());
		final Map<Long, List<TourProviderOfferingDto>> offeringsByProvider = new LinkedHashMap<>();
		for (final TourProviderOffering offering : tourProviderOfferingRepository
				.findAllByProviderIdInOrderByProviderIdAscNameAsc(providerIds)) {
			offeringsByProvider.computeIfAbsent(offering.getProvider().getId(), ignored -> new ArrayList<>())
					.add(TourProviderOfferingDto.from(offering));
		}
		return offeringsByProvider;
	}

	private TourProviderDto toDto(TourProvider provider, List<TourProviderOfferingDto> offerings) {
		final List<TourProviderOfferingDto> safeOfferings = offerings == null ? List.of() : offerings;
		return new TourProviderDto(
				provider.getId(),
				provider.getName(),
				provider.getContact(),
				provider.getDefaultCommissionPercent(),
				provider.isActive(),
				provider.getUpdatedAt(),
				provider.getUpdatedBy(),
				safeOfferings
		);
	}

	private void validateOfferingNames(List<TourProviderOfferingInputDto> offerings) {
		if (offerings == null || offerings.isEmpty()) {
			return;
		}
		final Set<String> names = offerings.stream()
				.map(TourProviderOfferingInputDto::name)
				.map(value -> value == null ? "" : value.trim().toLowerCase())
				.collect(Collectors.toSet());
		if (names.size() != offerings.size()) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Tour provider offerings must have unique names per provider."
			);
		}
	}
}
