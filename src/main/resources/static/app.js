const state = {
	selectedDate: toDateInputValue(new Date()),
	calendarReferenceDate: new Date(),
	bookingsForSelectedDate: [],
	bookingsMonth: [],
	providers: []
};

const currentDateElement = document.getElementById("current-date");
const currentTimeElement = document.getElementById("current-time");
const selectedDateLabelElement = document.getElementById("selected-date-label");
const todayListElement = document.getElementById("today-list");
const statusBannerElement = document.getElementById("status-banner");
const metricTotalElement = document.getElementById("metric-total");
const metricUpcomingElement = document.getElementById("metric-upcoming");
const metricPaidElement = document.getElementById("metric-paid");
const providerSelectElement = document.getElementById("provider-select");
const providerListElement = document.getElementById("provider-list");
const calendarGridElement = document.getElementById("calendar-grid");
const calendarTitleElement = document.getElementById("calendar-title");
const bookingFormElement = document.getElementById("booking-form");
const formFeedbackElement = document.getElementById("form-feedback");
const paidCheckboxElement = document.getElementById("paid-checkbox");
const paymentFieldElements = document.querySelectorAll("[data-payment-field]");

document.addEventListener("DOMContentLoaded", async () => {
	initializeClock();
	initializeFormDefaults();
	togglePaymentFields();
	renderSelectedDateLabel();
	state.calendarReferenceDate = monthReferenceFromIso(state.selectedDate);
	renderCalendar(state.calendarReferenceDate, []);

	paidCheckboxElement.addEventListener("change", togglePaymentFields);
	bookingFormElement.addEventListener("submit", submitBooking);

	await Promise.all([
		loadProviders(),
		loadMonthBookings()
	]);
	await loadBookingsForSelectedDate(state.selectedDate);
});

function initializeClock() {
	updateClock();
	window.setInterval(() => {
		updateClock();
		renderBookings();
	}, 60000);
}

function updateClock() {
	const now = new Date();
	currentDateElement.textContent = new Intl.DateTimeFormat("pt-BR", {
		weekday: "long",
		day: "2-digit",
		month: "long"
	}).format(now);
	currentTimeElement.textContent = new Intl.DateTimeFormat("pt-BR", {
		hour: "2-digit",
		minute: "2-digit"
	}).format(now);
}

function initializeFormDefaults() {
	bookingFormElement.elements.bookingDate.value = state.selectedDate;
	bookingFormElement.elements.startTime.value = nextQuarterHour();
	bookingFormElement.elements.paymentDate.value = state.selectedDate;
}

function renderSelectedDateLabel() {
	const formatted = formatLongDate(state.selectedDate);
	const isToday = state.selectedDate === toDateInputValue(new Date());
	selectedDateLabelElement.textContent = isToday ? `${formatted} - hoje` : formatted;
}

async function loadProviders() {
	try {
		const providers = await apiGet("/api/v1/massages/providers?activeOnly=true");
		state.providers = Array.isArray(providers) ? providers : [];
		renderProviders();
		renderProviderOptions();
	} catch (error) {
		state.providers = [];
		renderProviders();
		updateStatusBanner(error.message);
	}
}

async function loadMonthBookings() {
	try {
		const referenceDate = state.calendarReferenceDate || monthReferenceFromIso(state.selectedDate);
		const dateFrom = toDateInputValue(new Date(referenceDate.getFullYear(), referenceDate.getMonth(), 1));
		const dateTo = toDateInputValue(new Date(referenceDate.getFullYear(), referenceDate.getMonth() + 1, 0));
		const bookings = await apiGet(
			`/api/v1/massages/bookings?dateFrom=${dateFrom}&dateTo=${dateTo}`
		);
		state.bookingsMonth = Array.isArray(bookings) ? bookings : [];
		renderCalendar(state.calendarReferenceDate, state.bookingsMonth);
	} catch (error) {
		state.bookingsMonth = [];
		renderCalendar(state.calendarReferenceDate, []);
		updateStatusBanner(error.message);
	}
}

async function loadBookingsForSelectedDate(date) {
	try {
		const bookings = await apiGet(`/api/v1/massages/bookings?bookingDate=${date}`);
		state.selectedDate = date;
		state.bookingsForSelectedDate = Array.isArray(bookings) ? bookings : [];
		renderSelectedDateLabel();
		renderCalendar(state.calendarReferenceDate, state.bookingsMonth);
		renderBookings();
		updateStatusBanner(`Agenda carregada para ${formatShortDate(date)}.`);
	} catch (error) {
		state.selectedDate = date;
		state.bookingsForSelectedDate = [];
		renderSelectedDateLabel();
		renderCalendar(state.calendarReferenceDate, state.bookingsMonth);
		renderBookings();
		updateStatusBanner(error.message);
	}
}

