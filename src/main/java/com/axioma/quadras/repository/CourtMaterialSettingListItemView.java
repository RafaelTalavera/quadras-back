package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtMaterialCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface CourtMaterialSettingListItemView {
	Long getId();

	CourtMaterialCode getCode();

	String getLabel();

	BigDecimal getUnitPrice();

	Boolean getChargeGuest();

	Boolean getChargeVip();

	Boolean getChargeExternal();

	Boolean getChargePartnerCoach();

	Boolean getActive();

	OffsetDateTime getUpdatedAt();

	String getUpdatedBy();
}
