package com.axioma.quadras.repository.projection;

import com.axioma.quadras.domain.model.LaundryStockBucket;
import java.math.BigDecimal;

public interface LaundryStockLotTotalView {
	Long getItemId();
	LaundryStockBucket getBucket();
	Long getProviderScope();
	BigDecimal getQuantity();
}
