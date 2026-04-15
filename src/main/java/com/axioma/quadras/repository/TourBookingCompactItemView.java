package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.TourBookingStatus;
import com.axioma.quadras.domain.model.TourServiceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TourBookingCompactItemView {
	Long getId();

	TourServiceType getServiceType();

	LocalDateTime getStartAt();

	LocalDateTime getEndAt();

	String getClientName();

	String getGuestReference();

	Long getProviderId();

	String getProviderName();

	Long getProviderOfferingId();

	String getProviderOfferingName();

	BigDecimal getAmount();

	Boolean getPaid();

	TourBookingStatus getStatus();
}
