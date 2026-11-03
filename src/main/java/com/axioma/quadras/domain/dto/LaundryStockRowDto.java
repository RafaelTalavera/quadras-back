package com.axioma.quadras.domain.dto;

import com.axioma.quadras.domain.model.LaundryStockBalance;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryUnitType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LaundryStockRowDto(
		Long balanceId,
		Long itemId,
		String itemName,
		LaundryUnitType unitType,
		LaundryStockBucket bucket,
		Long providerId,
		String providerName,
		BigDecimal quantity,
		OffsetDateTime oldestEffectiveAt,
		long ageDays,
		long version
) {
	public static LaundryStockRowDto from(
			LaundryStockBalance balance,
			OffsetDateTime oldestEffectiveAt,
			long ageDays
	) {
		return new LaundryStockRowDto(
				balance.getId(),
				balance.getItem().getId(),
				balance.getItem().getName(),
				balance.getItem().getUnitType(),
				balance.getBucket(),
				balance.getProvider() == null ? null : balance.getProvider().getId(),
				balance.getProvider() == null ? null : balance.getProvider().getName(),
				balance.getQuantity(),
				oldestEffectiveAt,
				ageDays,
				balance.getVersion()
		);
	}
}