function renderBookings() {
	const nowMinutes = timeToMinutes(currentTimeElement.textContent);
	const isToday = state.selectedDate === toDateInputValue(new Date());
	const bookings = [...state.bookingsForSelectedDate]
		.sort((left, right) => String(left.startTime).localeCompare(String(right.startTime)));
	const upcoming = bookings.filter((booking) => !isToday || timeToMinutes(booking.startTime) >= nowMinutes);
	const paid = bookings.filter((booking) => booking.paid);

	metricTotalElement.textContent = String(bookings.length);
	metricUpcomingElement.textContent = String(upcoming.length);
	metricPaidElement.textContent = String(paid.length);

	if (!bookings.length) {
		todayListElement.innerHTML = `
			<article class="booking-item booking-item-empty">
				<p>Sem atendimentos para ${formatShortDate(state.selectedDate)}.</p>
			</article>
		`;
		return;
	}

	todayListElement.innerHTML = bookings.map((booking) => {
		const highlightClass = isToday && timeToMinutes(booking.startTime) >= nowMinutes
			? "booking-item-next"
			: "";
		const paymentCopy = booking.paid
			? `Pago${booking.paymentMethod ? ` - ${escapeHtml(booking.paymentMethod)}` : ""}`
			: "Pendente";
		return `
			<article class="booking-item ${highlightClass}">
				<div class="booking-time">
					<strong>${formatTime(booking.startTime)}</strong>
					<span>${escapeHtml(booking.providerName || "")}</span>
				</div>
				<div class="booking-main">
					<strong>${escapeHtml(booking.clientName || "")}</strong>
					<span>${escapeHtml(booking.treatment || "")} - ${escapeHtml(booking.guestReference || "")}</span>
				</div>
				<div class="booking-meta">
					<span>${formatCurrency(booking.amount)}</span>
					<span class="${booking.paid ? "paid-tag" : "pending-tag"}">${paymentCopy}</span>
				</div>
			</article>
		`;
	}).join("");
}

function renderProviders() {
	if (!state.providers.length) {
		providerListElement.innerHTML = `<p class="empty-copy">Sem prestadores ativos carregados.</p>`;
		return;
	}

	providerListElement.innerHTML = state.providers.map((provider) => `
		<article class="provider-item">
			<strong>${escapeHtml(provider.name)}</strong>
			<span>${escapeHtml(provider.specialty)}</span>
			<small>${escapeHtml(provider.contact)}</small>
		</article>
	`).join("");
}

function renderProviderOptions() {
	const options = ['<option value="">Selecione</option>']
		.concat(state.providers.map((provider) => (
			`<option value="${provider.id}">${escapeHtml(provider.name)} - ${escapeHtml(provider.specialty)}</option>`
		)));
	providerSelectElement.innerHTML = options.join("");
}

function renderCalendar(referenceDate, bookings) {
	const year = referenceDate.getFullYear();
	const month = referenceDate.getMonth();
	const firstDay = new Date(year, month, 1);
	const lastDay = new Date(year, month + 1, 0);
	const firstWeekday = (firstDay.getDay() + 6) % 7;
	const counts = bookings.reduce((accumulator, booking) => {
		if (!booking.bookingDate) {
			return accumulator;
		}
		const date = new Date(`${booking.bookingDate}T00:00:00`);
		if (date.getFullYear() !== year || date.getMonth() !== month) {
			return accumulator;
		}
		accumulator[booking.bookingDate] = (accumulator[booking.bookingDate] || 0) + 1;
		return accumulator;
	}, {});

	calendarTitleElement.textContent = new Intl.DateTimeFormat("pt-BR", {
		month: "long",
		year: "numeric"
	}).format(referenceDate);

	const weekdayLabels = ["Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"];
	const cells = weekdayLabels.map((label) => `<div class="calendar-label">${label}</div>`);

	for (let index = 0; index < firstWeekday; index += 1) {
		cells.push(`<div class="calendar-day calendar-day-muted"></div>`);
	}

	for (let day = 1; day <= lastDay.getDate(); day += 1) {
		const date = new Date(year, month, day);
		const isoDate = toDateInputValue(date);
		const count = counts[isoDate] || 0;
		const isToday = isoDate === toDateInputValue(new Date());
		const isSelected = isoDate === state.selectedDate;
		cells.push(`
			<button
				type="button"
				class="calendar-day ${isToday ? "calendar-day-today" : ""} ${isSelected ? "calendar-day-selected" : ""}"
				data-calendar-date="${isoDate}"
				aria-label="Ver atendimentos do dia ${day}"
			>
				<strong>${day}</strong>
				<span>${count}</span>
			</button>
		`);
	}

	calendarGridElement.innerHTML = cells.join("");
	calendarGridElement.querySelectorAll("[data-calendar-date]").forEach((element) => {
		element.addEventListener("click", () => {
			const date = element.getAttribute("data-calendar-date");
			if (date) {
				loadBookingsForSelectedDate(date);
			}
		});
	});
}

