package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryStockBucket;
import java.math.BigDecimal;

public record LaundryReconciliationMismatchDto(
		Long itemId,
		String itemName,
		LaundryStockBucket bucket,
		Long providerId,
		BigDecimal balanceQuantity,
		BigDecimal lotQuantity,
		BigDecimal difference
) { }
