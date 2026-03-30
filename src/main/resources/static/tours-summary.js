const state = {
	summary: null,
	modalOpen: false
};

const filterFormElement = document.getElementById("summary-filter-form");
const periodLabelElement = document.getElementById("summary-period-label");
const statusBannerElement = document.getElementById("summary-status-banner");
const providerBreakdownBodyElement = document.querySelector("#provider-breakdown-table tbody");
const serviceBreakdownBodyElement = document.querySelector("#service-breakdown-table tbody");
const paymentBreakdownBodyElement = document.querySelector("#payment-breakdown-table tbody");
const modalBackdropElement = document.getElementById("details-modal-backdrop");
const modalCloseElement = document.getElementById("details-modal-close");
const modalTitleElement = document.getElementById("details-modal-title");
const modalSubtitleElement = document.getElementById("details-modal-subtitle");
const modalStatusElement = document.getElementById("details-modal-status");
const modalItemsElement = document.getElementById("details-modal-items");

const detailMetricElements = {
	scheduled: document.getElementById("details-metric-scheduled"),
	hours: document.getElementById("details-metric-hours"),
	gross: document.getElementById("details-metric-gross")
};

const metricElements = {
	scheduled: document.getElementById("metric-scheduled"),
	cancelled: document.getElementById("metric-cancelled"),
	hours: document.getElementById("metric-hours"),
	gross: document.getElementById("metric-gross"),
	paid: document.getElementById("metric-paid"),
	pending: document.getElementById("metric-pending"),
	commission: document.getElementById("metric-commission"),
	net: document.getElementById("metric-net"),
	average: document.getElementById("metric-average")
};

document.addEventListener("DOMContentLoaded", async () => {
	initializeDefaultRange();
	renderPeriodLabel();
	renderSummary(null);
	filterFormElement.addEventListener("submit", submitFilters);
	modalCloseElement.addEventListener("click", closeDetailsModal);
	modalBackdropElement.addEventListener("click", handleBackdropClick);
	document.addEventListener("keydown", handleEscapeClose);
	await loadSummary();
});

function initializeDefaultRange() {
	const today = new Date();
	const dateTo = toDateInputValue(today);
	const dateFrom = toDateInputValue(new Date(today.getFullYear(), today.getMonth(), 1));
	filterFormElement.elements.dateFrom.value = dateFrom;
	filterFormElement.elements.dateTo.value = dateTo;
}

async function submitFilters(event) {
	event.preventDefault();
	await loadSummary();
}

async function loadSummary() {
	const dateFrom = filterFormElement.elements.dateFrom.value;
	const dateTo = filterFormElement.elements.dateTo.value;
	renderPeriodLabel();
	updateStatusBanner("Carregando resumo do periodo...");

	try {
		const summary = await apiGet(`/api/v1/tours/reports/summary?dateFrom=${dateFrom}&dateTo=${dateTo}`);
		state.summary = summary;
		renderSummary(summary);
		updateStatusBanner(`Resumo carregado para ${formatShortDate(dateFrom)} a ${formatShortDate(dateTo)}.`);
	} catch (error) {
		state.summary = null;
		renderSummary(null);
		updateStatusBanner(error.message);
	}
}

function renderPeriodLabel() {
	const dateFrom = filterFormElement.elements.dateFrom.value;
	const dateTo = filterFormElement.elements.dateTo.value;
	periodLabelElement.textContent = `${formatShortDate(dateFrom)} a ${formatShortDate(dateTo)}`;
}

