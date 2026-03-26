package com.axioma.quadras.service;

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
import com.axioma.quadras.repository.CourtMaterialSettingRepository;
import com.axioma.quadras.repository.CourtPartnerCoachRepository;
import com.axioma.quadras.repository.CourtRateRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourtConfigurationService {

	private final CourtRateRepository courtRateRepository;
	private final CourtMaterialSettingRepository courtMaterialSettingRepository;
	private final CourtPartnerCoachRepository courtPartnerCoachRepository;

	public CourtConfigurationService(
			CourtRateRepository courtRateRepository,
			CourtMaterialSettingRepository courtMaterialSettingRepository,
			CourtPartnerCoachRepository courtPartnerCoachRepository
	) {
		this.courtRateRepository = courtRateRepository;
		this.courtMaterialSettingRepository = courtMaterialSettingRepository;
		this.courtPartnerCoachRepository = courtPartnerCoachRepository;
	}

	public List<CourtRateDto> listRates() {
		return courtRateRepository.findAll().stream()
				.sorted(
						Comparator.comparing(CourtRate::getCustomerType)
								.thenComparing(CourtRate::getPricingPeriod)
				)
				.map(CourtRateDto::from)
				.toList();
	}

	@Transactional
	public CourtRateDto updateRate(Long rateId, UpdateCourtRateDto input, String actorUsername) {
		final CourtRate rate = findRateOrThrow(rateId);
		rate.update(input.amount(), input.active(), actorUsername);
		return CourtRateDto.from(rate);
	}

	public List<CourtMaterialSettingDto> listMaterials() {
		return courtMaterialSettingRepository.findAll().stream()
				.sorted(Comparator.comparing(CourtMaterialSetting::getCode))
				.map(CourtMaterialSettingDto::from)
				.toList();
	}

	public List<CourtPartnerCoachDto> listPartnerCoaches(boolean activeOnly) {
		final List<CourtPartnerCoach> coaches = activeOnly
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
		return CourtPartnerCoachDto.from(saved);
	}

	@Transactional
	public CourtPartnerCoachDto updatePartnerCoach(
			Long partnerCoachId,
			UpdateCourtPartnerCoachDto input,
			String actorUsername
	) {
		final CourtPartnerCoach coach = findPartnerCoachOrThrow(partnerCoachId);
		validateDuplicatedPartnerCoachName(input.name(), partnerCoachId);
		coach.update(input.name(), input.active(), actorUsername);
		return CourtPartnerCoachDto.from(coach);
	}

	@Transactional
	public CourtMaterialSettingDto updateMaterial(
			Long materialId,
			UpdateCourtMaterialSettingDto input,
			String actorUsername
	) {
		final CourtMaterialSetting material = findMaterialOrThrow(materialId);
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
		return CourtMaterialSettingDto.from(material);
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
}
