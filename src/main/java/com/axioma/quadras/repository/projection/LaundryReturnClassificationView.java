package com.axioma.quadras.repository.projection;

import java.math.BigDecimal;

public interface LaundryReturnClassificationView {
	BigDecimal getRewashQuantity();
	BigDecimal getClassifiedQuantity();
}
