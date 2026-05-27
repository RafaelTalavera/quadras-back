package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CreateTourProviderDto;
import com.axioma.quadras.domain.dto.TourProviderDto;
import com.axioma.quadras.domain.dto.TourProviderOfferingDto;
import com.axioma.quadras.domain.dto.TourProviderOfferingInputDto;
import com.axioma.quadras.domain.dto.UpdateTourProviderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.TourProvider;
import com.axioma.quadras.domain.model.TourProviderOffering;
import com.axioma.quadras.repository.TourProviderRepository;
import com.axioma.quadras.repository.TourProviderListItemView;
import com.axioma.quadras.repository.TourProviderOfferingRepository;
import com.axioma.quadras.repository.TourProviderOfferingListItemView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final AuditTrailService auditTrailService;

	public TourProviderService(
			TourProviderRepository tourProviderRepository,
			TourProviderOfferingRepository tourProviderOfferingRepository,
			AuditTrailService auditTrailService
	) {
		this.tourProviderRepository = tourProviderRepository;
		this.tourProviderOfferingRepository = tourProviderOfferingRepository;
		this.auditTrailService = auditTrailService;
	}

	public List<TourProviderDto> list(boolean activeOnly) {
		final List<TourProviderListItemView> providers = tourProviderRepository.findListItems(activeOnly);
		final Map<Long, List<TourProviderOfferingDto>> offeringsByProvider = loadOfferingsByProvider(
				providers.stream().map(TourProviderListItemView::getId).toList()
		);
		return providers.stream()
				.map(provider -> TourProviderDto.from(provider, offeringsByProvider.get(provider.getId())))
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
		final Map<String, Object> providerAfterState = snapshot(saved);
		auditTrailService.record(
				"tours",
				"tour-provider",
				saved.getId(),
				"CREATED",
				"Proveedor de tours creado",
				List.of(),
				null,
				providerAfterState
		);
		final List<TourProviderOfferingDto> offerings = syncOfferings(saved, input.offerings(), actorUsername);
		return toDto(saved, offerings);
	}

	@Transactional
	public TourProviderDto update(Long providerId, UpdateTourProviderDto input, String actorUsername) {
		final TourProvider provider = findProviderOrThrow(providerId);
		final Map<String, Object> beforeState = snapshot(provider);
		validateDuplicatedProvider(input.name(), input.contact(), providerId);
		validateOfferingNames(input.offerings());
		provider.update(
				input.name(),
				input.contact(),
				input.defaultCommissionPercent(),
				input.active(),
				actorUsername
		);
		recordAudit(
				"tour-provider",
				provider.getId(),
				"UPDATED",
				"Proveedor de tours actualizado",
				beforeState,
				snapshot(provider)
		);
		final List<TourProviderOfferingDto> offerings = syncOfferings(provider, input.offerings(), actorUsername);
		return toDto(provider, offerings);
	}

	public List<AuditEventDto> providerAudit(Long providerId) {
		findProviderOrThrow(providerId);
		return auditTrailService.findByEntity("tour-provider", providerId);
	}

	public List<AuditEventDto> offeringAudit(Long providerId, Long offeringId) {
		final TourProviderOffering offering = findOfferingOrThrow(offeringId);
		if (!Objects.equals(offering.getProvider().getId(), providerId)) {
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Tour provider offering " + offeringId + " not found");
		}
		return auditTrailService.findByEntity("tour-provider-offering", offeringId);
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

	private List<TourProviderOfferingDto> syncOfferings(
			TourProvider provider,
			List<TourProviderOfferingInputDto> offerings,
			String actorUsername
	) {
		final List<TourProviderOfferingInputDto> safeOfferings = offerings == null ? List.of() : offerings;
		final Map<String, TourProviderOffering> existingByName = new LinkedHashMap<>();
		for (final TourProviderOffering offering :
				tourProviderOfferingRepository.findAllByProviderIdOrderByNameAsc(provider.getId())) {
			existingByName.put(normalizeOfferingName(offering.getName()), offering);
		}
		if (safeOfferings.isEmpty()) {
			if (!existingByName.isEmpty()) {
				existingByName.values().forEach(offering -> auditTrailService.record(
						"tours",
						"tour-provider-offering",
						offering.getId(),
						"DELETED",
						"Oferta de tour eliminada",
						deletionChange("active", snapshot(offering)),
						snapshot(offering),
						null
				));
				tourProviderOfferingRepository.deleteAllInBatch(existingByName.values());
			}
			return List.of();
		}
		final List<TourProviderOffering> synchronizedOfferings = new ArrayList<>();
		final List<TourProviderOffering> offeringsToCreate = new ArrayList<>();
		for (final TourProviderOfferingInputDto item : safeOfferings) {
			final TourProviderOffering existing = existingByName.remove(normalizeOfferingName(item.name()));
			if (existing == null) {
				final TourProviderOffering created = TourProviderOffering.create(
						provider,
						item.serviceType(),
						item.name(),
						item.amount(),
						item.description(),
						Boolean.TRUE.equals(item.active()),
						actorUsername
				);
				offeringsToCreate.add(created);
				continue;
			}
			final Map<String, Object> beforeState = snapshot(existing);
			existing.update(
					item.serviceType(),
					item.name(),
					item.amount(),
					item.description(),
					Boolean.TRUE.equals(item.active()),
					actorUsername
			);
			recordAudit(
					"tour-provider-offering",
					existing.getId(),
					"UPDATED",
					"Oferta de tour actualizada",
					beforeState,
					snapshot(existing)
			);
			synchronizedOfferings.add(existing);
		}
		if (!existingByName.isEmpty()) {
			existingByName.values().forEach(offering -> auditTrailService.record(
					"tours",
					"tour-provider-offering",
					offering.getId(),
					"DELETED",
					"Oferta de tour eliminada",
					deletionChange("offering", snapshot(offering)),
					snapshot(offering),
					null
			));
			tourProviderOfferingRepository.deleteAllInBatch(existingByName.values());
		}
		if (!offeringsToCreate.isEmpty()) {
			final List<TourProviderOffering> savedOfferings = tourProviderOfferingRepository.saveAll(offeringsToCreate);
			savedOfferings.forEach(offering -> auditTrailService.record(
					"tours",
					"tour-provider-offering",
					offering.getId(),
					"CREATED",
					"Oferta de tour creada",
					List.of(),
					null,
					snapshot(offering)
			));
			synchronizedOfferings.addAll(savedOfferings);
		}
		return synchronizedOfferings.stream()
				.sorted(Comparator.comparing(item -> item.getName().toLowerCase()))
				.map(TourProviderOfferingDto::from)
				.toList();
	}

	private String normalizeOfferingName(String value) {
		return value == null ? "" : value.trim().toLowerCase();
	}

	private Map<Long, List<TourProviderOfferingDto>> loadOfferingsByProvider(List<Long> providerIds) {
		if (providerIds.isEmpty()) {
			return Map.of();
		}
		final Map<Long, List<TourProviderOfferingDto>> offeringsByProvider = new LinkedHashMap<>();
		for (final TourProviderOfferingListItemView offering : tourProviderOfferingRepository
				.findListItemsByProviderIdInOrderByProviderIdAscNameAsc(providerIds)) {
			offeringsByProvider.computeIfAbsent(offering.getProviderId(), ignored -> new ArrayList<>())
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

	private void recordAudit(
			String entityType,
			Long entityId,
			String actionName,
			String summaryText,
			Map<String, Object> beforeState,
			Map<String, Object> afterState
	) {
		auditTrailService.record(
				"tours",
				entityType,
				entityId,
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(TourProvider provider) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", provider.getId());
		snapshot.put("name", provider.getName());
		snapshot.put("contact", provider.getContact());
		snapshot.put("defaultCommissionPercent", toValue(provider.getDefaultCommissionPercent()));
		snapshot.put("active", provider.isActive());
		snapshot.put("createdAt", toValue(provider.getCreatedAt()));
		snapshot.put("updatedAt", toValue(provider.getUpdatedAt()));
		snapshot.put("updatedBy", provider.getUpdatedBy());
		return snapshot;
	}

	private Map<String, Object> snapshot(TourProviderOffering offering) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", offering.getId());
		snapshot.put("providerId", offering.getProvider() == null ? null : offering.getProvider().getId());
		snapshot.put("providerName", offering.getProvider() == null ? null : offering.getProvider().getName());
		snapshot.put("serviceType", offering.getServiceType() == null ? null : offering.getServiceType().name());
		snapshot.put("name", offering.getName());
		snapshot.put("amount", toValue(offering.getAmount()));
		snapshot.put("description", offering.getDescription());
		snapshot.put("active", offering.isActive());
		snapshot.put("updatedAt", toValue(offering.getUpdatedAt()));
		snapshot.put("updatedBy", offering.getUpdatedBy());
		return snapshot;
	}

	private List<Map<String, Object>> diff(Map<String, Object> before, Map<String, Object> after) {
		return before.keySet().stream()
				.filter(field -> !Objects.equals(before.get(field), after.get(field)))
				.map(field -> {
					final Map<String, Object> change = new LinkedHashMap<>();
					change.put("field", field);
					change.put("before", before.get(field));
					change.put("after", after.get(field));
					return change;
				})
				.toList();
	}

	private List<Map<String, Object>> deletionChange(String field, Object beforeValue) {
		final Map<String, Object> change = new LinkedHashMap<>();
		change.put("field", field);
		change.put("before", beforeValue);
		change.put("after", null);
		return List.of(change);
	}

	private String toValue(Object value) {
		return value == null ? null : value.toString();
	}
}
