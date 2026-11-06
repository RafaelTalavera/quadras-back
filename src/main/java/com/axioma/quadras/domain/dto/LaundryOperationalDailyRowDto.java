package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LaundryOperationalDailyRowDto(
		LocalDate date,
		Long itemId,
		String itemName,
		LaundryUnitType unitType,
		BigDecimal sentQuantity,
		BigDecimal receivedQuantity,
		BigDecimal rewashedQuantity
) { }
