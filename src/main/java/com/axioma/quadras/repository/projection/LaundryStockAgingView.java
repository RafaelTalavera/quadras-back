package com.axioma.quadras.repository.projection;

import com.axioma.quadras.domain.model.LaundryStockBucket;
import java.time.OffsetDateTime;

public interface LaundryStockAgingView {
	Long getItemId();

	LaundryStockBucket getBucket();

	Long getProviderScope();

	OffsetDateTime getOldestEffectiveAt();
}
