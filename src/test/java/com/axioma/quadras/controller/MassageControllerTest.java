package com.axioma.quadras.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axioma.quadras.domain.model.MassageBooking;
import com.axioma.quadras.domain.model.MassagePaymentMethod;
import com.axioma.quadras.domain.model.MassageProvider;
import com.axioma.quadras.repository.MassageBookingRepository;
import com.axioma.quadras.repository.MassageProviderRepository;
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

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String accessToken;

	@BeforeEach
	void cleanDb() throws Exception {
		massageBookingRepository.deleteAll();
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
				.andExpect(jsonPath("$.active").value(true));
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
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void shouldCreateMassageBooking() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "17:00:00",
				  "clientName": "Andriele",
				  "guestReference": "Externo",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "paid": true,
				  "paymentMethod": "CARD",
				  "paymentDate": "2026-03-19",
				  "paymentNotes": "Pago no balcao"
				}
				""".formatted(provider.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.clientName").value("Andriele"))
				.andExpect(jsonPath("$.providerId").value(provider.getId()))
				.andExpect(jsonPath("$.providerName").value("David"))
				.andExpect(jsonPath("$.paid").value(true))
				.andExpect(jsonPath("$.paymentMethod").value("CARD"))
				.andExpect(jsonPath("$.paymentDate").value("2026-03-19"))
				.andExpect(jsonPath("$.paymentNotes").value("Pago no balcao"));
	}

	@Test
	void shouldListMassageBookingsByDate() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Drenagem", "Agenda interna")
		);
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 20),
				LocalTime.of(17, 0),
				"Cacilda",
				"Apto 405",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				true,
				MassagePaymentMethod.CASH,
				LocalDate.of(2026, 3, 20),
				"Pago direto"
		));

		mockMvc.perform(get("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.param("bookingDate", "2026-03-20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].clientName").value("Cacilda"))
				.andExpect(jsonPath("$[0].providerName").value("Danuska"))
				.andExpect(jsonPath("$[0].paymentMethod").value("CASH"));
	}

	@Test
	void shouldFilterMassageBookingsByClientAndPaidStatus() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Danuska", "Drenagem", "Agenda interna")
		);
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 20),
				LocalTime.of(10, 0),
				"Carla Mendes",
				"Apto 101",
				"Relaxante",
				new BigDecimal("150.00"),
				provider,
				false,
				null,
				null,
				null
		));
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 20),
				LocalTime.of(11, 0),
				"Roberta Lima",
				"Apto 102",
				"Drenagem",
				new BigDecimal("180.00"),
				provider,
				true,
				MassagePaymentMethod.PIX,
				LocalDate.of(2026, 3, 20),
				"Pago por chave"
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
		final MassageBooking booking = massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 21),
				LocalTime.of(17, 0),
				"Paula Souza",
				"Apto 203",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				false,
				null,
				null,
				null
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
				.andExpect(jsonPath("$.paymentNotes").value("Pago apos atendimento"));
	}

	@Test
	void shouldRejectPaidBookingWithoutPaymentData() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		final String payload = """
				{
				  "bookingDate": "2026-03-19",
				  "startTime": "17:00:00",
				  "clientName": "Andriele",
				  "guestReference": "Externo",
				  "treatment": "Relaxante",
				  "amount": 200.00,
				  "providerId": %d,
				  "paid": true
				}
				""".formatted(provider.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("paymentMethod is required when paid is true"));
	}

	@Test
	void shouldReturnConflictWhenProviderAlreadyHasBookingForSameSlot() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("David", "Relaxante", "98804-3392")
		);
		massageBookingRepository.save(MassageBooking.schedule(
				LocalDate.of(2026, 3, 19),
				LocalTime.of(17, 0),
				"Cacilda",
				"Apto 405",
				"Relaxante",
				new BigDecimal("200.00"),
				provider,
				true,
				MassagePaymentMethod.CARD,
				LocalDate.of(2026, 3, 19),
				"Pago antecipado"
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
				  "paid": false,
				  "paymentMethod": null,
				  "paymentDate": null,
				  "paymentNotes": null
				}
				""".formatted(provider.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Massage provider already has a booking for the selected date and time."
				));
	}

	@Test
	void shouldReturnConflictWhenCreatingBookingForInactiveProvider() throws Exception {
		final MassageProvider provider = massageProviderRepository.save(
				MassageProvider.create("Juliana", "Premium", "Agenda interna")
		);
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
				  "paid": true,
				  "paymentMethod": "CASH",
				  "paymentDate": "2026-03-19",
				  "paymentNotes": "Pago na recepcao"
				}
				""".formatted(provider.getId());

		mockMvc.perform(post("/api/v1/massages/bookings")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Inactive massage providers cannot receive bookings."
				));
	}

	private String bearerToken() {
		return "Bearer " + accessToken;
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
