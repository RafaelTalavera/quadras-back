package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateLaundryOpeningStockDto;
import com.axioma.quadras.domain.dto.CreateLaundryStockAdjustmentDto;
import com.axioma.quadras.domain.dto.LaundryOpeningStockDto;
import com.axioma.quadras.domain.dto.LaundryOpeningStockLineInputDto;
import com.axioma.quadras.domain.dto.LaundryStockAdjustmentDto;
import com.axioma.quadras.domain.dto.LaundryStockDashboardDto;
import com.axioma.quadras.domain.dto.LaundryStockDashboardItemDto;
import com.axioma.quadras.domain.dto.LaundryStockPageDto;
import com.axioma.quadras.domain.dto.LaundryStockProviderTotalsDto;
import com.axioma.quadras.domain.dto.LaundryStockRowDto;
import com.axioma.quadras.domain.dto.LaundryStockTotalsDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryOpeningStock;
import com.axioma.quadras.domain.model.LaundryOpeningStockLine;
import com.axioma.quadras.domain.model.LaundryOpeningStockStatus;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryStockAdjustment;
import com.axioma.quadras.domain.model.LaundryStockBalance;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockDocumentType;
import com.axioma.quadras.domain.model.LaundryStockLot;
import com.axioma.quadras.domain.model.LaundryStockMovement;
import com.axioma.quadras.domain.model.LaundryStockMovementType;
import com.axioma.quadras.domain.model.LaundryStockOperation;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryOpeningStockRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryStockAdjustmentRepository;
import com.axioma.quadras.repository.LaundryStockBalanceRepository;
import com.axioma.quadras.repository.LaundryStockLotRepository;
import com.axioma.quadras.repository.LaundryStockMovementRepository;
import com.axioma.quadras.repository.projection.LaundryStockAgingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaundryStockService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;
	private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);
	private static final DateTimeFormatter REFERENCE_DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private static final String OPENING_PATH = "/api/v2/laundry/opening-stock";
	private static final String ADJUSTMENT_PATH = "/api/v2/laundry/stock-adjustments";
	private static final String SUPERVISOR = "SUPERVISOR";

	private final LaundryStockBalanceRepository balanceRepository;
	private final LaundryStockLotRepository lotRepository;
	private final LaundryStockMovementRepository movementRepository;
	private final LaundryOpeningStockRepository openingStockRepository;
	private final LaundryStockAdjustmentRepository adjustmentRepository;
	private final LaundryItemRepository itemRepository;
	private final LaundryProviderRepository providerRepository;
	private final LaundryIdempotencyService idempotencyService;
	private final AuditTrailService auditTrailService;

	public LaundryStockService(
			LaundryStockBalanceRepository balanceRepository,
			LaundryStockLotRepository lotRepository,
			LaundryStockMovementRepository movementRepository,
			LaundryOpeningStockRepository openingStockRepository,
			LaundryStockAdjustmentRepository adjustmentRepository,
			LaundryItemRepository itemRepository,
			LaundryProviderRepository providerRepository,
			LaundryIdempotencyService idempotencyService,
			AuditTrailService auditTrailService
	) {
		this.balanceRepository = balanceRepository;
		this.lotRepository = lotRepository;
		this.movementRepository = movementRepository;
		this.openingStockRepository = openingStockRepository;
		this.adjustmentRepository = adjustmentRepository;
		this.itemRepository = itemRepository;
		this.providerRepository = providerRepository;
		this.idempotencyService = idempotencyService;
		this.auditTrailService = auditTrailService;
	}

	public LaundryStockDashboardDto dashboard(Long providerId) {
		final LaundryProvider filterProvider = providerId == null ? null : requireProvider(providerId);
		final OffsetDateTime now = OffsetDateTime.now();
		final Map<String, OffsetDateTime> aging = agingByKey();
		final Map<Long, ItemAggregate> items = new LinkedHashMap<>();
		itemRepository.findAllByActiveTrueOrderByNameAsc()
				.forEach(item -> items.put(item.getId(), new ItemAggregate(item)));
		final Map<Long, ProviderAggregate> providers = new LinkedHashMap<>();
		final BucketTotals totals = new BucketTotals();

		for (LaundryStockBalance balance : balanceRepository.findAllWithItemAndProvider()) {
			if (filterProvider != null
					&& (balance.getProvider() == null
					|| !balance.getProvider().getId().equals(filterProvider.getId()))) {
				continue;
			}
			final ItemAggregate item = items.computeIfAbsent(
					balance.getItem().getId(),
					id -> new ItemAggregate(balance.getItem())
			);
			item.totals.add(balance.getBucket(), balance.getQuantity());
			totals.add(balance.getBucket(), balance.getQuantity());
			final OffsetDateTime oldest = aging.get(agingKey(balance));
			if (oldest != null) {
				item.observeOldest(balance.getBucket(), oldest);
			}
			if (balance.getProvider() != null) {
				final ProviderAggregate provider = providers.computeIfAbsent(
						balance.getProvider().getId(),
						id -> new ProviderAggregate(balance.getProvider())
				);
				provider.totals.add(balance.getBucket(), balance.getQuantity());
			}
		}

		final List<LaundryStockDashboardItemDto> itemDtos = items.values().stream()
				.filter(item -> item.totals.total().signum() > 0)
				.sorted((left, right) -> left.item.getName().compareToIgnoreCase(right.item.getName()))
				.map(ItemAggregate::toDto)
				.toList();
		final List<LaundryStockProviderTotalsDto> providerDtos = providers.values().stream()
				.filter(provider -> provider.totals.total().signum() > 0)
				.sorted((left, right) -> left.provider.getName().compareToIgnoreCase(right.provider.getName()))
				.map(ProviderAggregate::toDto)
				.toList();

		return new LaundryStockDashboardDto(
				now,
				providerId,
				totals.toDto(),
				itemDtos,
				providerDtos
		);
	}

	public LaundryStockPageDto stock(
			Long itemId,
			LaundryStockBucket bucket,
			Long providerId,
			Integer page,
			Integer size
	) {
		final int safePage = normalizePage(page);
		final int safeSize = normalizePageSize(size);
		if (bucket != null && !bucket.requiresProvider() && providerId != null) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"providerId is not allowed for hotel buckets"
			);
		}
		if (providerId != null) {
			requireProvider(providerId);
		}

		final Page<LaundryStockBalance> result = balanceRepository.search(
				itemId,
				bucket,
				providerId,
				PageRequest.of(safePage, safeSize)
		);
		final Map<String, OffsetDateTime> aging = agingByKey();
		final OffsetDateTime now = OffsetDateTime.now();
		final List<LaundryStockRowDto> rows = result.getContent().stream()
				.map(balance -> {
					final OffsetDateTime oldest = aging.get(agingKey(balance));
					return LaundryStockRowDto.from(balance, oldest, ageDays(oldest, now));
				})
				.toList();
		return new LaundryStockPageDto(safePage, safeSize, result.hasNext(), rows);
	}

	@Transactional
	public IdempotentResult<LaundryOpeningStockDto> createOpeningStock(
			CreateLaundryOpeningStockDto input,
			String actorUsername,
			String actorRole,
			String idempotencyKey
	) {
		requireSupervisor(actorRole);
		final String fingerprint = idempotencyService.fingerprint("POST", OPENING_PATH, input);
		return idempotencyService.execute(
				actorUsername,
				idempotencyKey,
				fingerprint,
				LaundryOpeningStockDto.class,
				201,
				() -> approveOpeningStock(input, actorUsername, actorRole)
		);
	}

	@Transactional
	public IdempotentResult<LaundryStockAdjustmentDto> createAdjustment(
			CreateLaundryStockAdjustmentDto input,
			String actorUsername,
			String actorRole,
			String idempotencyKey
	) {
		requireSupervisor(actorRole);
		final String fingerprint = idempotencyService.fingerprint("POST", ADJUSTMENT_PATH, input);
		return idempotencyService.execute(
				actorUsername,
				idempotencyKey,
				fingerprint,
				LaundryStockAdjustmentDto.class,
				201,
				() -> applyAdjustment(input, actorUsername, actorRole)
		);
	}

	private LaundryOpeningStockDto approveOpeningStock(
			CreateLaundryOpeningStockDto input,
			String actorUsername,
			String actorRole
	) {
		if (input.version() == null || input.version() != 0L) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "version must be 0 for opening stock");
		}
		validateEffectiveAt(input.effectiveAt());
		if (openingStockRepository.existsByStatus(LaundryOpeningStockStatus.APPROVED)) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Opening stock is already approved");
		}

		final Set<Long> itemIds = new HashSet<>();
		final List<ValidatedOpeningLine> lines = new ArrayList<>();
		boolean hasPositiveQuantity = false;
		for (LaundryOpeningStockLineInputDto line : input.lines()) {
			if (!itemIds.add(line.itemId())) {
				throw new ApplicationException(
						HttpStatus.BAD_REQUEST,
						"Opening stock cannot contain duplicate items"
				);
			}
			final LaundryItem item = itemRepository.findById(line.itemId())
					.orElseThrow(() -> new ApplicationException(
							HttpStatus.BAD_REQUEST,
							"Laundry item not found: " + line.itemId()
					));
			if (!item.isActive()) {
				throw new ApplicationException(
						HttpStatus.BAD_REQUEST,
						"Inactive laundry item cannot be included: " + item.getId()
				);
			}
			final BigDecimal quantity;
			try {
				quantity = LaundryStockBalance.normalize(line.quantity());
			} catch (IllegalArgumentException ex) {
				throw new ApplicationException(HttpStatus.BAD_REQUEST, ex.getMessage());
			}
			if (quantity.signum() < 0) {
				throw new ApplicationException(HttpStatus.BAD_REQUEST, "quantity cannot be negative");
			}
			hasPositiveQuantity = hasPositiveQuantity || quantity.signum() > 0;
			lines.add(new ValidatedOpeningLine(item, quantity));
		}
		if (!hasPositiveQuantity) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Opening stock must contain at least one positive quantity"
			);
		}

		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		final String correlationId = UUID.randomUUID().toString();
		final LaundryOpeningStock opening = LaundryOpeningStock.create(
				reference(),
				actorUsername,
				now,
				input.effectiveAt(),
				input.notes(),
				correlationId
		);
		lines.forEach(line -> opening.addLine(line.item(), line.quantity()));
		final LaundryOpeningStock saved = openingStockRepository.saveAndFlush(opening);
		BigDecimal totalQuantity = zero();
		for (LaundryOpeningStockLine line : saved.getLines()) {
			if (line.getQuantity().signum() == 0) {
				continue;
			}
			applyIncoming(
					line.getItem(),
					LaundryStockBucket.HOTEL_AVAILABLE,
					null,
					line.getQuantity(),
					saved.getEffectiveAt(),
					LaundryStockMovementType.OPENING,
					LaundryStockDocumentType.OPENING_STOCK,
					saved.getId(),
					line.getId(),
					actorUsername,
					actorRole,
					"Opening stock",
					correlationId
			);
			totalQuantity = totalQuantity.add(line.getQuantity());
		}

		final LaundryOpeningStockDto result = LaundryOpeningStockDto.from(saved);
		final Map<String, Object> changes = new LinkedHashMap<>();
		changes.put("correlationId", correlationId);
		changes.put("lineCount", saved.getLines().size());
		changes.put("totalQuantity", totalQuantity);
		auditTrailService.record(
				"LAUNDRY",
				"LAUNDRY_OPENING_STOCK",
				saved.getId(),
				"APPROVE",
				"Opening stock approved",
				changes,
				null,
				openingSnapshot(result)
		);
		return result;
	}

	private Map<String, Object> openingSnapshot(LaundryOpeningStockDto opening) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", opening.id());
		snapshot.put("reference", opening.reference());
		snapshot.put("status", opening.status());
		snapshot.put("approvedBy", opening.approvedBy());
		snapshot.put("approvedAt", opening.approvedAt() == null ? null : opening.approvedAt().toString());
		snapshot.put("effectiveAt", opening.effectiveAt() == null ? null : opening.effectiveAt().toString());
		snapshot.put("notes", opening.notes());
		snapshot.put("correlationId", opening.correlationId());
		snapshot.put("lines", opening.lines().stream().map(line -> {
			final Map<String, Object> lineSnapshot = new LinkedHashMap<>();
			lineSnapshot.put("itemId", line.itemId());
			lineSnapshot.put("itemName", line.itemName());
			lineSnapshot.put("unitType", line.unitType());
			lineSnapshot.put("quantity", line.quantity());
			return lineSnapshot;
		}).toList());
		return snapshot;
	}

	private LaundryStockAdjustmentDto applyAdjustment(
			CreateLaundryStockAdjustmentDto input,
			String actorUsername,
			String actorRole
	) {
		validateEffectiveAt(input.effectiveAt());
		if (input.version() == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "version is required");
		}
		final LaundryItem item = itemRepository.findById(input.itemId())
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Laundry item not found: " + input.itemId()
				));
		final LaundryProvider provider = input.providerId() == null
				? null
				: requireProvider(input.providerId());
		final LaundryStockBucket fromBucket = input.fromBucket();
		final LaundryStockBucket toBucket = input.toBucket();
		validateAdjustmentShape(input.operation(), fromBucket, toBucket, provider);
		if (input.operation() == LaundryStockOperation.INCREASE && input.version() != 0L) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"version must be 0 for INCREASE adjustments"
			);
		}

		final BigDecimal quantity;
		try {
			quantity = LaundryStockBalance.normalize(input.quantity());
		} catch (IllegalArgumentException ex) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, ex.getMessage());
		}
		if (quantity.signum() <= 0) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "quantity must be positive");
		}

		if (input.operation() == LaundryStockOperation.DECREASE
				|| input.operation() == LaundryStockOperation.TRANSFER) {
			final LaundryStockBalance source = balanceRepository
					.findForUpdate(item.getId(), fromBucket, providerScope(provider))
					.orElseThrow(() -> insufficient(fromBucket, item.getId(), zero(), quantity));
			if (source.getVersion() != input.version()) {
				throw new ApplicationException(
						HttpStatus.CONFLICT,
						"Stock balance version mismatch: expected " + input.version()
								+ ", current " + source.getVersion()
				);
			}
			if (source.getQuantity().compareTo(quantity) < 0) {
				throw insufficient(fromBucket, item.getId(), source.getQuantity(), quantity);
			}
		}

		final String correlationId = UUID.randomUUID().toString();
		final LaundryStockAdjustment adjustment = adjustmentRepository.saveAndFlush(
				LaundryStockAdjustment.create(
						item,
						input.operation(),
						fromBucket,
						toBucket,
						provider,
						quantity,
						input.reason(),
						input.effectiveAt(),
						actorUsername,
						actorRole,
						correlationId
				)
		);

		switch (input.operation()) {
			case INCREASE -> applyIncoming(
					item,
					toBucket,
					toBucket.requiresProvider() ? provider : null,
					quantity,
					input.effectiveAt(),
					LaundryStockMovementType.ADJUSTMENT_INCREASE,
					LaundryStockDocumentType.STOCK_ADJUSTMENT,
					adjustment.getId(),
					null,
					actorUsername,
					actorRole,
					input.reason(),
					correlationId
			);
			case DECREASE -> applyOutgoing(
					item,
					fromBucket,
					fromBucket.requiresProvider() ? provider : null,
					quantity,
					input.effectiveAt(),
					LaundryStockMovementType.ADJUSTMENT_DECREASE,
					LaundryStockDocumentType.STOCK_ADJUSTMENT,
					adjustment.getId(),
					null,
					actorUsername,
					actorRole,
					input.reason(),
					correlationId
			);
			case TRANSFER -> applyTransfer(
					item,
					fromBucket,
					fromBucket.requiresProvider() ? provider : null,
					toBucket,
					toBucket.requiresProvider() ? provider : null,
					quantity,
					input.effectiveAt(),
					LaundryStockDocumentType.STOCK_ADJUSTMENT,
					adjustment.getId(),
					null,
					actorUsername,
					actorRole,
					input.reason(),
					correlationId
			);
		}

		final LaundryStockAdjustmentDto result = LaundryStockAdjustmentDto.from(adjustment);
		final Map<String, Object> changes = new LinkedHashMap<>();
		changes.put("correlationId", correlationId);
		changes.put("operation", input.operation());
		changes.put("fromBucket", fromBucket);
		changes.put("toBucket", toBucket);
		changes.put("providerId", input.providerId());
		changes.put("quantity", quantity);
		auditTrailService.record(
				"LAUNDRY",
				"LAUNDRY_STOCK_ADJUSTMENT",
				adjustment.getId(),
				"APPLY",
				"Stock adjustment applied: " + input.operation(),
				changes,
				null,
				adjustmentSnapshot(result)
		);
		return result;
	}

	private Map<String, Object> adjustmentSnapshot(LaundryStockAdjustmentDto adjustment) {
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("id", adjustment.id());
		snapshot.put("itemId", adjustment.itemId());
		snapshot.put("itemName", adjustment.itemName());
		snapshot.put("operation", adjustment.operation());
		snapshot.put("fromBucket", adjustment.fromBucket());
		snapshot.put("toBucket", adjustment.toBucket());
		snapshot.put("providerId", adjustment.providerId());
		snapshot.put("quantity", adjustment.quantity());
		snapshot.put("reason", adjustment.reason());
		snapshot.put("effectiveAt", adjustment.effectiveAt() == null ? null : adjustment.effectiveAt().toString());
		snapshot.put("actorUsername", adjustment.actorUsername());
		snapshot.put("actorRole", adjustment.actorRole());
		snapshot.put("correlationId", adjustment.correlationId());
		snapshot.put("createdAt", adjustment.createdAt() == null ? null : adjustment.createdAt().toString());
		return snapshot;
	}

	private void validateAdjustmentShape(
			LaundryStockOperation operation,
			LaundryStockBucket fromBucket,
			LaundryStockBucket toBucket,
			LaundryProvider provider
	) {
		if (operation == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "operation is required");
		}
		switch (operation) {
			case INCREASE -> {
				if (fromBucket != null || toBucket == null) {
					throw new ApplicationException(
							HttpStatus.BAD_REQUEST,
							"INCREASE requires fromBucket = null and toBucket"
					);
				}
			}
			case DECREASE -> {
				if (fromBucket == null || toBucket != null) {
					throw new ApplicationException(
							HttpStatus.BAD_REQUEST,
							"DECREASE requires fromBucket and toBucket = null"
					);
				}
			}
			case TRANSFER -> {
				if (fromBucket == null || toBucket == null || fromBucket == toBucket) {
					throw new ApplicationException(
							HttpStatus.BAD_REQUEST,
							"TRANSFER requires two different buckets"
					);
				}
			}
		}

		final boolean requiresProvider =
				(fromBucket != null && fromBucket.requiresProvider())
				|| (toBucket != null && toBucket.requiresProvider());
		if (requiresProvider && provider == null) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"providerId is required for provider buckets"
			);
		}
		if (!requiresProvider && provider != null) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"providerId is not allowed when both buckets are hotel buckets"
			);
		}
	}

	private void applyIncoming(
			LaundryItem item,
			LaundryStockBucket bucket,
			LaundryProvider provider,
			BigDecimal quantity,
			OffsetDateTime effectiveAt,
			LaundryStockMovementType movementType,
			LaundryStockDocumentType documentType,
			Long documentId,
			Long documentLineId,
			String actorUsername,
			String actorRole,
			String reason,
			String correlationId
	) {
		final LaundryStockBalance balance = lockOrCreateBalance(item, bucket, provider);
		balance.add(quantity);
		balanceRepository.save(balance);
		final LaundryStockMovement movement = movementRepository.saveAndFlush(
				movement(
						item,
						provider,
						movementType,
						null,
						bucket,
						quantity,
						effectiveAt,
						documentType,
						documentId,
						documentLineId,
						actorUsername,
						actorRole,
						reason,
						correlationId
				)
		);
		lotRepository.save(
				LaundryStockLot.create(item, bucket, provider, quantity, effectiveAt, movement.getId())
		);
	}

	private void applyOutgoing(
			LaundryItem item,
			LaundryStockBucket bucket,
			LaundryProvider provider,
			BigDecimal quantity,
			OffsetDateTime effectiveAt,
			LaundryStockMovementType movementType,
			LaundryStockDocumentType documentType,
			Long documentId,
			Long documentLineId,
			String actorUsername,
			String actorRole,
			String reason,
			String correlationId
	) {
		final LaundryStockBalance source = balanceRepository
				.findForUpdate(item.getId(), bucket, providerScope(provider))
				.orElseThrow(() -> insufficient(bucket, item.getId(), zero(), quantity));
		if (source.getQuantity().compareTo(quantity) < 0) {
			throw insufficient(bucket, item.getId(), source.getQuantity(), quantity);
		}
		source.subtract(quantity);
		balanceRepository.save(source);
		consumeLots(item, bucket, providerScope(provider), quantity);
		movementRepository.save(
				movement(
						item,
						provider,
						movementType,
						bucket,
						null,
						quantity,
						effectiveAt,
						documentType,
						documentId,
						documentLineId,
						actorUsername,
						actorRole,
						reason,
						correlationId
				)
		);
	}

	private void applyTransfer(
			LaundryItem item,
			LaundryStockBucket fromBucket,
			LaundryProvider fromProvider,
			LaundryStockBucket toBucket,
			LaundryProvider toProvider,
			BigDecimal quantity,
			OffsetDateTime effectiveAt,
			LaundryStockDocumentType documentType,
			Long documentId,
			Long documentLineId,
			String actorUsername,
			String actorRole,
			String reason,
			String correlationId
	) {
		final LaundryStockBalance source = balanceRepository
				.findForUpdate(item.getId(), fromBucket, providerScope(fromProvider))
				.orElseThrow(() -> insufficient(fromBucket, item.getId(), zero(), quantity));
		if (source.getQuantity().compareTo(quantity) < 0) {
			throw insufficient(fromBucket, item.getId(), source.getQuantity(), quantity);
		}
		final LaundryStockBalance target = lockOrCreateBalance(item, toBucket, toProvider);
		source.subtract(quantity);
		target.add(quantity);
		balanceRepository.save(source);
		balanceRepository.save(target);
		final List<LotChunk> chunks = consumeLots(
				item,
				fromBucket,
				providerScope(fromProvider),
				quantity
		);
		final LaundryProvider movementProvider = fromProvider != null ? fromProvider : toProvider;
		final LaundryStockMovement movement = movementRepository.saveAndFlush(
				movement(
						item,
						movementProvider,
						LaundryStockMovementType.TRANSFER,
						fromBucket,
						toBucket,
						quantity,
						effectiveAt,
						documentType,
						documentId,
						documentLineId,
						actorUsername,
						actorRole,
						reason,
						correlationId
				)
		);
		for (LotChunk chunk : chunks) {
			lotRepository.save(
					LaundryStockLot.create(
							item,
							toBucket,
							toProvider,
							chunk.quantity(),
							chunk.effectiveAt(),
							movement.getId()
					)
			);
		}
	}

	private LaundryStockBalance lockOrCreateBalance(
			LaundryItem item,
			LaundryStockBucket bucket,
			LaundryProvider provider
	) {
		final long scope = providerScope(provider);
		final var existing = balanceRepository.findForUpdate(item.getId(), bucket, scope);
		if (existing.isPresent()) {
			return existing.get();
		}
		try {
			return balanceRepository.saveAndFlush(
					LaundryStockBalance.create(item, bucket, provider)
			);
		} catch (DataIntegrityViolationException ex) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Concurrent stock update detected; reload and retry"
			);
		}
	}

	private List<LotChunk> consumeLots(
			LaundryItem item,
			LaundryStockBucket bucket,
			long providerScope,
			BigDecimal quantity
	) {
		final List<LotChunk> chunks = new ArrayList<>();
		BigDecimal remaining = quantity;
		for (LaundryStockLot lot : lotRepository.findOpenForUpdate(item.getId(), bucket, providerScope)) {
			if (remaining.signum() == 0) {
				break;
			}
			final BigDecimal take = lot.getRemainingQuantity().min(remaining);
			lot.consume(take);
			lotRepository.save(lot);
			chunks.add(new LotChunk(lot.getEffectiveAt(), take));
			remaining = remaining.subtract(take);
		}
		if (remaining.signum() > 0) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Stock lots are inconsistent with the balance; reconciliation is required"
			);
		}
		return chunks;
	}

	private LaundryStockMovement movement(
			LaundryItem item,
			LaundryProvider provider,
			LaundryStockMovementType movementType,
			LaundryStockBucket fromBucket,
			LaundryStockBucket toBucket,
			BigDecimal quantity,
			OffsetDateTime effectiveAt,
			LaundryStockDocumentType documentType,
			Long documentId,
			Long documentLineId,
			String actorUsername,
			String actorRole,
			String reason,
			String correlationId
	) {
		return LaundryStockMovement.create(
				item,
				provider,
				providerScope(provider),
				movementType,
				fromBucket,
				toBucket,
				quantity,
				effectiveAt,
				documentType,
				documentId,
				documentLineId,
				actorUsername,
				actorRole,
				reason,
				correlationId
		);
	}

	private ApplicationException insufficient(
			LaundryStockBucket bucket,
			Long itemId,
			BigDecimal available,
			BigDecimal requested
	) {
		return new ApplicationException(
				HttpStatus.CONFLICT,
				"Insufficient " + bucket + " balance for item " + itemId
						+ ": available " + available.setScale(3, RoundingMode.HALF_UP).toPlainString()
						+ ", requested " + requested.setScale(3, RoundingMode.HALF_UP).toPlainString()
		);
	}

	private LaundryProvider requireProvider(Long providerId) {
		return providerRepository.findById(providerId)
				.orElseThrow(() -> new ApplicationException(
						HttpStatus.NOT_FOUND,
						"Laundry provider not found: " + providerId
				));
	}

	private void requireSupervisor(String actorRole) {
		if (!SUPERVISOR.equals(actorRole)) {
			throw new ApplicationException(
					HttpStatus.FORBIDDEN,
					"Only supervisors can change laundry stock"
			);
		}
	}

	private void validateEffectiveAt(OffsetDateTime effectiveAt) {
		if (effectiveAt == null) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "effectiveAt is required");
		}
		if (effectiveAt.isAfter(OffsetDateTime.now().plus(MAX_FUTURE_SKEW))) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"effectiveAt cannot be more than 5 minutes in the future"
			);
		}
	}

	private Map<String, OffsetDateTime> agingByKey() {
		final Map<String, OffsetDateTime> aging = new HashMap<>();
		for (LaundryStockAgingView view : lotRepository.findAging()) {
			aging.put(
					agingKey(view.getItemId(), view.getBucket(), view.getProviderScope()),
					view.getOldestEffectiveAt()
			);
		}
		return aging;
	}

	private String agingKey(LaundryStockBalance balance) {
		return agingKey(balance.getItem().getId(), balance.getBucket(), balance.getProviderScope());
	}

	private String agingKey(Long itemId, LaundryStockBucket bucket, long providerScope) {
		return itemId + ":" + bucket + ":" + providerScope;
	}

	private long ageDays(OffsetDateTime oldest, OffsetDateTime now) {
		if (oldest == null) {
			return 0L;
		}
		return Math.max(0L, ChronoUnit.DAYS.between(oldest, now));
	}

	private long providerScope(LaundryProvider provider) {
		return provider == null ? 0L : provider.getId();
	}

	private int normalizePage(Integer page) {
		if (page == null) {
			return 0;
		}
		if (page < 0) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"page must be greater than or equal to zero"
			);
		}
		return page;
	}

	private int normalizePageSize(Integer size) {
		if (size == null) {
			return DEFAULT_PAGE_SIZE;
		}
		if (size < 1) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "size must be greater than zero");
		}
		if (size > MAX_PAGE_SIZE) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"size must be less than or equal to " + MAX_PAGE_SIZE
			);
		}
		return size;
	}

	private String reference() {
		final String suffix = UUID.randomUUID()
				.toString()
				.replace("-", "")
				.substring(0, 6)
				.toUpperCase(Locale.ROOT);
		return "OPEN-" + LocalDate.now().format(REFERENCE_DATE) + "-" + suffix;
	}

	private static BigDecimal zero() {
		return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	}

	private record ValidatedOpeningLine(LaundryItem item, BigDecimal quantity) {
	}

	private record LotChunk(OffsetDateTime effectiveAt, BigDecimal quantity) {
	}

	private static final class BucketTotals {
		private BigDecimal hotelAvailable = zero();
		private BigDecimal hotelRewashQueue = zero();
		private BigDecimal providerProcessing = zero();
		private BigDecimal providerRewash = zero();
		private BigDecimal missing = zero();
		private BigDecimal damaged = zero();

		void add(LaundryStockBucket bucket, BigDecimal quantity) {
			switch (bucket) {
				case HOTEL_AVAILABLE -> hotelAvailable = hotelAvailable.add(quantity);
				case HOTEL_REWASH_QUEUE -> hotelRewashQueue = hotelRewashQueue.add(quantity);
				case PROVIDER_PROCESSING -> providerProcessing = providerProcessing.add(quantity);
				case PROVIDER_REWASH -> providerRewash = providerRewash.add(quantity);
				case MISSING -> missing = missing.add(quantity);
				case DAMAGED -> damaged = damaged.add(quantity);
			}
		}

		BigDecimal total() {
			return hotelAvailable
					.add(hotelRewashQueue)
					.add(providerProcessing)
					.add(providerRewash)
					.add(missing)
					.add(damaged);
		}

		LaundryStockTotalsDto toDto() {
			return new LaundryStockTotalsDto(
					hotelAvailable,
					hotelRewashQueue,
					providerProcessing,
					providerRewash,
					missing,
					damaged,
					total()
			);
		}
	}

	private static final class ItemAggregate {
		private final LaundryItem item;
		private final BucketTotals totals = new BucketTotals();
		private final Map<LaundryStockBucket, OffsetDateTime> oldest =
				new EnumMap<>(LaundryStockBucket.class);

		private ItemAggregate(LaundryItem item) {
			this.item = item;
		}

		void observeOldest(LaundryStockBucket bucket, OffsetDateTime value) {
			oldest.merge(bucket, value, (left, right) -> left.isBefore(right) ? left : right);
		}

		LaundryStockDashboardItemDto toDto() {
			return new LaundryStockDashboardItemDto(
					item.getId(),
					item.getName(),
					item.getUnitType(),
					totals.hotelAvailable,
					totals.hotelRewashQueue,
					totals.providerProcessing,
					totals.providerRewash,
					totals.missing,
					totals.damaged,
					totals.total(),
					oldest.get(LaundryStockBucket.HOTEL_REWASH_QUEUE),
					oldest.get(LaundryStockBucket.PROVIDER_PROCESSING),
					oldest.get(LaundryStockBucket.PROVIDER_REWASH),
					oldest.get(LaundryStockBucket.MISSING),
					oldest.get(LaundryStockBucket.DAMAGED)
			);
		}
	}

	private static final class ProviderAggregate {
		private final LaundryProvider provider;
		private final BucketTotals totals = new BucketTotals();

		private ProviderAggregate(LaundryProvider provider) {
			this.provider = provider;
		}

		LaundryStockProviderTotalsDto toDto() {
			return new LaundryStockProviderTotalsDto(
					provider.getId(),
					provider.getName(),
					totals.providerProcessing,
					totals.providerRewash,
					totals.missing,
					totals.damaged,
					totals.total()
			);
		}
	}
}
