package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.CreateLaundryDispatchDto;
import com.axioma.quadras.domain.dto.CreateLaundryDispatchLineDto;
import com.axioma.quadras.domain.dto.LaundryDocumentDto;
import com.axioma.quadras.domain.dto.LaundryDocumentPageDto;
import com.axioma.quadras.domain.dto.LaundryDocumentSummaryDto;
import com.axioma.quadras.domain.dto.AcknowledgeLaundryDocumentDto;
import com.axioma.quadras.domain.dto.ObserveLaundryDocumentDto;
import com.axioma.quadras.domain.dto.ProviderLaundryDocumentDto;
import com.axioma.quadras.domain.dto.ProviderLaundryDocumentPageDto;
import com.axioma.quadras.domain.dto.ProviderLaundryDocumentSummaryDto;
import com.axioma.quadras.domain.dto.ResolveLaundryDiscrepancyDto;
import com.axioma.quadras.domain.dto.SubmitLaundryDocumentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.LaundryDocument;
import com.axioma.quadras.domain.model.LaundryDocumentStatus;
import com.axioma.quadras.domain.model.LaundryDocumentType;
import com.axioma.quadras.domain.model.LaundryDocumentConfirmation;
import com.axioma.quadras.domain.model.LaundryDocumentConfirmationAction;
import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryObligation;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockDocumentType;
import com.axioma.quadras.repository.LaundryDocumentRepository;
import com.axioma.quadras.repository.LaundryDocumentConfirmationRepository;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryObligationRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryRewashDispatchAllocationRepository;
import com.axioma.quadras.repository.LaundryReturnAllocationRepository;
import com.axioma.quadras.domain.model.LaundryRewashDispatchAllocation;
import com.axioma.quadras.domain.model.LaundryReturnOutcome;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@Transactional(readOnly = true)
public class LaundryDispatchService {
	private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);
	private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private final LaundryDocumentRepository documentRepository;
	private final LaundryProviderRepository providerRepository;
	private final LaundryItemRepository itemRepository;
	private final LaundryObligationRepository obligationRepository;
	private final LaundryStockService stockService;
	private final LaundryRewashDispatchAllocationRepository rewashAllocationRepository;
	private final LaundryReturnAllocationRepository returnAllocationRepository;
	private final LaundryIdempotencyService idempotencyService;
	private final LaundryDocumentConfirmationRepository confirmationRepository;

	public LaundryDispatchService(LaundryDocumentRepository documentRepository,
			LaundryProviderRepository providerRepository, LaundryItemRepository itemRepository,
			LaundryObligationRepository obligationRepository, LaundryStockService stockService,
			LaundryIdempotencyService idempotencyService,
			LaundryRewashDispatchAllocationRepository rewashAllocationRepository,
			LaundryReturnAllocationRepository returnAllocationRepository,
			LaundryDocumentConfirmationRepository confirmationRepository) {
		this.documentRepository = documentRepository;
		this.providerRepository = providerRepository;
		this.itemRepository = itemRepository;
		this.obligationRepository = obligationRepository;
		this.stockService = stockService;
		this.idempotencyService = idempotencyService;
		this.rewashAllocationRepository = rewashAllocationRepository;
		this.returnAllocationRepository = returnAllocationRepository;
		this.confirmationRepository = confirmationRepository;
	}

	@Transactional
	public IdempotentResult<LaundryDocumentDto> create(CreateLaundryDispatchDto input,
			LaundryDocumentType type, String actor, String key) {
		String path = type == LaundryDocumentType.STANDARD_DISPATCH
				? "/api/v2/laundry/dispatches" : "/api/v2/laundry/rewash-dispatches";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryDocumentDto.class, 201,
				() -> createDraft(input, type, actor));
	}

	@Transactional
	public IdempotentResult<LaundryDocumentDto> submit(Long id, SubmitLaundryDocumentDto input,
			String actor, String actorRole, String key) {
		String path = "/api/v2/laundry/documents/" + id + "/submit";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryDocumentDto.class, 200,
				() -> submitDocument(id, input.version(), actor, actorRole));
	}

	public LaundryDocumentDto get(Long id) {
		return LaundryDocumentDto.from(documentRepository.findById(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry document not found")));
	}

	public ProviderLaundryDocumentDto getForProvider(Long id, Long providerId) {
		LaundryDocument document = ownedDocument(id, providerId, false);
		return providerDto(document);
	}

	public ProviderLaundryDocumentPageDto providerPage(Long providerId,
			LaundryDocumentStatus status, LaundryDocumentType type, Integer page, Integer size) {
		if (providerId == null) throw new ApplicationException(HttpStatus.FORBIDDEN,
				"Provider account is not associated with a laundry provider");
		int safePage = page == null ? 0 : Math.max(0, page);
		int safeSize = size == null ? 20 : Math.max(1, Math.min(100, size));
		Page<LaundryDocument> result = documentRepository.findProviderPage(providerId, status,
				type, PageRequest.of(safePage, safeSize));
		return new ProviderLaundryDocumentPageDto(safePage, safeSize, result.hasNext(),
				result.getContent().stream().map(ProviderLaundryDocumentSummaryDto::from).toList());
	}

	public LaundryDocumentPageDto page(Long providerId, LaundryDocumentStatus status,
			LaundryDocumentType type, Integer page, Integer size) {
		if (providerId != null && !providerRepository.existsById(providerId))
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found");
		int safePage = page == null ? 0 : Math.max(0, page);
		int safeSize = size == null ? 20 : Math.max(1, Math.min(100, size));
		Page<LaundryDocument> result = documentRepository.search(providerId, status, type,
				PageRequest.of(safePage, safeSize));
		return new LaundryDocumentPageDto(safePage, safeSize, result.hasNext(),
				result.getContent().stream().map(LaundryDocumentSummaryDto::from).toList());
	}

	@Transactional
	public IdempotentResult<ProviderLaundryDocumentDto> acknowledge(Long id,
			AcknowledgeLaundryDocumentDto input, Long providerId, String actor, String key) {
		String path = "/api/v2/laundry/dispatches/" + id + "/acknowledgements";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint,
				ProviderLaundryDocumentDto.class, 200,
				() -> providerDecision(id, input.version(), input.notes(), providerId, actor, true));
	}

	@Transactional
	public IdempotentResult<ProviderLaundryDocumentDto> observe(Long id,
			ObserveLaundryDocumentDto input, Long providerId, String actor, String key) {
		String path = "/api/v2/laundry/documents/" + id + "/observations";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint,
				ProviderLaundryDocumentDto.class, 200,
				() -> providerDecision(id, input.version(), input.notes(), providerId, actor, false));
	}

	@Transactional
	public IdempotentResult<LaundryDocumentDto> resolve(Long id,
			ResolveLaundryDiscrepancyDto input, String actor, String actorRole, String key) {
		String path = "/api/v2/laundry/discrepancies/" + id + "/resolve";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryDocumentDto.class, 200,
				() -> resolveDocument(id, input, actor, actorRole));
	}

	private ProviderLaundryDocumentDto providerDecision(Long id, long version, String notes,
			Long providerId, String actor, boolean confirmed) {
		LaundryDocument document = ownedDocument(id, providerId, true);
		checkVersion(document, version);
		if (document.getStatus() != LaundryDocumentStatus.SUBMITTED) {
			throw new ApplicationException(HttpStatus.CONFLICT,
					"Only SUBMITTED documents can receive a provider decision");
		}
		LaundryDocumentConfirmationAction action = confirmed
				? LaundryDocumentConfirmationAction.PROVIDER_CONFIRMED
				: LaundryDocumentConfirmationAction.PROVIDER_OBSERVED;
		confirmationRepository.save(LaundryDocumentConfirmation.create(document, action,
				notes, actor, "PROVIDER"));
		if (confirmed) document.confirmByProvider(); else document.observeByProvider();
		documentRepository.saveAndFlush(document);
		return providerDto(document);
	}

	private LaundryDocumentDto resolveDocument(Long id, ResolveLaundryDiscrepancyDto input,
			String actor, String actorRole) {
		LaundryDocument document = documentRepository.findForUpdate(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,
						"Laundry document not found"));
		checkVersion(document, input.version());
		if (document.getStatus() != LaundryDocumentStatus.OBSERVED) {
			throw new ApplicationException(HttpStatus.CONFLICT,
					"Only OBSERVED documents can be resolved");
		}
		confirmationRepository.save(LaundryDocumentConfirmation.create(document,
				LaundryDocumentConfirmationAction.HOTEL_RESOLVED, input.notes(), actor, actorRole));
		document.resolveByHotel();
		return LaundryDocumentDto.from(documentRepository.saveAndFlush(document));
	}

	private LaundryDocument ownedDocument(Long id, Long providerId, boolean lock) {
		if (providerId == null) {
			throw new ApplicationException(HttpStatus.FORBIDDEN,
					"Provider account is not associated with a laundry provider");
		}
		LaundryDocument document = (lock ? documentRepository.findForUpdate(id)
				: documentRepository.findById(id)).orElseThrow(() ->
				new ApplicationException(HttpStatus.NOT_FOUND, "Laundry document not found"));
		if (!document.getProvider().getId().equals(providerId)) {
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Laundry document not found");
		}
		return document;
	}

	private ProviderLaundryDocumentDto providerDto(LaundryDocument document) {
		return ProviderLaundryDocumentDto.from(document,
				confirmationRepository.findByDocumentIdOrderByCreatedAtAscIdAsc(document.getId()));
	}

	private void checkVersion(LaundryDocument document, long expectedVersion) {
		if (document.getVersion() != expectedVersion) {
			throw new ApplicationException(HttpStatus.CONFLICT,
					"Laundry document version mismatch: expected " + expectedVersion
							+ ", current " + document.getVersion());
		}
	}

	private LaundryDocumentDto createDraft(CreateLaundryDispatchDto input,
			LaundryDocumentType type, String actor) {
		if (input.version() != 0L) throw bad("version must be 0 for new documents");
		validateEffectiveAt(input.effectiveAt());
		LaundryProvider provider = providerRepository.findById(input.providerId())
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found"));
		if (!provider.isActive()) throw bad("Inactive laundry provider cannot receive dispatches");
		Set<Long> itemIds = new HashSet<>();
		List<ValidatedLine> lines = new ArrayList<>();
		for (CreateLaundryDispatchLineDto line : input.lines()) {
			if (!itemIds.add(line.itemId())) throw bad("Dispatch cannot contain duplicate items");
			LaundryItem item = itemRepository.findById(line.itemId())
					.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry item not found: " + line.itemId()));
			if (!item.isActive()) throw bad("Inactive laundry item cannot be dispatched: " + line.itemId());
			BigDecimal quantity = line.quantity().setScale(3);
			lines.add(new ValidatedLine(item, quantity));
		}
		String correlationId = UUID.randomUUID().toString();
		LaundryDocument document = LaundryDocument.draft(reference(type), type, provider,
				input.effectiveAt(), input.notes(), actor, correlationId);
		for (ValidatedLine line : lines) {
			document.addLine(line.item(), line.quantity(), type == LaundryDocumentType.STANDARD_DISPATCH
					? line.item().getCurrentUnitPrice() : BigDecimal.ZERO);
		}
		return LaundryDocumentDto.from(documentRepository.saveAndFlush(document));
	}

	private LaundryDocumentDto submitDocument(Long id, long expectedVersion,
			String actor, String actorRole) {
		LaundryDocument document = documentRepository.findForUpdate(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry document not found"));
		if (document.getVersion() != expectedVersion) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Laundry document version mismatch: expected "
					+ expectedVersion + ", current " + document.getVersion());
		}
		if (document.getStatus() != LaundryDocumentStatus.DRAFT) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Only DRAFT documents can be submitted");
		}
		for (var line : document.getLines()) {
			if (document.getDocumentType() == LaundryDocumentType.STANDARD_DISPATCH) {
				stockService.transferDocumentStock(line.getItem(), LaundryStockBucket.HOTEL_AVAILABLE,
						null, LaundryStockBucket.PROVIDER_PROCESSING, document.getProvider(),
						line.getQuantity(), document.getEffectiveAt(), LaundryStockDocumentType.DISPATCH,
						document.getId(), line.getId(), actor, actorRole, document.getCorrelationId());
				obligationRepository.save(LaundryObligation.create(document.getProvider(), line.getItem(),
						LaundryStockDocumentType.DISPATCH, document.getId(), line.getId(), line.getQuantity(),
						line.getUnitPrice(), document.getEffectiveAt()));
			} else {
				allocateRewash(document, line);
				stockService.transferDocumentStock(line.getItem(), LaundryStockBucket.HOTEL_REWASH_QUEUE,
						document.getProvider(), LaundryStockBucket.PROVIDER_REWASH, document.getProvider(),
						line.getQuantity(), document.getEffectiveAt(), LaundryStockDocumentType.REWASH_DISPATCH,
						document.getId(), line.getId(), actor, actorRole, document.getCorrelationId());
			}
		}
		document.submit(actor);
		return LaundryDocumentDto.from(documentRepository.saveAndFlush(document));
	}

	private void allocateRewash(LaundryDocument document,
			com.axioma.quadras.domain.model.LaundryDocumentLine line) {
		BigDecimal remaining = line.getQuantity();
		for (LaundryObligation obligation : obligationRepository.findRewashForUpdate(
				document.getProvider().getId(), line.getItem().getId())) {
			BigDecimal allocated = rewashAllocationRepository.sumAllocated(obligation.getId());
			BigDecimal returnedDirty = returnAllocationRepository.sumQuantity(obligation.getId(),
					LaundryReturnOutcome.RETURNED_DIRTY);
			BigDecimal dispatchable = returnedDirty.signum() > 0
					? returnedDirty : obligation.getInRewashQuantity();
			BigDecimal available = dispatchable.subtract(allocated);
			if (available.signum() <= 0) continue;
			BigDecimal take = available.min(remaining);
			rewashAllocationRepository.save(LaundryRewashDispatchAllocation.create(line, obligation, take));
			remaining = remaining.subtract(take);
			if (remaining.signum() == 0) return;
		}
		BigDecimal available = line.getQuantity().subtract(remaining);
		throw new ApplicationException(HttpStatus.CONFLICT,
				"No se puede enviar \"" + line.getItem().getName() + "\" a relavado. "
						+ "Cantidad solicitada: " + quantity(line.getQuantity()) + "; "
						+ "cantidad disponible para relavado: " + quantity(available) + ". "
						+ "Registre primero la recepción de las prendas como \"Para relavado\" "
						+ "o reduzca la cantidad.");
	}

	private String quantity(BigDecimal value) {
		return value.setScale(3).stripTrailingZeros().toPlainString();
	}

	private void validateEffectiveAt(OffsetDateTime value) {
		if (value.isAfter(OffsetDateTime.now().plus(MAX_FUTURE_SKEW)))
			throw bad("effectiveAt cannot be more than 5 minutes in the future");
	}

	private String reference(LaundryDocumentType type) {
		String prefix = type == LaundryDocumentType.STANDARD_DISPATCH ? "DSP" : "RWS";
		return prefix + "-" + OffsetDateTime.now(ZoneOffset.UTC).format(DATE) + "-"
				+ UUID.randomUUID().toString().substring(0, 6).toUpperCase();
	}

	private ApplicationException bad(String message) {
		return new ApplicationException(HttpStatus.BAD_REQUEST, message);
	}

	private record ValidatedLine(LaundryItem item, BigDecimal quantity) { }
}
