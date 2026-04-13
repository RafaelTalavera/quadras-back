package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtMaterialCode;
import java.math.BigDecimal;

public interface CourtBookingMaterialListItemView {
	Long getBookingId();

	CourtMaterialCode getMaterialCode();

	String getMaterialLabel();

	Integer getQuantity();

	BigDecimal getUnitPrice();

	BigDecimal getTotalPrice();
}
