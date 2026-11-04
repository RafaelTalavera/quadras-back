package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.LaundryOperationalMetricsDto;
import com.axioma.quadras.domain.dto.LaundryStockDashboardDto;
import com.axioma.quadras.domain.dto.LaundryStockDashboardItemDto;
import com.axioma.quadras.repository.LaundryBillableEventRepository;
import com.axioma.quadras.repository.LaundryDocumentRepository;
import com.axioma.quadras.repository.LaundryReturnManifestRepository;
import com.axioma.quadras.repository.projection.LaundryReturnClassificationView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaundryOperationalMetricsService {
	private final LaundryStockService stockService;
	private final LaundryDocumentRepository documentRepository;
	private final LaundryReturnManifestRepository returnRepository;
	private final LaundryBillableEventRepository billableRepository;

	public LaundryOperationalMetricsService(LaundryStockService stockService,
			LaundryDocumentRepository documentRepository,
			LaundryReturnManifestRepository returnRepository,
			LaundryBillableEventRepository billableRepository) {
		this.stockService = stockService;
		this.documentRepository = documentRepository;
		this.returnRepository = returnRepository;
		this.billableRepository = billableRepository;
	}

	public LaundryOperationalMetricsDto metrics(Long providerId) {
		LaundryStockDashboardDto dashboard = stockService.dashboard(providerId);
		LaundryReturnClassificationView classification = returnRepository.classification(providerId);
		BigDecimal rewash = quantity(classification.getRewashQuantity());
		BigDecimal classified = quantity(classification.getClassifiedQuantity());
		BigDecimal rate = classified.signum() == 0 ? BigDecimal.ZERO.setScale(2)
				: rewash.multiply(new BigDecimal("100")).divide(classified, 2, RoundingMode.HALF_UP);
		return new LaundryOperationalMetricsDto(OffsetDateTime.now(ZoneOffset.UTC), providerId,
				dashboard.totals(), oldestPending(dashboard), documentRepository.countObserved(providerId),
				returnRepository.countAwaitingHotel(providerId),
				quantity(billableRepository.sumAcceptedClean(providerId)), rewash, classified, rate);
	}

	private OffsetDateTime oldestPending(LaundryStockDashboardDto dashboard) {
		return dashboard.items().stream().flatMap(this::pendingDates)
				.filter(Objects::nonNull).min(OffsetDateTime::compareTo).orElse(null);
	}

	private Stream<OffsetDateTime> pendingDates(LaundryStockDashboardItemDto item) {
		return Stream.of(item.oldestHotelRewashQueueAt(), item.oldestProviderProcessingAt(),
				item.oldestProviderRewashAt(), item.oldestMissingAt(), item.oldestDamagedAt());
	}

	private BigDecimal quantity(BigDecimal value) {
		return (value == null ? BigDecimal.ZERO : value).setScale(3, RoundingMode.HALF_UP);
	}
}