function renderSummary(summary) {
	metricElements.scheduled.textContent = String(summary?.scheduledCount || 0);
	metricElements.cancelled.textContent = String(summary?.cancelledCount || 0);
	metricElements.hours.textContent = formatHours(summary?.totalHours || 0);
	metricElements.gross.textContent = formatCurrency(summary?.grossAmount || 0);
	metricElements.paid.textContent = formatCurrency(summary?.paidAmount || 0);
	metricElements.pending.textContent = formatCurrency(summary?.pendingAmount || 0);
	metricElements.commission.textContent = formatCurrency(summary?.commissionAmount || 0);
	metricElements.net.textContent = formatCurrency(summary?.netAmount || 0);
	metricElements.average.textContent = formatCurrency(summary?.averageTicket || 0);

	renderStandardBreakdown(
		providerBreakdownBodyElement,
		summary?.providerBreakdown || [],
		7,
		"PROVIDER",
		(item) => `
			<td>
				<button
					type="button"
					class="table-link-button"
					data-detail-group="PROVIDER"
					data-detail-code="${escapeHtml(item.code || "")}"
					data-detail-label="${escapeHtml(item.label || "-")}"
				>
					${escapeHtml(item.label || "-")}
				</button>
				${item.active === false ? '<span class="report-subtag">Inativo</span>' : ""}
			</td>
			<td>${Number(item.scheduledCount || 0)}</td>
			<td>${Number(item.paidCount || 0)}</td>
			<td>${Number(item.pendingCount || 0)}</td>
			<td>${formatHours(item.totalHours || 0)}</td>
			<td>${formatCurrency(item.grossAmount || 0)}</td>
			<td>${formatCurrency(item.commissionAmount || 0)}</td>
		`
	);

	renderStandardBreakdown(
		serviceBreakdownBodyElement,
		summary?.serviceTypeBreakdown || [],
		7,
		"SERVICE_TYPE",
		(item) => `
			<td>
				<button
					type="button"
					class="table-link-button"
					data-detail-group="SERVICE_TYPE"
					data-detail-code="${escapeHtml(item.code || "")}"
					data-detail-label="${escapeHtml(item.label || "-")}"
				>
					${escapeHtml(item.label || "-")}
				</button>
			</td>
			<td>${Number(item.scheduledCount || 0)}</td>
			<td>${Number(item.paidCount || 0)}</td>
			<td>${Number(item.pendingCount || 0)}</td>
			<td>${formatHours(item.totalHours || 0)}</td>
			<td>${formatCurrency(item.grossAmount || 0)}</td>
			<td>${formatCurrency(item.commissionAmount || 0)}</td>
		`
	);

	renderStandardBreakdown(
		paymentBreakdownBodyElement,
		summary?.paymentMethodBreakdown || [],
		5,
		"PAYMENT_METHOD",
		(item) => `
			<td>
				<button
					type="button"
					class="table-link-button"
					data-detail-group="PAYMENT_METHOD"
					data-detail-code="${escapeHtml(item.code || "")}"
					data-detail-label="${escapeHtml(item.label || "-")}"
				>
					${escapeHtml(item.label || "-")}
				</button>
			</td>
			<td>${Number(item.paidCount || 0)}</td>
			<td>${formatHours(item.totalHours || 0)}</td>
			<td>${formatCurrency(item.grossAmount || 0)}</td>
			<td>${formatCurrency(item.commissionAmount || 0)}</td>
		`
	);
}

function renderStandardBreakdown(targetElement, items, emptyColspan, groupBy, renderRow) {
	if (!items.length) {
		targetElement.innerHTML = `
			<tr>
				<td colspan="${emptyColspan}" class="report-empty">Sem dados no periodo selecionado.</td>
			</tr>
		`;
		return;
	}

	targetElement.innerHTML = items.map((item) => `
		<tr>
			${renderRow(item)}
		</tr>
	`).join("");
	targetElement.querySelectorAll("[data-detail-group]").forEach((buttonElement) => {
		buttonElement.addEventListener("click", async () => {
			await openDetailsModal(
				buttonElement.getAttribute("data-detail-group") || groupBy,
				buttonElement.getAttribute("data-detail-code") || "",
				buttonElement.getAttribute("data-detail-label") || ""
			);
		});
	});
}

async function openDetailsModal(groupBy, code, fallbackLabel) {
	state.modalOpen = true;
	modalBackdropElement.classList.remove("is-hidden");
	modalBackdropElement.setAttribute("aria-hidden", "false");
	modalTitleElement.textContent = fallbackLabel || "Detalhes";
	modalSubtitleElement.textContent = buildModalSubtitle();
	modalStatusElement.textContent = "Carregando detalhes...";
	renderDetailSummary(null);
	renderDetailItems([]);

	try {
		const detail = await apiGet(
			`/api/v1/tours/reports/summary/details?groupBy=${encodeURIComponent(groupBy)}&code=${encodeURIComponent(code)}&dateFrom=${filterFormElement.elements.dateFrom.value}&dateTo=${filterFormElement.elements.dateTo.value}`
		);
		modalTitleElement.textContent = detail.label || fallbackLabel || "Detalhes";
		modalStatusElement.textContent = `${detail.items.length} item(ns) carregado(s).`;
		renderDetailSummary(detail.summary);
		renderDetailItems(detail.items || []);
	} catch (error) {
		modalStatusElement.textContent = error.message;
		renderDetailSummary(null);
		renderDetailItems([]);
	}
}

