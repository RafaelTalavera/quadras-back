# PLAN BACKEND QUADRAS

## Objetivo
- Crear un dominio backend propio para `Quadras` y dejar de depender de `reservations` genericas para la operacion de canchas.

## Analisis del estado actual
- `Reservation` solo soporta:
  - nombre de huesped
  - fecha
  - hora inicio
  - hora fin
  - notas
  - estado basico
- No soporta:
  - tipo de cliente
  - tarifas editables
  - materiales
  - pago con modalidad
  - auditoria operativa completa por actor
  - snapshot de precios aplicados

## Reglas de negocio a soportar
- `GUEST`:
  - cancha sin costo
  - materiales en prestamo sin costo
- `EXTERNAL`:
  - paga tarifa de cancha
  - paga materiales prestados por uso
  - tarifa diurna editable, valor inicial `60`
  - tarifa nocturna editable, valor inicial `80`
- `PARTNER_COACH`:
  - paga tarifa diferencial editable
- `VIP`:
  - tipo separado de `GUEST`
  - no paga tarifa de cancha
  - debe computarse en control de horas y resumenes
- Materiales iniciales:
  - `RACKET` = `20` por unidad
  - `BALL` = `10` por unidad
- Pago:
  - reserva pagada o no pagada
  - registro de modalidad y observaciones
- Horas:
  - los resumenes deben informar horas reservadas
  - deben existir totales por tipo de cliente
- Regla solar:
  - el backend debe resolver `DAY` y `NIGHT` usando estimaciones de amanecer y atardecer de Florianopolis
  - la regla debe variar por estacion del anio
  - referencia operativa inicial tomada de timeanddate.com para 2026:
    - 20/03/2026: amanecer `06:17`, atardecer `18:25`
    - 21/06/2026: amanecer `07:04`, atardecer `17:27`
    - 21/12/2026: amanecer `05:15`, atardecer `19:09`

## Diseno de dominio propuesto

### Enumeraciones
- `CourtCustomerType`
- `CourtPricingPeriod`
- `CourtBookingStatus`
- `CourtPaymentMethod`
- `CourtMaterialCode`

### Entidades
- `CourtBooking`
- `CourtBookingMaterial`
- `CourtRate`
- `CourtMaterialSetting`

### Regla clave
- La reserva debe guardar snapshot del valor aplicado al momento de crear o editar:
  - `courtAmount`
  - `materialsAmount`
  - `totalAmount`
  - items de materiales con `unitPrice`
  - `sunriseEstimate`
  - `sunsetEstimate`
  - `durationMinutes`
- Motivo:
  - si la tarifa cambia luego, la reserva historica no debe recalcularse sola

## Modelo de persistencia sugerido

### Tabla `court_rates`
- `id`
- `customer_type`
- `pricing_period`
- `amount`
- `active`
- `created_at`
- `updated_at`
- `updated_by`

### Tabla `court_material_settings`
- `id`
- `code`
- `label`
- `unit_price`
- `charge_guest`
- `charge_vip`
- `charge_external`
- `charge_partner_coach`
- `active`
- `created_at`
- `updated_at`
- `updated_by`

### Tabla `court_bookings`
- `id`
- `booking_date`
- `start_time`
- `end_time`
- `duration_minutes`
- `customer_name`
- `customer_reference`
- `customer_type`
- `pricing_period`
- `sunrise_estimate`
- `sunset_estimate`
- `court_amount`
- `materials_amount`
- `total_amount`
- `paid`
- `payment_method`
- `payment_date`
- `payment_notes`
- `status`
- `cancellation_notes`
- `created_at`
- `updated_at`
- `cancelled_at`
- `created_by`
- `updated_by`
- `cancelled_by`

### Tabla `court_booking_materials`
- `id`
- `court_booking_id`
- `material_code`
- `material_label`
- `quantity`
- `unit_price`
- `total_price`

## Endpoints propuestos

### Configuracion
- `GET /api/v1/courts/rates`
- `PUT /api/v1/courts/rates/{id}`
- `GET /api/v1/courts/materials`
- `PUT /api/v1/courts/materials/{id}`

### Reservas
- `GET /api/v1/courts/bookings`
- `POST /api/v1/courts/bookings`
- `PUT /api/v1/courts/bookings/{id}`
- `PATCH /api/v1/courts/bookings/{id}/payment`
- `PATCH /api/v1/courts/bookings/{id}/cancel`

### Reportes
- `GET /api/v1/courts/reports/summary`
- `GET /api/v1/courts/reports/daily`
- Los DTOs de resumen deben incluir:
  - `totalHours`
  - `guestHours`
  - `vipHours`
  - `externalHours`
  - `partnerCoachHours`

## DTO minimo de reserva
- `bookingDate`
- `startTime`
- `endTime`
- `durationMinutes`
- `customerName`
- `customerReference`
- `customerType`
- `pricingPeriod`
- `sunriseEstimate`
- `sunsetEstimate`
- `courtAmount`
- `materialsAmount`
- `totalAmount`
- `paid`
- `paymentMethod`
- `paymentDate`
- `paymentNotes`
- `materials`

## Validaciones de negocio
- No permitir solapamientos con reservas activas.
- Una reserva cancelada no puede:
  - editarse
  - cobrarse
  - cancelarse nuevamente
- Si `paid = true`, `paymentMethod` debe ser obligatorio.
- Si el tipo es `GUEST`, `courtAmount` y `materialsAmount` deben quedar en `0`.
- Si el tipo es `VIP`, `courtAmount` debe quedar en `0`.
- Si el tipo es `EXTERNAL`, los materiales deben valorarse segun configuracion activa.
- El periodo `DAY` o `NIGHT` debe resolverse en backend para no depender solo del cliente.
- No permitir importes negativos ni cantidades menores a `0`.
- Los resumenes deben poder reconstruir horas por tipo de usuario.

## Auditoria
- Tomar usuario autenticado desde JWT.
- Persistir:
  - `createdBy`
  - `updatedBy`
  - `cancelledBy`
- Persistir timestamps:
  - `createdAt`
  - `updatedAt`
  - `cancelledAt`

## Fases de implementacion
1. Crear migracion SQL para tablas de Quadras.
2. Crear enums, entidades y repositories.
3. Implementar servicios de tarifas y materiales.
4. Implementar servicio de reservas con calculo y snapshot de importes.
5. Exponer controllers y DTOs.
6. Agregar tests de controller y servicio.
7. Ejecutar smoke real con frontend.

## Tests minimos
- Crear reserva `GUEST` con total `0`.
- Crear reserva `VIP` con total `0` y horas acumulables.
- Crear reserva `EXTERNAL` diurna con total de cancha correcto.
- Crear reserva `EXTERNAL` nocturna con materiales.
- Crear reserva `PARTNER_COACH` con tarifa diferencial.
- Registrar pago con modalidad.
- Cancelar reserva y bloquear nuevas mutaciones.
- Verificar que cambio de tarifa no altera reservas historicas.
- Verificar que el corte `DAY` / `NIGHT` cambie con la referencia solar guardada.

## Riesgos abiertos
- Confirmar si `PARTNER_COACH` paga o no materiales.
- Definir si la estimacion solar sera:
  - calculada localmente por fecha
  - almacenada en tabla mensual
  - resuelta por integracion externa
- Confirmar si `COURTESY` debe ser modalidad de pago o solo total `0`.

## Criterio de cierre
- Existe dominio propio de Quadras.
- Las reservas ya no dependen de `Reservation`.
- El contrato backend permite implementar la UI de Quadras sin logica inventada en frontend.
