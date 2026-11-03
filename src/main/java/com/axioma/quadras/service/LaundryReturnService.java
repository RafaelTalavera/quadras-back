package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.ConfirmLaundryReturnDto;
import com.axioma.quadras.domain.dto.ConfirmLaundryReturnLineDto;
import com.axioma.quadras.domain.dto.CreateLaundryReturnDto;
import com.axioma.quadras.domain.dto.CreateLaundryReturnLineDto;
import com.axioma.quadras.domain.dto.LaundryBillableEventDto;
import com.axioma.quadras.domain.dto.LaundryBillablePageDto;
import com.axioma.quadras.domain.dto.LaundryReturnDto;
import com.axioma.quadras.domain.dto.ManualLaundryReturnDto;
import com.axioma.quadras.domain.dto.SubmitLaundryDocumentDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.LaundryBillableEvent;
import com.axioma.quadras.domain.model.LaundryBillableExportStatus;
import com.axioma.quadras.domain.model.LaundryItem;
import com.axioma.quadras.domain.model.LaundryObligation;
import com.axioma.quadras.domain.model.LaundryProvider;
import com.axioma.quadras.domain.model.LaundryReturnAllocation;
import com.axioma.quadras.domain.model.LaundryReturnManifest;
import com.axioma.quadras.domain.model.LaundryReturnManifestLine;
import com.axioma.quadras.domain.model.LaundryReturnOutcome;
import com.axioma.quadras.domain.model.LaundryReturnStatus;
import com.axioma.quadras.domain.model.LaundryStockBucket;
import com.axioma.quadras.domain.model.LaundryStockDocumentType;
import com.axioma.quadras.repository.LaundryBillableEventRepository;
import com.axioma.quadras.repository.LaundryItemRepository;
import com.axioma.quadras.repository.LaundryObligationRepository;
import com.axioma.quadras.repository.LaundryProviderRepository;
import com.axioma.quadras.repository.LaundryReturnAllocationRepository;
import com.axioma.quadras.repository.LaundryReturnManifestRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaundryReturnService {
	private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);
	private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private final LaundryReturnManifestRepository manifestRepository;
	private final LaundryReturnAllocationRepository allocationRepository;
	private final LaundryBillableEventRepository billableRepository;
	private final LaundryObligationRepository obligationRepository;
	private final LaundryProviderRepository providerRepository;
	private final LaundryItemRepository itemRepository;
	private final LaundryStockService stockService;
	private final LaundryIdempotencyService idempotencyService;

	public LaundryReturnService(LaundryReturnManifestRepository manifestRepository,
			LaundryReturnAllocationRepository allocationRepository,
			LaundryBillableEventRepository billableRepository,
			LaundryObligationRepository obligationRepository,
			LaundryProviderRepository providerRepository, LaundryItemRepository itemRepository,
			LaundryStockService stockService, LaundryIdempotencyService idempotencyService) {
		this.manifestRepository = manifestRepository;
		this.allocationRepository = allocationRepository;
		this.billableRepository = billableRepository;
		this.obligationRepository = obligationRepository;
		this.providerRepository = providerRepository;
		this.itemRepository = itemRepository;
		this.stockService = stockService;
		this.idempotencyService = idempotencyService;
	}

	@Transactional
	public IdempotentResult<LaundryReturnDto> create(CreateLaundryReturnDto input,
			String actor, String key) {
		String path = "/api/v2/laundry/returns";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryReturnDto.class, 201,
				() -> createDraft(input, actor));
	}

	@Transactional
	public IdempotentResult<LaundryReturnDto> submit(Long id, SubmitLaundryDocumentDto input,
			String actor, String key) {
		String path = "/api/v2/laundry/returns/" + id + "/submit";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryReturnDto.class, 200,
				() -> submitManifest(id, input.version(), actor));
	}

	@Transactional
	public IdempotentResult<LaundryReturnDto> confirm(Long id, ConfirmLaundryReturnDto input,
			String actor, String actorRole, String key) {
		String path = "/api/v2/laundry/returns/" + id + "/confirm";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryReturnDto.class, 200,
				() -> confirmManifest(id, input, actor, actorRole, false));
	}

	@Transactional
	public IdempotentResult<LaundryReturnDto> createManual(ManualLaundryReturnDto input,
			String actor, String actorRole, String key) {
		String path = "/api/v2/laundry/returns/manual";
		String fingerprint = idempotencyService.fingerprint("POST", path, input);
		return idempotencyService.execute(actor, key, fingerprint, LaundryReturnDto.class, 201,
				() -> createAndConfirmManual(input, actor, actorRole));
	}

	public LaundryReturnDto get(Long id) {
		return LaundryReturnDto.from(manifestRepository.findById(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry return not found")));
	}

	public LaundryBillablePageDto billable(Long providerId, OffsetDateTime from, OffsetDateTime to,
			LaundryBillableExportStatus status, Integer page, Integer size, String actorRole) {
		if (!"SUPERVISOR".equals(actorRole))
			throw new ApplicationException(HttpStatus.FORBIDDEN, "Supervisor role required");
		if (providerId != null && !providerRepository.existsById(providerId))
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found");
		if (from != null && to != null && from.isAfter(to)) throw bad("from must not be after to");
		int safePage = page == null ? 0 : Math.max(0, page);
		int safeSize = size == null ? 20 : Math.max(1, Math.min(100, size));
		Page<LaundryBillableEvent> result = billableRepository.search(providerId, from, to,
				status, PageRequest.of(safePage, safeSize));
		return new LaundryBillablePageDto(safePage, safeSize, result.hasNext(),
				result.getContent().stream().map(LaundryBillableEventDto::from).toList());
	}

	private LaundryReturnDto createDraft(CreateLaundryReturnDto input, String actor) {
		if (input.version() != 0L) throw bad("version must be 0 for new returns");
		if (input.effectiveAt().isAfter(OffsetDateTime.now().plus(MAX_FUTURE_SKEW)))
			throw bad("effectiveAt cannot be more than 5 minutes in the future");
		LaundryProvider provider = providerRepository.findById(input.providerId())
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found"));
		Set<Long> itemIds = new HashSet<>();
		List<DeclaredLine> lines = new ArrayList<>();
		for (CreateLaundryReturnLineDto line : input.lines()) {
			if (!itemIds.add(line.itemId())) throw bad("Return cannot contain duplicate items");
			LaundryItem item = itemRepository.findById(line.itemId())
					.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry item not found: " + line.itemId()));
			BigDecimal clean = line.declaredClean().setScale(3);
			BigDecimal retained = line.declaredRetained().setScale(3);
			if (clean.add(retained).signum() <= 0) throw bad("Each return line must declare a positive quantity");
			lines.add(new DeclaredLine(item, clean, retained));
		}
		String correlationId = UUID.randomUUID().toString();
		LaundryReturnManifest manifest = LaundryReturnManifest.draft(reference(), provider,
				input.effectiveAt(), input.notes(), actor, correlationId);
		lines.forEach(line -> manifest.addLine(line.item(), line.clean(), line.retained()));
		return LaundryReturnDto.from(manifestRepository.saveAndFlush(manifest));
	}

	private LaundryReturnDto createAndConfirmManual(ManualLaundryReturnDto input,
			String actor, String actorRole) {
		CreateLaundryReturnDto declaration = new CreateLaundryReturnDto(input.version(),
				input.providerId(), input.effectiveAt(), input.notes(), input.lines().stream()
				.map(line -> new CreateLaundryReturnLineDto(line.itemId(), line.declaredClean(),
						line.declaredRetained())).toList());
		LaundryReturnDto created = createDraft(declaration, actor);
		ConfirmLaundryReturnDto decision = new ConfirmLaundryReturnDto(0L, input.correctionNotes(),
				input.lines().stream().map(line -> new ConfirmLaundryReturnLineDto(line.itemId(),
						line.acceptedClean(), line.returnedDirty(), line.retained(), line.missing(),
						line.damaged())).toList());
		return confirmManifest(created.id(), decision, actor, actorRole, true);
	}

	private LaundryReturnDto submitManifest(Long id, long version, String actor) {
		LaundryReturnManifest manifest = lock(id);
		checkVersion(manifest, version);
		if (manifest.getStatus() != LaundryReturnStatus.DRAFT)
			throw conflict("Only DRAFT returns can be submitted");
		manifest.submit(actor);
		return LaundryReturnDto.from(manifestRepository.saveAndFlush(manifest));
	}

	private LaundryReturnDto confirmManifest(Long id, ConfirmLaundryReturnDto input,
			String actor, String actorRole, boolean manual) {
		LaundryReturnManifest manifest = lock(id);
		checkVersion(manifest, input.version());
		if ((!manual && manifest.getStatus() != LaundryReturnStatus.SUBMITTED)
				|| (manual && manifest.getStatus() != LaundryReturnStatus.DRAFT))
			throw conflict("Return manifest is not confirmable");
		Map<Long, ConfirmLaundryReturnLineDto> decisions = decisionMap(input.lines());
		if (decisions.size() != manifest.getLines().size()) throw bad("A final decision is required for every return line");
		boolean corrected = false;
		for (LaundryReturnManifestLine line : manifest.getLines()) {
			ConfirmLaundryReturnLineDto decision = decisions.remove(line.getItem().getId());
			if (decision == null) throw bad("A final decision is required for item " + line.getItem().getId());
			BigDecimal total = total(decision);
			if (total.signum() <= 0) throw bad("Final quantity must be positive for item " + line.getItem().getId());
			BigDecimal declared = line.getDeclaredCleanQuantity().add(line.getDeclaredRetainedQuantity());
			corrected |= total.compareTo(declared) != 0
					|| decision.acceptedClean().compareTo(line.getDeclaredCleanQuantity()) != 0
					|| decision.retained().compareTo(line.getDeclaredRetainedQuantity()) != 0
					|| decision.returnedDirty().signum() > 0 || decision.missing().signum() > 0
					|| decision.damaged().signum() > 0;
			line.decide(decision.acceptedClean(), decision.returnedDirty(), decision.retained(),
					decision.missing(), decision.damaged());
			allocateLine(manifest, line, decision, actor, actorRole);
		}
		if (!decisions.isEmpty()) throw bad("Final decision contains items not declared in the return");
		if (corrected && (input.correctionNotes() == null || input.correctionNotes().isBlank()))
			throw bad("correctionNotes is required when final quantities differ from declaration");
		manifest.confirm(actor, input.correctionNotes(), manual);
		return LaundryReturnDto.from(manifestRepository.saveAndFlush(manifest));
	}

	private void allocateLine(LaundryReturnManifest manifest, LaundryReturnManifestLine line,
			ConfirmLaundryReturnLineDto decision, String actor, String actorRole) {
		List<LaundryObligation> obligations = obligationRepository.findOpenForUpdate(
				manifest.getProvider().getId(), line.getItem().getId());
		allocateOutcome(manifest, line, obligations, LaundryReturnOutcome.ACCEPTED_CLEAN,
				decision.acceptedClean(), actor, actorRole);
		allocateOutcome(manifest, line, obligations, LaundryReturnOutcome.RETURNED_DIRTY,
				decision.returnedDirty(), actor, actorRole);
		allocateOutcome(manifest, line, obligations, LaundryReturnOutcome.RETAINED,
				decision.retained(), actor, actorRole);
		allocateOutcome(manifest, line, obligations, LaundryReturnOutcome.MISSING,
				decision.missing(), actor, actorRole);
		allocateOutcome(manifest, line, obligations, LaundryReturnOutcome.DAMAGED,
				decision.damaged(), actor, actorRole);
	}

	private void allocateOutcome(LaundryReturnManifest manifest, LaundryReturnManifestLine line,
			List<LaundryObligation> obligations, LaundryReturnOutcome outcome, BigDecimal requested,
			String actor, String actorRole) {
		BigDecimal remaining = requested.setScale(3);
		for (LaundryObligation obligation : obligations) {
			remaining = allocateSource(manifest, line, obligation, outcome,
					LaundryStockBucket.PROVIDER_PROCESSING, remaining, actor, actorRole);
			remaining = allocateSource(manifest, line, obligation, outcome,
					LaundryStockBucket.PROVIDER_REWASH, remaining, actor, actorRole);
			if (remaining.signum() == 0) return;
		}
		if (remaining.signum() > 0) throw conflict("Return exceeds open FIFO balance for item " + line.getItem().getId());
	}

	private BigDecimal allocateSource(LaundryReturnManifest manifest, LaundryReturnManifestLine line,
			LaundryObligation obligation, LaundryReturnOutcome outcome, LaundryStockBucket source,
			BigDecimal remaining, String actor, String actorRole) {
		if (remaining.signum() == 0) return remaining;
		BigDecimal available = source == LaundryStockBucket.PROVIDER_REWASH
				? obligation.getInRewashQuantity() : obligation.processingQuantity();
		if (available.signum() <= 0) return remaining;
		BigDecimal take = available.min(remaining);
		LaundryReturnAllocation allocation = allocationRepository.saveAndFlush(
				LaundryReturnAllocation.create(line, obligation, outcome, source, take));
		moveStock(manifest, line, source, outcome, take, actor, actorRole);
		obligation.applyReturn(outcome, source, take);
		obligationRepository.save(obligation);
		if (outcome == LaundryReturnOutcome.ACCEPTED_CLEAN)
			billableRepository.save(LaundryBillableEvent.create(manifest, allocation, actor));
		return remaining.subtract(take);
	}

	private void moveStock(LaundryReturnManifest manifest, LaundryReturnManifestLine line,
			LaundryStockBucket source, LaundryReturnOutcome outcome, BigDecimal quantity,
			String actor, String actorRole) {
		LaundryStockBucket target = switch (outcome) {
			case ACCEPTED_CLEAN -> LaundryStockBucket.HOTEL_AVAILABLE;
			case RETURNED_DIRTY -> LaundryStockBucket.HOTEL_REWASH_QUEUE;
			case RETAINED -> LaundryStockBucket.PROVIDER_REWASH;
			case MISSING -> LaundryStockBucket.MISSING;
			case DAMAGED -> LaundryStockBucket.DAMAGED;
		};
		if (source == target) return;
		stockService.transferDocumentStock(line.getItem(), source, manifest.getProvider(), target,
				target == LaundryStockBucket.HOTEL_AVAILABLE ? null : manifest.getProvider(), quantity,
				manifest.getEffectiveAt(), LaundryStockDocumentType.RETURN, manifest.getId(), line.getId(),
				actor, actorRole, manifest.getCorrelationId());
	}

	private Map<Long, ConfirmLaundryReturnLineDto> decisionMap(List<ConfirmLaundryReturnLineDto> lines) {
		Map<Long, ConfirmLaundryReturnLineDto> result = new HashMap<>();
		for (ConfirmLaundryReturnLineDto line : lines)
			if (result.put(line.itemId(), line) != null) throw bad("Final decision cannot contain duplicate items");
		return result;
	}
	private BigDecimal total(ConfirmLaundryReturnLineDto line) {
		return line.acceptedClean().add(line.returnedDirty()).add(line.retained())
				.add(line.missing()).add(line.damaged());
	}
	private LaundryReturnManifest lock(Long id) {
		return manifestRepository.findForUpdate(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry return not found"));
	}
	private void checkVersion(LaundryReturnManifest manifest, long version) {
		if (manifest.getVersion() != version) throw conflict("Laundry return version mismatch: expected "
				+ version + ", current " + manifest.getVersion());
	}
	private String reference() {
		return "RET-" + OffsetDateTime.now(ZoneOffset.UTC).format(DATE) + "-"
				+ UUID.randomUUID().toString().substring(0, 6).toUpperCase();
	}
	private ApplicationException bad(String message) { return new ApplicationException(HttpStatus.BAD_REQUEST, message); }
	private ApplicationException conflict(String message) { return new ApplicationException(HttpStatus.CONFLICT, message); }
	private record DeclaredLine(LaundryItem item, BigDecimal clean, BigDecimal retained) { }
}
