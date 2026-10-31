package com.axioma.quadras.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface LaundryMovementSummaryView {
	OffsetDateTime getOccurredAt();
	BigDecimal getQuantity();
}
