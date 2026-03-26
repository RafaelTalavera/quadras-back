package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.CourtMaterialCode;
import com.axioma.quadras.domain.model.CourtMaterialSetting;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CourtMaterialSettingDto(
		Long id,
		CourtMaterialCode code,
		String label,
		BigDecimal unitPrice,
		boolean chargeGuest,
		boolean chargeVip,
		boolean chargeExternal,
		boolean chargePartnerCoach,
		boolean active,
		OffsetDateTime updatedAt,
		String updatedBy
) {
	public static CourtMaterialSettingDto from(CourtMaterialSetting material) {
		return new CourtMaterialSettingDto(
				material.getId(),
				material.getCode(),
				material.getLabel(),
				material.getUnitPrice(),
				material.isChargeGuest(),
				material.isChargeVip(),
				material.isChargeExternal(),
				material.isChargePartnerCoach(),
				material.isActive(),
				material.getUpdatedAt(),
				material.getUpdatedBy()
		);
	}
}
