package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.*;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.*;
import com.axioma.quadras.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaundryService {
	private final LaundryProviderRepository providerRepository;
	private final LaundryItemRepository itemRepository;
	private final LaundryShipmentRepository shipmentRepository;
	private final AuditTrailService auditTrailService;

	public LaundryService(
			LaundryProviderRepository providerRepository,
			LaundryItemRepository itemRepository,
			LaundryShipmentRepository shipmentRepository,
			AuditTrailService auditTrailService
	) {
		this.providerRepository = providerRepository;
		this.itemRepository = itemRepository;
		this.shipmentRepository = shipmentRepository;
		this.auditTrailService = auditTrailService;
	}

	public List<LaundryProviderDto> providers() {
		return providerRepository.findAllByOrderByNameAsc().stream().map(LaundryProviderDto::from).toList();
	}

	@Transactional
	public LaundryProviderDto createProvider(CreateLaundryProviderDto input) {
		if (providerRepository.existsByNameIgnoreCase(input.name().trim())) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Laundry provider name already exists.");
		}
		LaundryProvider saved = providerRepository.save(LaundryProvider.create(input.name(), input.contact(), input.active()));
		auditTrailService.record("LAUNDRY", "LAUNDRY_PROVIDER", saved.getId(), "CREATE", "Laundry provider created", null, null, LaundryProviderDto.from(saved));
		return LaundryProviderDto.from(saved);
	}

	@Transactional
	public LaundryProviderDto updateProvider(Long id, UpdateLaundryProviderDto input) {
		LaundryProvider provider = providerRepository.findById(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found."));
		String normalizedName = input.name().trim();
		if (providerRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Laundry provider name already exists.");
		}
		LaundryProviderDto before = LaundryProviderDto.from(provider);
		provider.update(normalizedName, input.contact(), input.active());
		LaundryProviderDto result = LaundryProviderDto.from(providerRepository.save(provider));
		auditTrailService.record("LAUNDRY", "LAUNDRY_PROVIDER", id, "UPDATE",
				"Laundry provider updated", input, before, result);
		return result;
	}

	public List<LaundryItemDto> items() {
		return itemRepository.findAllByOrderByNameAsc().stream().map(LaundryItemDto::from).toList();
	}

	@Transactional
	public LaundryItemDto createItem(CreateLaundryItemDto input) {
		if (itemRepository.existsByNameIgnoreCase(input.name().trim())) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Laundry item name already exists.");
		}
		LaundryItem saved = itemRepository.save(LaundryItem.create(input.name(), input.unitType(), input.currentUnitPrice(), input.active()));
		auditTrailService.record("LAUNDRY", "LAUNDRY_ITEM", saved.getId(), "CREATE", "Laundry item created", null, null, LaundryItemDto.from(saved));
		return LaundryItemDto.from(saved);
	}

	@Transactional
	public LaundryItemDto updateItem(Long id, UpdateLaundryItemDto input) {
		LaundryItem item = itemRepository.findById(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry item not found."));
		String normalizedName = input.name().trim();
		if (itemRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Laundry item name already exists.");
		}
		LaundryItemDto before = LaundryItemDto.from(item);
		item.update(normalizedName, input.unitType(), input.currentUnitPrice(), input.active());
		LaundryItemDto result = LaundryItemDto.from(itemRepository.save(item));
		auditTrailService.record("LAUNDRY", "LAUNDRY_ITEM", id, "UPDATE", "Laundry item updated", input, before, result);
		return result;
	}

	@Transactional
	public LaundryShipmentDto createShipment(CreateLaundryShipmentDto input, String actor) {
		LaundryProvider provider = providerRepository.findById(input.providerId())
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry provider not found."));
		if (!provider.isActive()) throw new ApplicationException(HttpStatus.CONFLICT, "Inactive laundry provider cannot receive shipments.");
		Set<Long> itemIds = new HashSet<>();
		LaundryShipmentKind kind = input.kind() == null ? LaundryShipmentKind.STANDARD : input.kind();
		LaundryShipment shipment = kind == LaundryShipmentKind.RECEIPT
				? LaundryShipment.createReceipt(number(), provider, input.sentAt(), input.notes(), actor)
				: LaundryShipment.create(number(), provider, kind, input.sentAt(), input.notes(), actor);
		for (CreateLaundryShipmentLineDto lineInput : input.lines()) {
			if (!itemIds.add(lineInput.itemId())) throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipment cannot contain duplicate items.");
			LaundryItem item = itemRepository.findById(lineInput.itemId())
					.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry item not found: " + lineInput.itemId()));
			if (!item.isActive()) throw new ApplicationException(HttpStatus.CONFLICT, "Inactive laundry item cannot be sent: " + item.getName());
			shipment.addLine(switch (kind) {
				case REWASH -> LaundryShipmentLine.manualRewash(item, lineInput.quantity(), lineInput.rewashReason());
				case RECEIPT -> LaundryShipmentLine.receipt(item, lineInput.quantity());
				case STANDARD -> LaundryShipmentLine.standard(item, lineInput.quantity());
			});
		}
		LaundryShipment saved = shipmentRepository.save(shipment);
		LaundryShipmentDto result = LaundryShipmentDto.from(saved);
		auditTrailService.record("LAUNDRY", "LAUNDRY_SHIPMENT", saved.getId(),
				kind == LaundryShipmentKind.REWASH ? "CREATE_REWASH" : kind == LaundryShipmentKind.RECEIPT ? "CREATE_RECEIPT" : "CREATE",
				kind == LaundryShipmentKind.REWASH ? "Manual no-charge rewash created"
						: kind == LaundryShipmentKind.RECEIPT ? "Independent laundry receipt created" : "Laundry shipment created",
				null, null, auditSnapshot(result));
		return result;
	}

	public List<LaundryShipmentDto> shipments(
			LaundryShipmentStatus status, LaundryShipmentKind kind, Long providerId, LocalDate from, LocalDate to
	) {
		if (from != null && to != null && to.isBefore(from)) throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateTo must be on or after dateFrom.");
		OffsetDateTime start = from == null ? null : from.atStartOfDay().atOffset(ZoneOffset.UTC);
		OffsetDateTime end = to == null ? null : to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
		return shipmentRepository.search(status, kind, providerId, start, end).stream().map(LaundryShipmentDto::from).toList();
	}

	public LaundrySummaryDto summary(Long providerId, LocalDate from, LocalDate to) {
		if (from != null && to != null && to.isBefore(from)) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "dateTo must be on or after dateFrom.");
		}
		OffsetDateTime start = from == null ? null : from.atStartOfDay().atOffset(ZoneOffset.UTC);
		OffsetDateTime end = to == null ? null : to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
		Map<LocalDate, BigDecimal[]> daily = new TreeMap<>();
		addSummary(daily, shipmentRepository.summarizeReceivedMovements(providerId, start, end), 0);
		addSummary(daily, shipmentRepository.summarizeSentMovements(LaundryShipmentKind.STANDARD, providerId, start, end), 1);
		addSummary(daily, shipmentRepository.summarizeSentMovements(LaundryShipmentKind.REWASH, providerId, start, end), 2);

		List<LaundryDailySummaryRowDto> rows = daily.entrySet().stream()
				.sorted(Map.Entry.<LocalDate, BigDecimal[]>comparingByKey().reversed())
				.map(entry -> new LaundryDailySummaryRowDto(
						entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]))
				.toList();
		BigDecimal[] totals = rows.stream().reduce(
				zeroQuantities(),
				(total, row) -> new BigDecimal[] {
						total[0].add(row.receivedQuantity()),
						total[1].add(row.sentQuantity()),
						total[2].add(row.rewashedQuantity())
				},
				(left, right) -> new BigDecimal[] {
						left[0].add(right[0]), left[1].add(right[1]), left[2].add(right[2])
				});
		return new LaundrySummaryDto(rows, totals[0], totals[1], totals[2]);
	}

	public LaundryShipmentDto shipment(Long id) { return LaundryShipmentDto.from(requireShipment(id)); }

	@Transactional
	public LaundryReceiptResultDto receive(Long id, ReceiveLaundryShipmentDto input, String actor) {
		LaundryShipment shipment = requireShipment(id);
		if (shipment.getVersion() != input.version()) throw new ApplicationException(HttpStatus.CONFLICT, "Shipment was modified by another operation. Reload it and try again.");
		if (shipment.getStatus() != LaundryShipmentStatus.SENT
				&& shipment.getStatus() != LaundryShipmentStatus.PARTIALLY_RECEIVED) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Shipment cannot receive more items.");
		}
		if (input.receivedAt().isBefore(shipment.getSentAt())) throw new ApplicationException(HttpStatus.BAD_REQUEST, "receivedAt cannot be before sentAt.");
		Map<Long, LaundryShipmentLine> available = new HashMap<>();
		shipment.getLines().forEach(line -> available.put(line.getId(), line));
		Set<Long> processed = new HashSet<>();
		try {
			for (ReceiveLaundryLineDto received : input.lines()) {
				if (!processed.add(received.lineId())) throw new ApplicationException(HttpStatus.BAD_REQUEST, "Receipt cannot contain duplicate lines.");
				LaundryShipmentLine line = available.get(received.lineId());
				if (line == null) throw new ApplicationException(HttpStatus.BAD_REQUEST, "Receipt line does not belong to shipment: " + received.lineId());
				if (received.rewashQuantity().signum() > 0 || received.rewashReason() != null) {
					throw new ApplicationException(HttpStatus.BAD_REQUEST, "Rewash must be registered as an independent transaction.");
				}
				line.classify(received.acceptedQuantity(), received.rewashQuantity(), received.missingQuantity(), received.damagedQuantity());
			}
			shipment.refreshAfterReceipt(input.receivedAt(), actor);
		} catch (IllegalStateException ex) {
			throw new ApplicationException(HttpStatus.CONFLICT, ex.getMessage());
		}
		LaundryShipment saved = shipmentRepository.saveAndFlush(shipment);
		LaundryShipmentDto result = LaundryShipmentDto.from(saved);
		auditTrailService.record("LAUNDRY", "LAUNDRY_SHIPMENT", saved.getId(), "RECEIVE_SHIPMENT", "Laundry shipment received", receiptChanges(input), null, auditSnapshot(result));
		return new LaundryReceiptResultDto(result, null);
	}

	@Transactional
	public LaundryShipmentDto dispatch(Long id, DispatchLaundryShipmentDto input, String actor) {
		LaundryShipment shipment = requireShipment(id);
		if (shipment.getVersion() != input.version()) {
			throw new ApplicationException(HttpStatus.CONFLICT, "Shipment was modified by another operation. Reload it and try again.");
		}
		Map<String, Object> before = auditSnapshot(LaundryShipmentDto.from(shipment));
		try {
			shipment.dispatch(input.sentAt(), input.notes(), actor);
		} catch (IllegalStateException ex) {
			throw new ApplicationException(HttpStatus.CONFLICT, ex.getMessage());
		}
		LaundryShipmentDto result = LaundryShipmentDto.from(shipmentRepository.saveAndFlush(shipment));
		auditTrailService.record(
				"LAUNDRY", "LAUNDRY_SHIPMENT", id, "DISPATCH_REWASH", "Rewash shipment dispatched",
				Map.of("sentAt", input.sentAt().toString(), "version", input.version(),
						"notes", input.notes() == null ? "" : input.notes()), before, auditSnapshot(result)
		);
		return result;
	}

	@Transactional
	public LaundryShipmentDto cancel(Long id, CancelLaundryShipmentDto input, String actor) {
		LaundryShipment shipment = requireShipment(id);
		Map<String, Object> before = auditSnapshot(LaundryShipmentDto.from(shipment));
		try { shipment.cancel(input.cancellationNotes(), actor); }
		catch (IllegalStateException ex) { throw new ApplicationException(HttpStatus.CONFLICT, ex.getMessage()); }
		LaundryShipmentDto result = LaundryShipmentDto.from(shipmentRepository.save(shipment));
		auditTrailService.record("LAUNDRY", "LAUNDRY_SHIPMENT", id, "CANCEL", "Laundry shipment cancelled", input, before, auditSnapshot(result));
		return result;
	}

	public List<AuditEventDto> audit(Long id) { requireShipment(id); return auditTrailService.findByEntity("LAUNDRY_SHIPMENT", id); }

	private Map<String, Object> auditSnapshot(LaundryShipmentDto shipment) {
		Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
		snapshot.put("id", shipment.id());
		snapshot.put("shipmentNumber", shipment.shipmentNumber());
		snapshot.put("providerId", shipment.providerId());
		snapshot.put("kind", shipment.kind());
		snapshot.put("status", shipment.status());
		snapshot.put("sentAt", shipment.sentAt() == null ? null : shipment.sentAt().toString());
		snapshot.put("notes", shipment.notes());
		snapshot.put("version", shipment.version());
		snapshot.put("receivedAt", shipment.receivedAt() == null ? null : shipment.receivedAt().toString());
		snapshot.put("billableAmount", shipment.billableAmount());
		snapshot.put("lines", shipment.lines());
		return snapshot;
	}

	private Map<String, Object> receiptChanges(ReceiveLaundryShipmentDto input) {
		return Map.of("receivedAt", input.receivedAt().toString(), "version", input.version(), "lines", input.lines());
	}

	private void addSummary(
			Map<LocalDate, BigDecimal[]> daily, List<LaundryMovementSummaryView> movements, int quantityIndex
	) {
		for (LaundryMovementSummaryView movement : movements) {
			if (movement.getOccurredAt() == null || movement.getQuantity() == null) continue;
			BigDecimal[] quantities = daily.computeIfAbsent(
					movement.getOccurredAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate(), ignored -> zeroQuantities());
			quantities[quantityIndex] = quantities[quantityIndex].add(movement.getQuantity());
		}
	}

	private BigDecimal[] zeroQuantities() {
		return new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
	}

	private LaundryShipment requireShipment(Long id) {
		return shipmentRepository.findDetailedById(id)
				.orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Laundry shipment not found."));
	}

	private String number() { return "LAV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
}