async function submitBooking(event) {
	event.preventDefault();
	formFeedbackElement.textContent = "Guardando atendimento...";

	const isPaid = bookingFormElement.elements.paid.checked;
	const payload = {
		bookingDate: bookingFormElement.elements.bookingDate.value,
		startTime: bookingFormElement.elements.startTime.value,
		clientName: bookingFormElement.elements.clientName.value.trim(),
		guestReference: bookingFormElement.elements.guestReference.value.trim(),
		treatment: bookingFormElement.elements.treatment.value.trim(),
		amount: Number(bookingFormElement.elements.amount.value).toFixed(2),
		providerId: Number(bookingFormElement.elements.providerId.value),
		paid: isPaid,
		paymentMethod: isPaid ? bookingFormElement.elements.paymentMethod.value || null : null,
		paymentDate: isPaid ? bookingFormElement.elements.paymentDate.value || null : null,
		paymentNotes: isPaid ? bookingFormElement.elements.paymentNotes.value.trim() || null : null
	};

	try {
		await apiPost("/api/v1/massages/bookings", payload);
		formFeedbackElement.textContent = "Atendimento guardado com sucesso.";
		state.selectedDate = payload.bookingDate;
		state.calendarReferenceDate = monthReferenceFromIso(payload.bookingDate);
		bookingFormElement.reset();
		initializeFormDefaults();
		togglePaymentFields();
		await Promise.all([
			loadMonthBookings(),
			loadBookingsForSelectedDate(payload.bookingDate)
		]);
	} catch (error) {
		formFeedbackElement.textContent = error.message;
	}
}

function togglePaymentFields() {
	const enabled = paidCheckboxElement.checked;
	paymentFieldElements.forEach((field) => {
		field.classList.toggle("is-disabled", !enabled);
		field.querySelectorAll("input, select").forEach((input) => {
			input.disabled = !enabled;
			if (!enabled) {
				input.value = "";
			}
		});
	});
	if (enabled && !bookingFormElement.elements.paymentDate.value) {
		bookingFormElement.elements.paymentDate.value = bookingFormElement.elements.bookingDate.value || state.selectedDate;
	}
}

async function apiGet(url) {
	const response = await fetch(url, {
		headers: buildHeaders()
	});
	return handleResponse(response);
}

async function apiPost(url, body) {
	const response = await fetch(url, {
		method: "POST",
		headers: buildHeaders({ "Content-Type": "application/json" }),
		body: JSON.stringify(body)
	});
	return handleResponse(response);
}

function buildHeaders(extraHeaders = {}) {
	const token = readToken();
	const headers = { ...extraHeaders };
	if (token) {
		headers.Authorization = `Bearer ${token}`;
	}
	return headers;
}

async function handleResponse(response) {
	if (response.ok) {
		return response.json();
	}

	const message = await extractResponseErrorMessage(
		response,
		"No fue posible completar la operacion."
	);
	throw new Error(message);
}

async function extractResponseErrorMessage(response, fallbackMessage) {
	const responseText = await response.text();
	if (responseText) {
		try {
			const body = JSON.parse(responseText);
			return body.message || body.error || fallbackMessage;
		} catch (_error) {
			const trimmedText = responseText.trim();
			if (trimmedText) {
				return trimmedText;
			}
		}
	}
	if (response.status === 401) {
		return "Falta autenticacao. Faca login e salve o JWT no localStorage.";
	}
	return fallbackMessage;
}

function readToken() {
	return localStorage.getItem("authToken")
		|| localStorage.getItem("token")
		|| localStorage.getItem("jwt");
}

function updateStatusBanner(message) {
	statusBannerElement.textContent = message;
}

function nextQuarterHour() {
	const date = new Date();
	date.setMinutes(Math.ceil(date.getMinutes() / 15) * 15, 0, 0);
	return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

function timeToMinutes(value) {
	const [hours = "0", minutes = "0"] = String(value).split(":");
	return Number(hours) * 60 + Number(minutes);
}

function toDateInputValue(date) {
	return [
		date.getFullYear(),
		String(date.getMonth() + 1).padStart(2, "0"),
		String(date.getDate()).padStart(2, "0")
	].join("-");
}

function monthReferenceFromIso(value) {
	const [year = "0", month = "1"] = String(value).split("-");
	return new Date(Number(year), Number(month) - 1, 1);
}

function formatTime(value) {
	return String(value || "").slice(0, 5);
}

function formatCurrency(value) {
	return new Intl.NumberFormat("pt-BR", {
		style: "currency",
		currency: "BRL"
	}).format(Number(value || 0));
}

function formatLongDate(value) {
	return new Intl.DateTimeFormat("pt-BR", {
		weekday: "long",
		day: "2-digit",
		month: "long",
		year: "numeric"
	}).format(new Date(`${value}T00:00:00`));
}

function formatShortDate(value) {
	return new Intl.DateTimeFormat("pt-BR", {
		day: "2-digit",
		month: "2-digit",
		year: "numeric"
	}).format(new Date(`${value}T00:00:00`));
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("\"", "&quot;")
		.replaceAll("'", "&#39;");
}
