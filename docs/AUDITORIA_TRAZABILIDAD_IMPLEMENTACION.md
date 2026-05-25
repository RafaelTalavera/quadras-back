# AUDITORIA Y TRAZABILIDAD - IMPLEMENTACION

## Objetivo
Implementar historial consultable por entidad para responder:
- que cambio
- cuando cambio
- que usuario hizo el cambio

La solucion cubre modulos operativos y catalogos principales en backend `quadras`, con consumo UI en `quedras-front`.

## Diseno backend
- Persistencia central en tabla `audit_events`
- Registro desde capa de servicio mediante `AuditTrailService`
- Resolucion del actor autenticado mediante `CurrentActorService`
- Serializacion JSON consistente mediante `JacksonConfig`

Cada evento persiste:
- `module`
- `entity_type`
- `entity_id`
- `action`
- `summary`
- `changes_json`
- `before_json`
- `after_json`
- `created_at`
- `created_by`

## Criterios de snapshot
- Se guarda estado anterior y posterior por entidad
- El diff se calcula por campo para simplificar la UI
- No se persisten blobs ni payloads binarios
- En mantenimiento, los adjuntos solo registran metadatos: nombre, tipo, tamano, fecha y usuario

## Cobertura backend

### Modulos operativos
- `GET /api/v1/reservations/{id}/audit`
- `GET /api/v1/courts/bookings/{id}/audit`
- `GET /api/v1/massages/bookings/{id}/audit`
- `GET /api/v1/maintenance/orders/{id}/audit`
- `GET /api/v1/tours/bookings/{id}/audit`

### Catalogos principales
- `GET /api/v1/courts/rates/{id}/audit`
- `GET /api/v1/courts/materials/{id}/audit`
- `GET /api/v1/courts/partner-coaches/{id}/audit`
- `GET /api/v1/massages/providers/{id}/audit`
- `GET /api/v1/massages/providers/{providerId}/therapists/{therapistId}/audit`
- `GET /api/v1/maintenance/locations/{id}/audit`
- `GET /api/v1/maintenance/providers/{id}/audit`
- `GET /api/v1/tours/providers/{id}/audit`
- `GET /api/v1/tours/providers/{providerId}/offerings/{offeringId}/audit`

## Cobertura frontend
- Modelo comun: `lib/core/audit/audit_models.dart`
- UI reutilizable: `lib/core/audit/audit_timeline_dialog.dart`
- Acceso `Historial` agregado en:
  - reservas y agenda
  - canchas y configuracion de canchas
  - masajes
  - mantenimiento
  - tours

## Consideraciones operativas
- Si el usuario no esta autenticado, el actor se normaliza segun la politica definida en `CurrentActorService`
- Los eventos de auditoria no reemplazan `createdBy` o `updatedBy`; los complementan
- Nuevas acciones de negocio deben registrar auditoria en el mismo punto de aplicacion de reglas

## Validacion minima recomendada
- `.\mvnw.cmd -q -DskipTests compile`
- `.\mvnw.cmd -q "-Dtest=ReservationControllerTest,ReservationDtoTest,ReservationTest" test`
- `flutter analyze lib/core/audit`
- `flutter analyze lib/features/reservations lib/features/courts lib/features/massages lib/features/maintenance lib/features/tours`
