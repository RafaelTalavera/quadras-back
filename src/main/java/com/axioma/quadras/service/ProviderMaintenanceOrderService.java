package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AddMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.CompleteMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.ProviderMaintenanceAttachmentDto;
import com.axioma.quadras.domain.dto.ProviderMaintenanceOrderDto;
import com.axioma.quadras.domain.dto.StartMaintenanceOrderDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderMaintenanceOrderService {

	private static final long MAX_RANGE_DAYS = 31;

	private final MaintenanceOrderRepository maintenanceOrderRepository;
	private final MaintenanceOrderService maintenanceOrderService;
	private final ProviderAccessGuard providerAccessGuard;
	private final CurrentActorService currentActorService;

	public ProviderMaintenanceOrderService(MaintenanceOrderRepository maintenanceOrderRepository,
			MaintenanceOrderService maintenanceOrderService, ProviderAccessGuard providerAccessGuard,
			CurrentActorService currentActorService) {
		this.maintenanceOrderRepository = maintenanceOrderRepository;
		this.maintenanceOrderService = maintenanceOrderService;
		this.providerAccessGuard = providerAccessGuard;
		this.currentActorService = currentActorService;
	}

	@Transactional(readOnly = true)
	public List<ProviderMaintenanceOrderDto> list(LocalDate dateFrom, LocalDate dateTo,
			MaintenanceOrderStatus status) {
		final ProviderAccess access = requireAccess();
		final DateRange range = resolveRange(dateFrom, dateTo);
		return maintenanceOrderRepository.findProviderViews(
				access.resourceId(), range.dateFrom().atStartOfDay(), range.dateTo().plusDays(1).atStartOfDay(),
				range.dateFrom().atStartOfDay().atOffset(ZoneOffset.UTC),
				range.dateTo().plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC), status
		).stream().map(ProviderMaintenanceOrderDto::from).toList();
	}

	@Transactional(readOnly = true)
	public ProviderMaintenanceOrderDto get(Long orderId) {
		final ProviderAccess access = requireAccess();
		return maintenanceOrderRepository.findProviderViewByIdAndProviderId(orderId, access.resourceId())
				.map(ProviderMaintenanceOrderDto::from)
				.orElseThrow(this::notFound);
	}

	public ProviderMaintenanceOrderDto start(Long orderId, StartMaintenanceOrderDto input) {
		final ProviderAccess access = requireAccess();
		return ProviderMaintenanceOrderDto.from(maintenanceOrderService.startForProvider(
				orderId, access.resourceId(), input, currentActorService.currentUsername()
		));
	}

	public ProviderMaintenanceOrderDto complete(Long orderId, CompleteMaintenanceOrderDto input) {
		final ProviderAccess access = requireAccess();
		return ProviderMaintenanceOrderDto.from(maintenanceOrderService.completeForProvider(
				orderId, access.resourceId(), input, currentActorService.currentUsername()
		));
	}

	public List<ProviderMaintenanceAttachmentDto> listAttachments(Long orderId) {
		final ProviderAccess access = requireAccess();
		return maintenanceOrderService.listAttachmentsForProvider(orderId, access.resourceId()).stream()
				.map(ProviderMaintenanceAttachmentDto::from).toList();
	}

	public ProviderMaintenanceAttachmentDto addAttachment(Long orderId, AddMaintenanceAttachmentDto input) {
		final ProviderAccess access = requireAccess();
		return ProviderMaintenanceAttachmentDto.from(maintenanceOrderService.addAttachmentForProvider(
				orderId, access.resourceId(), input, currentActorService.currentUsername()
		));
	}

	private ProviderAccess requireAccess() {
		return providerAccessGuard.requireType(ProviderAccessType.MAINTENANCE);
	}

	private DateRange resolveRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom == null && dateTo == null) {
			final LocalDate today = LocalDate.now();
			return new DateRange(today, today);
		}
		if (dateFrom == null || dateTo == null) {
			throw badRequest("dateFrom and dateTo must be provided together");
		}
		if (dateFrom.isAfter(dateTo)) {
			throw badRequest("dateFrom must be before or equal to dateTo");
		}
		if (ChronoUnit.DAYS.between(dateFrom, dateTo) + 1 > MAX_RANGE_DAYS) {
			throw badRequest("Date range must not exceed 31 days");
		}
		return new DateRange(dateFrom, dateTo);
	}

	private ApplicationException notFound() {
		return new ApplicationException(HttpStatus.NOT_FOUND, "Maintenance order not found");
	}

	private ApplicationException badRequest(String message) {
		return new ApplicationException(HttpStatus.BAD_REQUEST, message);
	}

	private record DateRange(LocalDate dateFrom, LocalDate dateTo) {
	}
}
