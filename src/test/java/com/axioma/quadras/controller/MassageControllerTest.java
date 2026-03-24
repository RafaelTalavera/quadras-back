package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassageBookingStatus;
import com.axioma.quadras.domain.model.MassagePaymentMethod;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.domain.model.MassageTherapist;
import com.axioma.quadras.repository.MassageBookingRepository;
import com.axioma.quadras.repository.MassageProviderRepository;
import com.axioma.quadras.repository.MassageTherapistRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.flyway.enabled=true",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class MassageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MassageProviderRepository massageProviderRepository;

	@Autowired
	private MassageBookingRepository massageBookingRepository;

	@Autowired
	private MassageTherapistRepository massageTherapistRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		massageBookingRepository.deleteAll();
		massageTherapistRepository.deleteAll();
		massageProviderRepository.deleteAll();
		accessToken = authenticate();
	}

	@Test
	void shouldCreateMassageProvider() throws Exception {
		final String payload = """
				{
				  "name": "Danuska",
				  "specialty": "Drenagem e relaxante",
				  "contact": "Agenda interna"
				}
				""";

		mockMvc.perform(post("/api/v1/massages/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Danuska"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.therapists").isArray())
				.andExpect(jsonPath("$.therapists.length()").value(0));
	}

	@Test
	void shouldListOnlyActiveMassageProvidersWhenRequested() throws Exception {
		final MassageProvider active = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageProvider inactive = massageProviderRepository.save(
				MassageProvider.create("Juliana", "Premium", "Agenda interna")
		);
		inactive.update("Juliana", "Premium", "Agenda interna", false);
		massageProviderRepository.save(inactive);

		mockMvc.perform(get("/api/v1/massages/providers")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("activeOnly", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(active.getId()))
				.andExpect(jsonPath("$[0].name").value("David"));
	}

	@Test
	void shouldUpdateMassageProvider() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Isabelita", "Pedras quentes", "Interno")
		);
		final String payload = """
				{
				  "name": "Isabelita",
				  "specialty": "Pedras quentes e terapeutica",
				  "contact": "98888-1111",
				  "active": false
				}
				""";

		mockMvc.perform(put("/api/v1/massages/providers/{id}", provider.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(provider.getId()))
				.andExpect(jsonPath("$.contact").value("98888-1111"))
				.andExpect(jsonPath("$.active").value(false))
				.andExpect(jsonPath("$.therapists").isArray());
	}

	@Test
	void shouldCreateMassageTherapistInsideProvider() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Relaxante", "Interno")
		);
		final String payload = """
				{
				  "name": "Bruna"
				}
				""";

		mockMvc.perform(post("/api/v1/massages/providers/{id}/therapists", provider.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Bruna"))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void shouldUpdateMassageTherapist() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Relaxante", "Interno")
		);
		final MassageTherapist therapist = massageTherapistRepository.save(
				MassageTherapist.create(provider, "Bruna")
		);
		final String payload = """
				{
				  "name": "Bruna Souza",
				  "active": false
				}
				""";

		mockMvc.perform(put("/api/v1/massages/providers/{providerId}/therapists/{therapistId}",
						provider.getId(),
						therapist.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(therapist.getId()))
				.andExpect(jsonPath("$.name").value("Bruna Souza"))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void shouldCreateMassageBooking() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "17:00:00",
				  "clientName": "Andriele",
				  "guestReference": "Externo",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": true,
				  "paymentMethod": "CARD",
				  "paymentDate": "2026-03-19",
				  "paymentNotes": "Pago no balcao"
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.clientName").value("Andriele"))
				.andExpect(jsonPath("$.providerId").value(provider.getId()))
				.andExpect(jsonPath("$.providerName").value("David"))
				.andExpect(jsonPath("$.therapistId").value(therapist.getId()))
				.andExpect(jsonPath("$.therapistName").value("David"))
				.andExpect(jsonPath("$.paid").value(true))
				.andExpect(jsonPath("$.paymentMethod").value("CARD"))
				.andExpect(jsonPath("$.paymentDate").value("2026-03-19"))
				.andExpect(jsonPath("$.paymentNotes").value("Pago no balcao"))
				.andExpect(jsonPath("$.status").value("SCHEDULED"))
				.andExpect(jsonPath("$.createdBy").value("operador.demo"))
				.andExpect(jsonPath("$.updatedBy").value("operador.demo"));
	}

	@Test
	void shouldListMassageBookingsByDate() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Drenagem", "Agenda interna")
		);
		final MassageTherapist therapist = createTherapist(provider, "Danuska");
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 20),
				LocalTime.of(17, 0),
				"Cacilda",
				"Apto 405",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				true,
				MassagePaymentMethod.CASH,
				LocalDate.of(2026, 3, 20),
				"Pago direto",
				"operador.demo"
		));

		mockMvc.perform(get("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("bookingDate", "2026-03-20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].clientName").value("Cacilda"))
				.andExpect(jsonPath("$[0].providerName").value("Danuska"))
				.andExpect(jsonPath("$[0].therapistName").value("Danuska"))
				.andExpect(jsonPath("$[0].paymentMethod").value("CASH"));
	}

	@Test
	void shouldFilterMassageBookingsByClientAndPaidStatus() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Drenagem", "Agenda interna")
		);
		final MassageTherapist therapist = createTherapist(provider, "Danuska");
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 20),
				LocalTime.of(10, 0),
				"Carla Mendes",
				"Apto 101",
				"Relaxante",
				new BigDecimal("150.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 20),
				LocalTime.of(11, 0),
				"Roberta Lima",
				"Apto 102",
				"Drenagem",
				new BigDecimal("180.00"),
				provider,
				therapist,
				true,
				MassagePaymentMethod.PIX,
				LocalDate.of(2026, 3, 20),
				"Pago por chave",
				"operador.demo"
		));

		mockMvc.perform(get("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("clientName", "carla")
						.param("paid", "false"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].clientName").value("Carla Mendes"))
				.andExpect(jsonPath("$[0].paid").value(false));
	}

	@Test
	void shouldUpdateMassagePaymentForExistingBooking() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		final String payload = """
				{
				  "paymentMethod": "PIX",
				  "paymentDate": "2026-03-21",
				  "paymentNotes": "Pago apos atendimento"
				}
				""";

		mockMvc.perform(patch("/api/v1/massages/bookings/{id}/payment", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(booking.getId()))
				.andExpect(jsonPath("$.paid").value(true))
				.andExpect(jsonPath("$.paymentMethod").value("PIX"))
				.andExpect(jsonPath("$.paymentDate").value("2026-03-21"))
				.andExpect(jsonPath("$.paymentNotes").value("Pago apos atendimento"))
				.andExpect(jsonPath("$.updatedBy").value("operador.demo"));
	}

	@Test
	void shouldRejectPaidBookingWithoutPaymentData() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "17:00:00",
				  "clientName": "Andriele",
				  "guestReference": "Externo",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": true
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("paymentMethod is required when paid is true"));
	}

	@Test
	void shouldReturnConflictWhenTherapistAlreadyHasBookingForSameSlot() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 19),
				LocalTime.of(17, 0),
				"Cacilda",
				"Apto 405",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				true,
				MassagePaymentMethod.CARD,
				LocalDate.of(2026, 3, 19),
				"Pago antecipado",
				"operador.demo"
		));
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "17:00:00",
				  "clientName": "Paulo",
				  "guestReference": "Externo",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Massage therapist already has a booking for the selected date and time."
				));
	}

	@Test
	void shouldReturnConflictWhenCreatingBookingForInactiveProvider() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Juliana", "Premium", "Agenda interna")
		);
		final MassageTherapist therapist = createTherapist(provider, "Juliana");
		provider.update("Juliana", "Premium", "Agenda interna", false);
		massageProviderRepository.save(provider);
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "19:00:00",
				  "clientName": "Alessandra",
				  "guestReference": "Apto 405",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": true,
				  "paymentMethod": "CASH",
				  "paymentDate": "2026-03-19",
				  "paymentNotes": "Pago na recepcao"
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Inactive massage providers cannot receive bookings."
				));
	}

	@Test
	void shouldReturnConflictWhenCreatingBookingForInactiveTherapist() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Juliana", "Premium", "Agenda interna")
		);
		final MassageTherapist therapist = massageTherapistRepository.save(
				MassageTherapist.create(provider, "Bruna")
		);
		therapist.update("Bruna", false);
		massageTherapistRepository.save(therapist);
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "19:00:00",
				  "clientName": "Alessandra",
				  "guestReference": "Apto 405",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": true,
				  "paymentMethod": "CASH",
				  "paymentDate": "2026-03-19",
				  "paymentNotes": "Pago na recepcao"
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Inactive massage therapists cannot receive bookings."
				));
	}

	@Test
	void shouldUpdateMassageBooking() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageProvider otherProvider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Drenagem", "Agenda interna")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final MassageTherapist otherTherapist = createTherapist(otherProvider, "Danuska");
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		final String payload = """
				{
				  "bookingDate": "2026-03-21",
				  "startTime": "18:00:00",
				  "clientName": "Paula Souza",
				  "guestReference": "Apto 305",
				  "treatment": "Drenagem corporal",
				  "amount": 240.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": true,
				  "paymentMethod": "PIX",
				  "paymentDate": "2026-03-21",
				  "paymentNotes": "Pago apos ajuste"
				}
				""".formatted(otherProvider.getId(), otherTherapist.getId());

		mockMvc.perform(put("/api/v1/massages/bookings/{id}", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(booking.getId()))
				.andExpect(jsonPath("$.startTime").value("18:00:00"))
				.andExpect(jsonPath("$.guestReference").value("Apto 305"))
				.andExpect(jsonPath("$.treatment").value("Drenagem corporal"))
				.andExpect(jsonPath("$.providerId").value(otherProvider.getId()))
				.andExpect(jsonPath("$.therapistId").value(otherTherapist.getId()))
				.andExpect(jsonPath("$.paymentMethod").value("PIX"))
				.andExpect(jsonPath("$.updatedBy").value("operador.demo"));
	}

	@Test
	void shouldCancelMassageBookingWithObservation() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		final String payload = """
				{
				  "cancellationNotes": "Cliente desistiu do atendimento"
				}
				""";

		mockMvc.perform(patch("/api/v1/massages/bookings/{id}/cancel", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(booking.getId()))
				.andExpect(jsonPath("$.status").value("CANCELLED"))
				.andExpect(jsonPath("$.cancellationNotes").value("Cliente desistiu do atendimento"))
				.andExpect(jsonPath("$.cancelledBy").value("operador.demo"))
				.andExpect(jsonPath("$.updatedBy").value("operador.demo"));
	}

	@Test
	void shouldRejectEditingCancelledMassageBooking() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		booking.markCancelled("Cliente desistiu", "operador.demo");
		massageBookingRepository.save(booking);
		final String payload = """
				{
				  "bookingDate": "2026-03-21",
				  "startTime": "18:00:00",
				  "clientName": "Paula Souza",
				  "guestReference": "Apto 305",
				  "treatment": "Drenagem corporal",
				  "amount": 240.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(put("/api/v1/massages/bookings/{id}", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Cancelled massage bookings cannot be edited."));
	}

	@Test
	void shouldRequireCancellationObservation() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		final String payload = """
				{
				  "cancellationNotes": ""
				}
				""";

		mockMvc.perform(patch("/api/v1/massages/bookings/{id}/cancel", booking.getId())
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("cancellationNotes: cancellationNotes is required"));
	}

	@Test
	void shouldAllowRebookingSameSlotAfterCancellation() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final MassageTherapist therapist = createTherapist(provider, "David");
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				therapist,
				false,
				null,
				null,
				null,
				"operador.demo"
		));
		booking.markCancelled("Cliente desistiu", "operador.demo");
		massageBookingRepository.save(booking);
		final String payload = """
				{
				  "bookingDate": "2026-03-21",
				  "startTime": "17:00:00",
				  "clientName": "Nova Cliente",
				  "guestReference": "Apto 305",
				  "treatment": "Drenagem corporal",
				  "amount": 240.00,
				  "providerId": %d,
				  "therapistId": %d,
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null
				}
				""".formatted(provider.getId(), therapist.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.clientName").value("Nova Cliente"))
				.andExpect(jsonPath("$.status").value(MassageBookingStatus.SCHEDULED.name()));
	}

	private String bearerToken() {
		return "Bearer " + accessToken;
	}

	private MassageTherapist createTherapist(MassageProvider provider, String name) {
		return massageTherapistRepository.save(MassageTherapist.create(provider, name));
	}

	private String authenticate() throws Exception {
		final String payload = """
				{
				  "username": "operador.demo",
				  "password": "Costanorte2026!"
				}
				""";

		final String responseBody = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final JsonNode response = objectMapper.readTree(responseBody);
		return response.get("accessToken").asText();
	}
}