function closeDetailsModal() {
	state.modalOpen = false;
	modalBackdropElement.classList.add("is-hidden");
	modalBackdropElement.setAttribute("aria-hidden", "true");
}

function handleBackdropClick(event) {
	if (event.target === modalBackdropElement) {
		closeDetailsModal();
	}
}

function handleEscapeClose(event) {
	if (event.key === "Escape" && state.modalOpen) {
		closeDetailsModal();
	}
}

function buildModalSubtitle() {
	return `${formatShortDate(filterFormElement.elements.dateFrom.value)} a ${formatShortDate(filterFormElement.elements.dateTo.value)}`;
}

function renderDetailSummary(summary) {
	detailMetricElements.scheduled.textContent = String(summary?.scheduledCount || 0);
	detailMetricElements.hours.textContent = formatHours(summary?.totalHours || 0);
	detailMetricElements.gross.textContent = formatCurrency(summary?.grossAmount || 0);
}

function renderDetailItems(items) {
	if (!items.length) {
		modalItemsElement.innerHTML = `
			<tr>
				<td colspan="6" class="report-empty">Sem itens ativos para o recorte selecionado.</td>
			</tr>
		`;
		return;
	}

	modalItemsElement.innerHTML = items.map((item) => {
		const paymentCopy = item.paid
			? `Pago${item.paymentMethod ? ` - ${escapeHtml(item.paymentMethod)}` : ""}`
			: "Pendente";
		return `
			<tr>
				<td>${formatDateTimeRange(item.startAt, item.endAt)}</td>
				<td>
					<strong>${escapeHtml(item.clientName || "-")}</strong>
					<div class="table-secondary-copy">${escapeHtml(item.guestReference || "-")}</div>
				</td>
				<td>
					<strong>${escapeHtml(item.serviceType || "-")}</strong>
					<div class="table-secondary-copy">${escapeHtml(item.providerOfferingName || item.description || "-")}</div>
				</td>
				<td>${escapeHtml(item.providerName || "-")}</td>
				<td>${formatCurrency(item.amount || 0)}</td>
				<td>
					<span class="${item.paid ? "paid-tag" : "pending-tag"}">${paymentCopy}</span>
				</td>
			</tr>
		`;
	}).join("");
}

async function apiGet(url) {
	const response = await fetch(url, {
		headers: buildHeaders()
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
		"Nao foi possivel carregar o resumo."
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

function toDateInputValue(date) {
	return [
		date.getFullYear(),
		String(date.getMonth() + 1).padStart(2, "0"),
		String(date.getDate()).padStart(2, "0")
	].join("-");
}

function formatShortDate(value) {
	return new Intl.DateTimeFormat("pt-BR", {
		day: "2-digit",
		month: "2-digit",
		year: "numeric"
	}).format(new Date(`${value}T00:00:00`));
}

function formatDateTimeRange(startAt, endAt) {
	const start = new Date(startAt);
	const end = new Date(endAt);
	const dateCopy = new Intl.DateTimeFormat("pt-BR", {
		day: "2-digit",
		month: "2-digit"
	}).format(start);
	const startTime = new Intl.DateTimeFormat("pt-BR", {
		hour: "2-digit",
		minute: "2-digit"
	}).format(start);
	const endTime = new Intl.DateTimeFormat("pt-BR", {
		hour: "2-digit",
		minute: "2-digit"
	}).format(end);
	return `${dateCopy} ${startTime}-${endTime}`;
}

function formatCurrency(value) {
	return new Intl.NumberFormat("pt-BR", {
		style: "currency",
		currency: "BRL"
	}).format(Number(value || 0));
}

function formatHours(value) {
	return `${Number(value || 0).toFixed(2)}h`;
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("\"", "&quot;")
		.replaceAll("'", "&#39;");
}
