# TOURS BACKEND IMPLEMENTATION

## Alcance
- Nuevo dominio backend para `tours` y `travel`.
- Soporta:
  - proveedores
  - catalogo de destinos / traslados por proveedor
  - agendamiento
  - edicion
  - cancelacion
  - registro de pago
  - resumen por proveedor
  - resumen estandar de periodo

## Endpoints
- `GET /api/v1/tours/providers`
- `POST /api/v1/tours/providers`
- `PUT /api/v1/tours/providers/{providerId}`
- `GET /api/v1/tours/bookings`
- `POST /api/v1/tours/bookings`
- `PUT /api/v1/tours/bookings/{bookingId}`
- `PATCH /api/v1/tours/bookings/{bookingId}/payment`
- `PATCH /api/v1/tours/bookings/{bookingId}/cancel`
- `GET /api/v1/tours/reports/providers/summary`
- `GET /api/v1/tours/reports/summary`

## Reglas implementadas
- Los bookings pueden superponerse.
- Cada proveedor puede tener multiples items predefinidos de tipo `TOUR` o `TRAVEL`.
- Cada booking guarda:
  - `serviceType`
  - `startAt`
  - `endAt`
  - `clientName`
  - `guestReference`
  - `provider`
  - `providerOffering` opcional con snapshot de nombre
  - `amount`
  - `commissionPercent`
  - `commissionAmount`
  - `description`
  - pago
  - auditoria
- La comision se recalcula desde:
  - `amount`
  - `commissionPercent`
- Un booking cancelado:
  - no puede editarse
  - no puede pagarse
  - no puede cancelarse de nuevo
- El resumen estandar de periodo:
  - exige `dateFrom` y `dateTo`
  - rechaza rangos invertidos
  - limita el rango a `93` dias
  - excluye cancelados de totales economicos y horas
  - mantiene `cancelledCount` como control operativo

## Contrato de resumen estandar
- `GET /api/v1/tours/reports/summary`
- `GET /api/v1/tours/reports/summary/details`
- Devuelve un `TourSummaryReportDto` con:
  - `scheduledCount`
  - `cancelledCount`
  - `paidCount`
  - `pendingCount`
  - `totalHours`
  - `grossAmount`
  - `paidAmount`
  - `pendingAmount`
  - `commissionAmount`
  - `netAmount`
  - `averageTicket`
  - `providerBreakdown`
  - `serviceTypeBreakdown`
  - `paymentMethodBreakdown`
- Cada breakdown devuelve:
  - `code`
  - `label`
  - `active` cuando aplica
  - `scheduledCount`
  - `paidCount`
  - `pendingCount`
  - `totalHours`
  - `grossAmount`
  - `paidAmount`
  - `pendingAmount`
  - `commissionAmount`
- El endpoint de detalle recibe:
  - `groupBy` = `PROVIDER` | `SERVICE_TYPE` | `PAYMENT_METHOD`
  - `code`
  - `dateFrom`
  - `dateTo`
- Devuelve:
  - metadata del grupo seleccionado
  - resumen del grupo
  - lista de bookings activos que explican la fila del resumen

## Frontend estatico incluido
- Pantalla dedicada:
  - `src/main/resources/static/tours-summary.html`
- Script:
  - `src/main/resources/static/tours-summary.js`
- Tema reutilizado:
  - `src/main/resources/static/brand/theme.css`
- Alcance:
  - filtro por periodo
  - KPIs principales
  - tablas por prestador, tipo de servicio y medio de pago
  - click en filas para abrir detalle
  - ventana emergente con items del grupo seleccionado

## Persistencia
- Migracion Flyway:
  - `V10__create_tours_domain.sql`
  - `V11__add_tour_provider_offerings.sql`
- Tablas:
  - `tour_providers`
  - `tour_provider_offerings`
  - `tour_bookings`

## Archivos principales
- `src/main/java/com/axioma/quadras/domain/model/TourProvider.java`
- `src/main/java/com/axioma/quadras/domain/model/TourProviderOffering.java`
- `src/main/java/com/axioma/quadras/domain/model/TourBooking.java`
- `src/main/java/com/axioma/quadras/service/TourProviderService.java`
- `src/main/java/com/axioma/quadras/service/TourBookingService.java`
- `src/main/java/com/axioma/quadras/controller/TourProviderController.java`
- `src/main/java/com/axioma/quadras/controller/TourBookingController.java`
- `src/main/java/com/axioma/quadras/controller/TourReportController.java`
- `src/main/java/com/axioma/quadras/domain/dto/{TourSummaryReportDto,TourSummaryBreakdownDto}.java`

## Validacion ejecutada
- Test focalizado:
  - `./mvnw -q -Dtest=TourControllerTest test`
- Cubre:
  - superposicion permitida
  - seleccion de item del proveedor en booking
  - pago
  - cancelacion
  - resumen por proveedor
  - resumen estandar de periodo
  - detalle por prestador / tipo / medio de pago
  - alta y edicion de proveedor con catalogo
