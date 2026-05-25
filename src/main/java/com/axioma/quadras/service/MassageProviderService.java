package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CreateMassageProviderDto;
import com.axioma.quadras.domain.dto.CreateMassageTherapistDto;
import com.axioma.quadras.domain.dto.MassageProviderDto;
import com.axioma.quadras.domain.dto.MassageTherapistDto;
import com.axioma.quadras.domain.dto.UpdateMassageProviderDto;
import com.axioma.quadras.domain.dto.UpdateMassageTherapistDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.repository.MassageProviderListItemView;
import com.axioma.quadras.repository.MassageProviderRepository;
import com.axioma.quadras.repository.MassageTherapistListItemView;
import com.axioma.quadras.repository.MassageTherapistRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MassageProviderService {

	private final MassageProviderRepository massageProviderRepository;
	private final MassageTherapistRepository massageTherapistRepository;
	private final AuditTrailService auditTrailService;

	public MassageProviderService(
			MassageProviderRepository massageProviderRepository,
			MassageTherapistRepository massageTherapistRepository,
			AuditTrailService auditTrailService
	) {
		this.massageProviderRepository = massageProviderRepository;
		this.massageTherapistRepository = massageTherapistRepository;
		this.auditTrailService = auditTrailService;
	}

	@Transactional
	public MassageProviderDto create(CreateMassageProviderDto input) {
		validateDuplicatedName(input.name(), null);
		final MassageProvider saved = massageProviderRepository.save(
				MassageProvider.create(input.name(), input.specialty(), input.contact())
		);
		auditTrailService.record(
				"massages",
				"massage-provider",
				saved.getId(),
				"CREATED",
				"Prestador de masajes creado",
				List.of(),
				null,
				snapshot(saved)
		);
		return MassageProviderDto.from(saved);
	}

	@Transactional
	public MassageProviderDto update(Long providerId, UpdateMassageProviderDto input) {
		final MassageProvider provider = findProviderOrThrow(providerId);
		final Map<String, Object> beforeState = snapshot(provider);
		validateDuplicatedName(input.name(), providerId);
		provider.update(
				input.name(),
				input.specialty(),
				input.contact(),
				input.active()
		);
		recordAudit(
				"massage-provider",
				provider.getId(),
				"UPDATED",
				"Prestador de masajes actualizado",
				beforeState,
				snapshot(provider)
		);
		return MassageProviderDto.from(provider);
	}

	public List<MassageProviderDto> list(boolean activeOnly) {
		final List<MassageProviderListItemView> providers = massageProviderRepository.findListItems(activeOnly);
		final Map<Long, List<MassageTherapistDto>> therapistsByProvider = loadTherapistsByProvider(
				providers.stream().map(MassageProviderListItemView::getId).toList()
		);
		return providers.stream()
				.map(provider -> MassageProviderDto.from(provider, therapistsByProvider.get(provider.getId())))
				.toList();
	}

	@Transactional
	public MassageTherapistDto createTherapist(Long providerId, CreateMassageTherapistDto input) {
		final MassageProvider provider = findProviderOrThrow(providerId);
		validateDuplicatedTherapistName(providerId, input.name(), null);
		final MassageTherapist saved = massageTherapistRepository.save(
				MassageTherapist.create(provider, input.name())
		);
		auditTrailService.record(
				"massages",
				"massage-therapist",
				saved.getId(),
				"CREATED",
				"Masajista creado",
				List.of(),
				null,
				snapshot(saved)
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
		final Map<String, Object> beforeState = snapshot(therapist);
		validateDuplicatedTherapistName(providerId, input.name(), therapistId);
		therapist.update(input.name(), input.active());
		recordAudit(
				"massage-therapist",
				therapist.getId(),
				"UPDATED",
				"Masajista actualizado",
				beforeState,
				snapshot(therapist)
		);
		return MassageTherapistDto.from(therapist);
	}

	public List<AuditEventDto> providerAudit(Long providerId) {
		findProviderOrThrow(providerId);
		return auditTrailService.findByEntity("massage-provider", providerId);
	}

	public List<AuditEventDto> therapistAudit(Long providerId, Long therapistId) {
		findTherapistOrThrow(providerId, therapistId);
		return auditTrailService.findByEntity("massage-therapist", therapistId);
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

	private Map<Long, List<MassageTherapistDto>> loadTherapistsByProvider(List<Long> providerIds) {
		if (providerIds.isEmpty()) {
			return Map.of();
		}
		final Map<Long, List<MassageTherapistDto>> therapistsByProvider = new LinkedHashMap<>();
		for (final MassageTherapistListItemView therapist :
				massageTherapistRepository.findListItemsByProviderIdInOrderByProviderIdAscNameAsc(
				providerIds
		)) {
			therapistsByProvider.computeIfAbsent(
					therapist.getProviderId(),
					ignored -> new ArrayList<>()
			).add(MassageTherapistDto.from(therapist));
		}
		return therapistsByProvider;
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
				"massages",
				entityType,
				entityId,
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(MassageProvider provider) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", provider.getId());
		snapshot.put("name", provider.getName());
		snapshot.put("specialty", provider.getSpecialty());
		snapshot.put("contact", provider.getContact());
		snapshot.put("active", provider.isActive());
		snapshot.put("createdAt", toValue(provider.getCreatedAt()));
		snapshot.put("updatedAt", toValue(provider.getUpdatedAt()));
		return snapshot;
	}

	private Map<String, Object> snapshot(MassageTherapist therapist) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", therapist.getId());
		snapshot.put("providerId", therapist.getProvider() == null ? null : therapist.getProvider().getId());
		snapshot.put("providerName", therapist.getProvider() == null ? null : therapist.getProvider().getName());
		snapshot.put("name", therapist.getName());
		snapshot.put("active", therapist.isActive());
		snapshot.put("createdAt", toValue(therapist.getCreatedAt()));
		snapshot.put("updatedAt", toValue(therapist.getUpdatedAt()));
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

	private String toValue(Object value) {
		return value == null ? null : value.toString();
	}
}
