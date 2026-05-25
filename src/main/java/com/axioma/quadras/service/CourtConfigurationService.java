package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuditEventDto;
import com.axioma.quadras.domain.dto.CourtMaterialSettingDto;
import com.axioma.quadras.domain.dto.CourtPartnerCoachDto;
import com.axioma.quadras.domain.dto.CourtRateDto;
import com.axioma.quadras.domain.dto.CreateCourtPartnerCoachDto;
import com.axioma.quadras.domain.dto.UpdateCourtMaterialSettingDto;
import com.axioma.quadras.domain.dto.UpdateCourtPartnerCoachDto;
import com.axioma.quadras.domain.dto.UpdateCourtRateDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.CourtMaterialSetting;
import com.axioma.quadras.domain.model.CourtPartnerCoach;
import com.axioma.quadras.domain.model.CourtRate;
import com.axioma.quadras.repository.CourtMaterialSettingListItemView;
import com.axioma.quadras.repository.CourtPartnerCoachListItemView;
import com.axioma.quadras.repository.CourtRateListItemView;
import com.axioma.quadras.repository.CourtMaterialSettingRepository;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import com.axioma.quadras.repository.CourtRateRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourtConfigurationService {

	private final CourtRateRepository courtRateRepository;
	private final CourtMaterialSettingRepository courtMaterialSettingRepository;
	private final CourtPartnerCoachRepository courtPartnerCoachRepository;
	private final AuditTrailService auditTrailService;

	public CourtConfigurationService(
			CourtRateRepository courtRateRepository,
			CourtMaterialSettingRepository courtMaterialSettingRepository,
			CourtPartnerCoachRepository courtPartnerCoachRepository,
			AuditTrailService auditTrailService
	) {
		this.courtRateRepository = courtRateRepository;
		this.courtMaterialSettingRepository = courtMaterialSettingRepository;
		this.courtPartnerCoachRepository = courtPartnerCoachRepository;
		this.auditTrailService = auditTrailService;
	}

	public List<CourtRateDto> listRates() {
		return courtRateRepository.findAllByOrderByCustomerTypeAscPricingPeriodAsc().stream()
				.map(CourtRateDto::from)
				.toList();
	}

	@Transactional
	public CourtRateDto updateRate(Long rateId, UpdateCourtRateDto input, String actorUsername) {
		final CourtRate rate = findRateOrThrow(rateId);
		final Map<String, Object> beforeState = snapshot(rate);
		rate.update(input.amount(), input.active(), actorUsername);
		recordAudit("court-rate", rate.getId(), "UPDATED", "Tarifa de cancha actualizada", beforeState, snapshot(rate));
		return CourtRateDto.from(rate);
	}

	public List<CourtMaterialSettingDto> listMaterials() {
		return courtMaterialSettingRepository.findAllByOrderByCodeAsc().stream()
				.map(CourtMaterialSettingDto::from)
				.toList();
	}

	public List<CourtPartnerCoachDto> listPartnerCoaches(boolean activeOnly) {
		final List<CourtPartnerCoachListItemView> coaches = activeOnly
				? courtPartnerCoachRepository.findAllByActiveTrueOrderByNameAsc()
				: courtPartnerCoachRepository.findAllByOrderByNameAsc();
		return coaches.stream().map(CourtPartnerCoachDto::from).toList();
	}

	@Transactional
	public CourtPartnerCoachDto createPartnerCoach(
			CreateCourtPartnerCoachDto input,
			String actorUsername
	) {
		validateDuplicatedPartnerCoachName(input.name(), null);
		final CourtPartnerCoach saved = courtPartnerCoachRepository.save(
				CourtPartnerCoach.create(input.name(), actorUsername)
		);
		auditTrailService.record(
				"courts",
				"court-partner-coach",
				saved.getId(),
				"CREATED",
				"Profesor asociado creado",
				List.of(),
				null,
				snapshot(saved)
		);
		return CourtPartnerCoachDto.from(saved);
	}

	@Transactional
	public CourtPartnerCoachDto updatePartnerCoach(
			Long partnerCoachId,
			UpdateCourtPartnerCoachDto input,
			String actorUsername
	) {
		final CourtPartnerCoach coach = findPartnerCoachOrThrow(partnerCoachId);
		final Map<String, Object> beforeState = snapshot(coach);
		validateDuplicatedPartnerCoachName(input.name(), partnerCoachId);
		coach.update(input.name(), input.active(), actorUsername);
		recordAudit(
				"court-partner-coach",
				coach.getId(),
				"UPDATED",
				"Profesor asociado actualizado",
				beforeState,
				snapshot(coach)
		);
		return CourtPartnerCoachDto.from(coach);
	}

	@Transactional
	public CourtMaterialSettingDto updateMaterial(
			Long materialId,
			UpdateCourtMaterialSettingDto input,
			String actorUsername
	) {
		final CourtMaterialSetting material = findMaterialOrThrow(materialId);
		final Map<String, Object> beforeState = snapshot(material);
		material.update(
				input.label(),
				input.unitPrice(),
				input.chargeGuest(),
				input.chargeVip(),
				input.chargeExternal(),
				input.chargePartnerCoach(),
				input.active(),
				actorUsername
		);
		recordAudit(
				"court-material-setting",
				material.getId(),
				"UPDATED",
				"Material de cancha actualizado",
				beforeState,
				snapshot(material)
		);
		return CourtMaterialSettingDto.from(material);
	}

	public List<AuditEventDto> rateAudit(Long rateId) {
		findRateOrThrow(rateId);
		return auditTrailService.findByEntity("court-rate", rateId);
	}

	public List<AuditEventDto> materialAudit(Long materialId) {
		findMaterialOrThrow(materialId);
		return auditTrailService.findByEntity("court-material-setting", materialId);
	}

	public List<AuditEventDto> partnerCoachAudit(Long partnerCoachId) {
		findPartnerCoachOrThrow(partnerCoachId);
		return auditTrailService.findByEntity("court-partner-coach", partnerCoachId);
	}

	public CourtRate findRateOrThrow(Long rateId) {
		return courtRateRepository.findById(rateId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Court rate " + rateId + " not found"
				));
	}

	public CourtMaterialSetting findMaterialOrThrow(Long materialId) {
		return courtMaterialSettingRepository.findById(materialId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Court material " + materialId + " not found"
				));
	}

	public boolean isActivePartnerCoachName(String name) {
		return name != null
				&& !name.isBlank()
				&& courtPartnerCoachRepository.existsByNameIgnoreCaseAndActiveTrue(name.trim());
	}

	public CourtPartnerCoach findPartnerCoachOrThrow(Long partnerCoachId) {
		return courtPartnerCoachRepository.findById(partnerCoachId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Court partner coach " + partnerCoachId + " not found"
				));
	}

	private void validateDuplicatedPartnerCoachName(String name, Long excludedId) {
		final boolean duplicated = excludedId == null
				? courtPartnerCoachRepository.existsByNameIgnoreCase(name)
				: courtPartnerCoachRepository.existsByNameIgnoreCaseAndIdNot(name, excludedId);
		if (duplicated) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Court partner coach name already exists."
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
				"courts",
				entityType,
				entityId,
				actionName,
				summaryText,
				diff(beforeState, afterState),
				beforeState,
				afterState
		);
	}

	private Map<String, Object> snapshot(CourtRate rate) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", rate.getId());
		snapshot.put("customerType", rate.getCustomerType() == null ? null : rate.getCustomerType().name());
		snapshot.put("pricingPeriod", rate.getPricingPeriod() == null ? null : rate.getPricingPeriod().name());
		snapshot.put("amount", toValue(rate.getAmount()));
		snapshot.put("active", rate.isActive());
		snapshot.put("updatedAt", toValue(rate.getUpdatedAt()));
		snapshot.put("updatedBy", rate.getUpdatedBy());
		return snapshot;
	}

	private Map<String, Object> snapshot(CourtMaterialSetting material) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", material.getId());
		snapshot.put("code", material.getCode() == null ? null : material.getCode().name());
		snapshot.put("label", material.getLabel());
		snapshot.put("unitPrice", toValue(material.getUnitPrice()));
		snapshot.put("chargeGuest", material.isChargeGuest());
		snapshot.put("chargeVip", material.isChargeVip());
		snapshot.put("chargeExternal", material.isChargeExternal());
		snapshot.put("chargePartnerCoach", material.isChargePartnerCoach());
		snapshot.put("active", material.isActive());
		snapshot.put("updatedAt", toValue(material.getUpdatedAt()));
		snapshot.put("updatedBy", material.getUpdatedBy());
		return snapshot;
	}

	private Map<String, Object> snapshot(CourtPartnerCoach coach) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", coach.getId());
		snapshot.put("name", coach.getName());
		snapshot.put("active", coach.isActive());
		snapshot.put("updatedAt", toValue(coach.getUpdatedAt()));
		snapshot.put("updatedBy", coach.getUpdatedBy());
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
