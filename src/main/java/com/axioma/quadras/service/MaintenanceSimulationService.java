package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.LoadMaintenanceSimulationDto;
import com.axioma.quadras.domain.dto.MaintenanceSimulationResultDto;
import com.axioma.quadras.domain.model.MaintenanceBusinessPriority;
import com.axioma.quadras.domain.model.MaintenanceLocation;
import com.axioma.quadras.domain.model.MaintenanceLocationType;
import com.axioma.quadras.domain.model.MaintenanceOrder;
import com.axioma.quadras.domain.model.MaintenanceOrderStatus;
import com.axioma.quadras.domain.model.MaintenancePaymentMethod;
import com.axioma.quadras.domain.model.MaintenancePriority;
import com.axioma.quadras.domain.model.MaintenanceProvider;
import com.axioma.quadras.domain.model.MaintenanceProviderSpecialty;
import com.axioma.quadras.domain.model.MaintenanceProviderType;
import com.axioma.quadras.domain.model.MaintenanceRequestOrigin;
import com.axioma.quadras.repository.MaintenanceLocationRepository;
import com.axioma.quadras.repository.MaintenanceOrderRepository;
import com.axioma.quadras.repository.MaintenanceProviderRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceSimulationService {

	private static final String SIMULATION_PREFIX = "sim.";
	private static final String SIMULATION_SEED_ACTOR = "sim.seed.maintenance";
	private static final long DEFAULT_SEED = 20260405L;
	private static final int DEFAULT_DAYS_BACK = 21;
	private static final int DEFAULT_DAYS_FORWARD = 10;
	private static final int DEFAULT_ORDERS_PER_DAY = 6;
	private static final int DEFAULT_BACKLOG_ORDERS = 10;

	private static final List<LocationSeed> BASE_LOCATIONS = buildBaseLocations();
	private static final List<ProviderSeed> BASE_PROVIDERS = List.of(
			new ProviderSeed(
					MaintenanceProviderType.INTERNAL,
					MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
					"Equipo interno de mantenimiento",
					"Manutencao geral del hotel",
					"Atiende correctivos generales, habitaciones, chalets y areas comunes.",
					"interno.mantenimiento@hotel.local"
			),
			new ProviderSeed(
					MaintenanceProviderType.EXTERNAL,
					MaintenanceProviderSpecialty.ELEVATORS,
					"Ascensores del Norte",
					"Servicio de elevadores",
					"Inspeccion, reparacion y soporte tecnico de elevadores.",
					"guardia@ascensoresnorte.local"
			),
			new ProviderSeed(
					MaintenanceProviderType.EXTERNAL,
					MaintenanceProviderSpecialty.AIR_CONDITIONING,
					"Clima Sul",
					"Servicio de aires acondicionados",
					"Limpieza, mantenimiento preventivo y correctivo de aire acondicionado.",
					"soporte@climasul.local"
			)
	);
	private static final List<RequesterSeed> GUEST_REQUESTERS = List.of(
			new RequesterSeed("sim.recepcion.1", "FRONT_DESK"),
			new RequesterSeed("sim.recepcion.2", "FRONT_DESK"),
			new RequesterSeed("sim.conserjeria.1", "CONCIERGE")
	);
	private static final List<RequesterSeed> INTERNAL_REQUESTERS = List.of(
			new RequesterSeed("sim.gobernanza.1", "HOUSEKEEPING"),
			new RequesterSeed("sim.operaciones.1", "OPERATIONS"),
			new RequesterSeed("sim.supervision.1", "SUPERVISOR")
	);
	private static final List<String> GUEST_NAMES = List.of(
			"Familia Duarte",
			"Sra. Medina",
			"Sr. Correa",
			"Familia Paredes",
			"Huesped VIP 1",
			"Huesped VIP 2",
			"Pareja suite 3"
	);
	private static final List<String> GENERAL_ROOM_TITLES = List.of(
			"Ajustar cerradura",
			"Corregir luminaria",
			"Destapar desague",
			"Revisar ducha",
			"Corregir TV sin senal",
			"Reparar toma electrica"
	);
	private static final List<String> GENERAL_COMMON_AREA_TITLES = List.of(
			"Revisar iluminacion",
			"Ajustar puerta de acceso",
			"Corregir filtracion menor",
			"Repasar pintura correctiva",
			"Inspeccion preventiva"
	);
	private static final List<String> AIR_TITLES = List.of(
			"Aire no enfria",
			"Limpieza de filtros",
			"Goteo de aire acondicionado",
			"Ruido en unidad interior"
	);
	private static final List<String> ELEVATOR_TITLES = List.of(
			"Revision preventiva de elevador",
			"Puerta del elevador no cierra",
			"Alarma del elevador activada"
	);
	private static final List<String> COMPLETION_NOTES = List.of(
			"Trabajo finalizado y validado por mantenimiento.",
			"Incidencia resuelta y area operativa nuevamente.",
			"Se reemplazo componente y se dejo evidencia en orden."
	);
	private static final List<String> CANCELLATION_NOTES = List.of(
			"Se reprograma por disponibilidad de proveedor.",
			"Se pospone por espera de repuesto.",
			"El area solicitante informo que ya no requiere intervencion."
	);

	private final MaintenanceLocationRepository maintenanceLocationRepository;
	private final MaintenanceProviderRepository maintenanceProviderRepository;
	private final MaintenanceOrderRepository maintenanceOrderRepository;

	public MaintenanceSimulationService(
			MaintenanceLocationRepository maintenanceLocationRepository,
			MaintenanceProviderRepository maintenanceProviderRepository,
			MaintenanceOrderRepository maintenanceOrderRepository
	) {
		this.maintenanceLocationRepository = maintenanceLocationRepository;
		this.maintenanceProviderRepository = maintenanceProviderRepository;
		this.maintenanceOrderRepository = maintenanceOrderRepository;
	}

	@Transactional
	public MaintenanceSimulationResultDto load(
			LoadMaintenanceSimulationDto input,
			String actorUsername
	) {
		final SimulationConfig config = SimulationConfig.from(input);

		int ordersDeleted = 0;
		int locationsDeleted = 0;
		int providersDeleted = 0;
		if (config.resetPreviousSimulation()) {
			final CleanupResult cleanupResult = clearSyntheticData();
			ordersDeleted = cleanupResult.ordersDeleted();
			locationsDeleted = cleanupResult.locationsDeleted();
			providersDeleted = cleanupResult.providersDeleted();
		}

		final CatalogResult catalogResult = ensureCatalog();
		final List<MaintenanceLocation> locations =
				maintenanceLocationRepository.findAllByOrderByLocationTypeAscCodeAsc();
		final Map<MaintenanceProviderSpecialty, MaintenanceProvider> providersBySpecialty =
				maintenanceProviderRepository.findAllByOrderByProviderTypeAscNameAsc()
						.stream()
						.filter(MaintenanceProvider::isActive)
						.collect(Collectors.toMap(
								MaintenanceProvider::getSpecialty,
								provider -> provider,
								(first, ignored) -> first,
								() -> new EnumMap<>(MaintenanceProviderSpecialty.class)
						));

		final Random random = new Random(config.seed());
		final LocalDate today = LocalDate.now();
		final LocalDate dateFrom = today.minusDays(config.daysBack());
		final LocalDate dateTo = today.plusDays(config.daysForward());
		final EnumMap<MaintenanceOrderStatus, Integer> counts =
				new EnumMap<>(MaintenanceOrderStatus.class);
		final List<MaintenanceOrder> generatedOrders = new ArrayList<>();

		for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
			final int dailyOrders = config.ordersPerDay() + random.nextInt(3);
			for (int index = 0; index < dailyOrders; index++) {
				final MaintenanceOrder order = buildScheduledOrder(
						date,
						today,
						index,
						locations,
						providersBySpecialty,
						random
				);
				generatedOrders.add(order);
				counts.merge(order.getStatus(), 1, Integer::sum);
			}
		}

		for (int index = 0; index < config.backlogOrders(); index++) {
			final MaintenanceOrder order = buildBacklogOrder(
					locations,
					providersBySpecialty,
					random,
					index % 3 == 0
			);
			generatedOrders.add(order);
			counts.merge(order.getStatus(), 1, Integer::sum);
		}

		maintenanceOrderRepository.saveAll(generatedOrders);

		return new MaintenanceSimulationResultDto(
				dateFrom,
				dateTo,
				config.seed(),
				config.resetPreviousSimulation(),
				catalogResult.locationsCreated(),
				catalogResult.providersCreated(),
				ordersDeleted,
				locationsDeleted,
				providersDeleted,
				generatedOrders.size(),
				counts.getOrDefault(MaintenanceOrderStatus.OPEN, 0),
				counts.getOrDefault(MaintenanceOrderStatus.ASSIGNED, 0),
				counts.getOrDefault(MaintenanceOrderStatus.SCHEDULED, 0),
				counts.getOrDefault(MaintenanceOrderStatus.IN_PROGRESS, 0),
				counts.getOrDefault(MaintenanceOrderStatus.COMPLETED, 0),
				counts.getOrDefault(MaintenanceOrderStatus.CANCELLED, 0)
		);
	}

	private CatalogResult ensureCatalog() {
		final Map<String, MaintenanceLocation> existingLocations =
				maintenanceLocationRepository.findAllByOrderByLocationTypeAscCodeAsc()
						.stream()
						.collect(Collectors.toMap(
								location -> locationKey(location.getLocationType(), location.getCode()),
								location -> location,
								(first, ignored) -> first
						));
		int locationsCreated = 0;
		for (final LocationSeed seed : BASE_LOCATIONS) {
			final String key = locationKey(seed.locationType(), seed.code());
			if (existingLocations.containsKey(key)) {
				continue;
			}
			final MaintenanceLocation created = maintenanceLocationRepository.save(
					MaintenanceLocation.create(
							seed.locationType(),
							null,
							seed.code(),
							seed.label(),
							seed.floor(),
							seed.description(),
							true,
							SIMULATION_SEED_ACTOR
					)
			);
			existingLocations.put(key, created);
			locationsCreated++;
		}

		final List<MaintenanceProvider> allProviders =
				maintenanceProviderRepository.findAllByOrderByProviderTypeAscNameAsc();
		final Map<MaintenanceProviderSpecialty, MaintenanceProvider> providersBySpecialty =
				allProviders.stream()
						.filter(MaintenanceProvider::isActive)
						.collect(Collectors.toMap(
								MaintenanceProvider::getSpecialty,
								provider -> provider,
								(first, ignored) -> first,
								() -> new EnumMap<>(MaintenanceProviderSpecialty.class)
						));
		int providersCreated = 0;
		for (final ProviderSeed seed : BASE_PROVIDERS) {
			if (providersBySpecialty.containsKey(seed.specialty())) {
				continue;
			}
			final MaintenanceProvider created = maintenanceProviderRepository.save(
					MaintenanceProvider.create(
							seed.providerType(),
							seed.specialty(),
							seed.name(),
							seed.serviceLabel(),
							seed.scopeDescription(),
							seed.contact(),
							true,
							SIMULATION_SEED_ACTOR
					)
			);
			providersBySpecialty.put(seed.specialty(), created);
			providersCreated++;
		}

		return new CatalogResult(locationsCreated, providersCreated);
	}

	private CleanupResult clearSyntheticData() {
		return new CleanupResult(
				maintenanceOrderRepository.deleteInBulkByCreatedByPrefix(SIMULATION_PREFIX),
				maintenanceLocationRepository.deleteInBulkByCreatedByPrefix(SIMULATION_PREFIX),
				maintenanceProviderRepository.deleteInBulkByCreatedByPrefix(SIMULATION_PREFIX)
		);
	}

	private MaintenanceOrder buildScheduledOrder(
			LocalDate date,
			LocalDate today,
			int slotIndex,
			List<MaintenanceLocation> locations,
			Map<MaintenanceProviderSpecialty, MaintenanceProvider> providersBySpecialty,
			Random random
	) {
		final ScenarioSeed scenario = scenarioForDate(date, today, random);
		final IssueSeed issue = chooseIssue(locations, random);
		final MaintenanceLocation location = chooseLocation(locations, issue.specialty(), random);
		final boolean guestRequest = issue.allowsGuestRequest()
				&& location.getLocationType() == MaintenanceLocationType.ROOM
				&& random.nextDouble() < 0.55;
		final RequesterSeed requester = guestRequest
				? randomItem(GUEST_REQUESTERS, random)
				: randomItem(INTERNAL_REQUESTERS, random);
		final String guestReference = guestRequest ? location.getCode() : null;
		final String guestName = guestRequest ? randomItem(GUEST_NAMES, random) : null;
		final MaintenanceBusinessPriority businessPriority = guestRequest
				? MaintenanceBusinessPriority.GUEST_PRIORITY
				: (random.nextDouble() < 0.18
						? MaintenanceBusinessPriority.CRITICAL_OPERATION
						: MaintenanceBusinessPriority.INTERNAL_STANDARD);
		final MaintenanceProvider provider = providersBySpecialty.get(issue.specialty());
		final String assignee = assigneeFor(issue.specialty(), random);
		final LocalDateTime startAt = date.atTime(8 + (slotIndex % 10), random.nextInt(2) * 30);
		final int durationMinutes = issue.minMinutes()
				+ random.nextInt(issue.maxMinutes() - issue.minMinutes() + 1);
		final LocalDateTime endAt = startAt.plusMinutes(durationMinutes);

		final MaintenanceOrder order = MaintenanceOrder.report(
				location,
				provider,
				issue.title(),
				buildDescription(issue, location, guestRequest),
				issue.priority(),
				guestRequest ? MaintenanceRequestOrigin.GUEST_REQUEST : MaintenanceRequestOrigin.INTERNAL_ROLE,
				guestRequest,
				guestName,
				guestReference,
				businessPriority,
				durationMinutes,
				assignee,
				startAt,
				endAt,
				requester.username(),
				requester.role()
		);

		if (scenario.status() == MaintenanceOrderStatus.IN_PROGRESS) {
			order.start(
					OffsetDateTime.of(startAt.plusMinutes(5), ZoneOffset.UTC),
					assignee
			);
		} else if (scenario.status() == MaintenanceOrderStatus.COMPLETED) {
			order.start(
					OffsetDateTime.of(startAt.plusMinutes(3), ZoneOffset.UTC),
					assignee
			);
			order.complete(
					OffsetDateTime.of(endAt, ZoneOffset.UTC),
					randomItem(COMPLETION_NOTES, random),
					assignee
			);
			if (provider != null
					&& provider.getProviderType() == MaintenanceProviderType.EXTERNAL
					&& random.nextDouble() < 0.55) {
				order.markPayment(
						randomPaymentMethod(random),
						date,
						"Pago registrado dentro de la simulacion operativa.",
						"sim.administracion.1"
				);
			}
		} else if (scenario.status() == MaintenanceOrderStatus.CANCELLED) {
			order.cancel(randomItem(CANCELLATION_NOTES, random), assignee);
		}

		return order;
	}

	private MaintenanceOrder buildBacklogOrder(
			List<MaintenanceLocation> locations,
			Map<MaintenanceProviderSpecialty, MaintenanceProvider> providersBySpecialty,
			Random random,
			boolean createOpenOrder
	) {
		final IssueSeed issue = chooseIssue(locations, random);
		final MaintenanceLocation location = chooseLocation(locations, issue.specialty(), random);
		final boolean guestRequest = issue.allowsGuestRequest()
				&& location.getLocationType() == MaintenanceLocationType.ROOM
				&& random.nextDouble() < 0.45;
		final RequesterSeed requester = guestRequest
				? randomItem(GUEST_REQUESTERS, random)
				: randomItem(INTERNAL_REQUESTERS, random);
		final MaintenanceProvider provider = createOpenOrder
				? null
				: providersBySpecialty.get(issue.specialty());
		final String assignee = createOpenOrder ? null : assigneeFor(issue.specialty(), random);
		final String guestReference = guestRequest ? location.getCode() : null;
		final String guestName = guestRequest ? randomItem(GUEST_NAMES, random) : null;

		return MaintenanceOrder.report(
				location,
				provider,
				issue.title(),
				buildDescription(issue, location, guestRequest),
				issue.priority(),
				guestRequest ? MaintenanceRequestOrigin.GUEST_REQUEST : MaintenanceRequestOrigin.INTERNAL_ROLE,
				guestRequest,
				guestName,
				guestReference,
				guestRequest
						? MaintenanceBusinessPriority.GUEST_PRIORITY
						: MaintenanceBusinessPriority.INTERNAL_STANDARD,
				issue.minMinutes() + random.nextInt(31),
				assignee,
				null,
				null,
				requester.username(),
				requester.role()
		);
	}

	private ScenarioSeed scenarioForDate(LocalDate date, LocalDate today, Random random) {
		if (date.isBefore(today)) {
			final double marker = random.nextDouble();
			if (marker < 0.68) {
				return new ScenarioSeed(MaintenanceOrderStatus.COMPLETED);
			}
			if (marker < 0.84) {
				return new ScenarioSeed(MaintenanceOrderStatus.CANCELLED);
			}
			return new ScenarioSeed(MaintenanceOrderStatus.SCHEDULED);
		}
		if (date.isAfter(today)) {
			return new ScenarioSeed(MaintenanceOrderStatus.SCHEDULED);
		}
		final double marker = random.nextDouble();
		if (marker < 0.20) {
			return new ScenarioSeed(MaintenanceOrderStatus.COMPLETED);
		}
		if (marker < 0.38) {
			return new ScenarioSeed(MaintenanceOrderStatus.IN_PROGRESS);
		}
		if (marker < 0.48) {
			return new ScenarioSeed(MaintenanceOrderStatus.CANCELLED);
		}
		return new ScenarioSeed(MaintenanceOrderStatus.SCHEDULED);
	}

	private IssueSeed chooseIssue(List<MaintenanceLocation> locations, Random random) {
		final double marker = random.nextDouble();
		if (marker < 0.62) {
			return new IssueSeed(
					randomItem(GENERAL_ROOM_TITLES, random),
					MaintenanceProviderSpecialty.GENERAL_MAINTENANCE,
					randomPriority(random, false),
					35,
					110,
					true
			);
		}
		if (marker < 0.87) {
			return new IssueSeed(
					randomItem(AIR_TITLES, random),
					MaintenanceProviderSpecialty.AIR_CONDITIONING,
					randomPriority(random, true),
					45,
					150,
					true
			);
		}
		return new IssueSeed(
				randomItem(ELEVATOR_TITLES, random),
				MaintenanceProviderSpecialty.ELEVATORS,
				randomPriority(random, true),
				60,
				180,
				false
		);
	}

	private MaintenanceLocation chooseLocation(
			List<MaintenanceLocation> locations,
			MaintenanceProviderSpecialty specialty,
			Random random
	) {
		final Predicate<MaintenanceLocation> filter = switch (specialty) {
			case ELEVATORS -> location -> location.getLocationType() == MaintenanceLocationType.COMMON_AREA;
			case AIR_CONDITIONING -> location -> true;
			case GENERAL_MAINTENANCE -> location -> true;
		};
		final List<MaintenanceLocation> filtered = locations.stream()
				.filter(MaintenanceLocation::isActive)
				.filter(filter)
				.toList();
		return randomItem(filtered.isEmpty() ? locations : filtered, random);
	}

	private String buildDescription(
			IssueSeed issue,
			MaintenanceLocation location,
			boolean guestRequest
	) {
		final String target = guestRequest
				? "Incidencia reportada para atencion prioritaria al huesped"
				: "Incidencia relevada para seguimiento operativo interno";
		return "%s en %s. %s.".formatted(issue.title(), location.getLabel(), target);
	}

	private MaintenancePriority randomPriority(Random random, boolean allowUrgent) {
		final double marker = random.nextDouble();
		if (allowUrgent && marker < 0.18) {
			return MaintenancePriority.URGENT;
		}
		if (marker < 0.42) {
			return MaintenancePriority.HIGH;
		}
		if (marker < 0.78) {
			return MaintenancePriority.MEDIUM;
		}
		return MaintenancePriority.LOW;
	}

	private MaintenancePaymentMethod randomPaymentMethod(Random random) {
		final MaintenancePaymentMethod[] values = MaintenancePaymentMethod.values();
		return values[random.nextInt(values.length)];
	}

	private String assigneeFor(MaintenanceProviderSpecialty specialty, Random random) {
		return switch (specialty) {
			case GENERAL_MAINTENANCE -> random.nextBoolean()
					? "sim.mantenimiento.1"
					: "sim.mantenimiento.2";
			case AIR_CONDITIONING -> "sim.aires.ext";
			case ELEVATORS -> "sim.elevadores.ext";
		};
	}

	private String locationKey(MaintenanceLocationType locationType, String code) {
		return locationType.name() + "|" + code.trim().toUpperCase();
	}

	private static <T> T randomItem(List<T> items, Random random) {
		return items.get(random.nextInt(items.size()));
	}

	private static List<LocationSeed> buildBaseLocations() {
		final List<LocationSeed> seeds = new ArrayList<>();
		seeds.add(new LocationSeed(
				MaintenanceLocationType.COMMON_AREA,
				"LOBBY",
				"Lobby",
				"PB",
				"Area principal de recepcion y circulacion."
		));
		seeds.add(new LocationSeed(
				MaintenanceLocationType.COMMON_AREA,
				"JUEGOS",
				"Sala de juegos",
				"PB",
				"Area recreativa para huespedes."
		));
		seeds.add(new LocationSeed(
				MaintenanceLocationType.COMMON_AREA,
				"TV",
				"Sala de TV",
				"PB",
				"Area comun con television y estar."
		));
		seeds.add(new LocationSeed(
				MaintenanceLocationType.COMMON_AREA,
				"GOB",
				"Gobernanza",
				"PB",
				"Area operativa de housekeeping."
		));
		seeds.add(new LocationSeed(
				MaintenanceLocationType.COMMON_AREA,
				"REFECTORIO",
				"Refectorio",
				"PB",
				"Sector de servicio y apoyo."
		));
		seeds.add(new LocationSeed(
				MaintenanceLocationType.COMMON_AREA,
				"ESTAC",
				"Estacionamiento",
				"PB",
				"Area de estacionamiento y circulacion vehicular."
		));
		for (int chalet = 1; chalet <= 12; chalet++) {
			seeds.add(new LocationSeed(
					MaintenanceLocationType.ROOM,
					"CH%02d".formatted(chalet),
					"Chalet %d".formatted(chalet),
					"Chalets",
					"Unidad de alojamiento chalet %d.".formatted(chalet)
			));
		}
		for (int floor = 1; floor <= 4; floor++) {
			seeds.add(new LocationSeed(
					MaintenanceLocationType.COMMON_AREA,
					"P%d-ESC".formatted(floor),
					"Escalera piso %d".formatted(floor),
					String.valueOf(floor),
					"Circulacion vertical del piso %d.".formatted(floor)
			));
			seeds.add(new LocationSeed(
					MaintenanceLocationType.COMMON_AREA,
					"P%d-COM".formatted(floor),
					"Area comun piso %d".formatted(floor),
					String.valueOf(floor),
					"Pasillo y area comun del piso %d.".formatted(floor)
			));
		}
		for (int room = 101; room <= 111; room++) {
			seeds.add(roomSeed(room, 1));
		}
		for (int room = 201; room <= 211; room++) {
			seeds.add(roomSeed(room, 2));
		}
		for (int room = 301; room <= 311; room++) {
			seeds.add(roomSeed(room, 3));
		}
		for (int room = 401; room <= 407; room++) {
			seeds.add(roomSeed(room, 4));
		}
		return seeds;
	}

	private static LocationSeed roomSeed(int roomNumber, int floor) {
		return new LocationSeed(
				MaintenanceLocationType.ROOM,
				String.valueOf(roomNumber),
				"Habitacion %d".formatted(roomNumber),
				String.valueOf(floor),
				"Habitacion del piso %d.".formatted(floor)
		);
	}

	private record SimulationConfig(
			int daysBack,
			int daysForward,
			int ordersPerDay,
			int backlogOrders,
			boolean resetPreviousSimulation,
			long seed
	) {
		private static SimulationConfig from(LoadMaintenanceSimulationDto input) {
			if (input == null) {
				return new SimulationConfig(
						DEFAULT_DAYS_BACK,
						DEFAULT_DAYS_FORWARD,
						DEFAULT_ORDERS_PER_DAY,
						DEFAULT_BACKLOG_ORDERS,
						true,
						DEFAULT_SEED
				);
			}
			return new SimulationConfig(
					input.daysBack() == null ? DEFAULT_DAYS_BACK : input.daysBack(),
					input.daysForward() == null ? DEFAULT_DAYS_FORWARD : input.daysForward(),
					input.ordersPerDay() == null ? DEFAULT_ORDERS_PER_DAY : input.ordersPerDay(),
					input.backlogOrders() == null ? DEFAULT_BACKLOG_ORDERS : input.backlogOrders(),
					input.resetPreviousSimulation() == null || input.resetPreviousSimulation(),
					input.seed() == null ? DEFAULT_SEED : input.seed()
			);
		}
	}

	private record CatalogResult(int locationsCreated, int providersCreated) {
	}

	private record CleanupResult(int ordersDeleted, int locationsDeleted, int providersDeleted) {
	}

	private record LocationSeed(
			MaintenanceLocationType locationType,
			String code,
			String label,
			String floor,
			String description
	) {
	}

	private record ProviderSeed(
			MaintenanceProviderType providerType,
			MaintenanceProviderSpecialty specialty,
			String name,
			String serviceLabel,
			String scopeDescription,
			String contact
	) {
	}

	private record RequesterSeed(String username, String role) {
	}

	private record ScenarioSeed(MaintenanceOrderStatus status) {
	}

	private record IssueSeed(
			String title,
			MaintenanceProviderSpecialty specialty,
			MaintenancePriority priority,
			int minMinutes,
			int maxMinutes,
			boolean allowsGuestRequest
	) {
	}
}
