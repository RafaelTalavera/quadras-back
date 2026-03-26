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

## Validacion ejecutada
- Test focalizado:
  - `./mvnw -q -Dtest=TourControllerTest test`
- Cubre:
  - superposicion permitida
  - seleccion de item del proveedor en booking
  - pago
  - cancelacion
  - resumen por proveedor
  - alta y edicion de proveedor con catalogo
