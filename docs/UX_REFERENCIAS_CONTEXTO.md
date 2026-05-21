# Memoria de Contexto - UX sin codigos como referencia principal

Ultima actualizacion: 2026-05-19

## Objetivo
Adaptar COSTANORTE para que la operacion diaria use nombres, estados, fechas, clientes, servicios y responsables como referencia principal.

Regla funcional:
- `code` no se elimina del sistema.
- `code` queda como dato tecnico para trazabilidad, auditoria, exportaciones e integraciones.
- La API y la UI deben priorizar claves semanticas y etiquetas de negocio.

## Criterio de migracion
- Backend:
  - mantener `id` y `code` para persistencia y compatibilidad;
  - dejar de exigir `code` cuando no aporta valor operativo;
  - introducir `groupKey` en reportes agregados para evitar que la semantica de negocio dependa del texto `code`.
- Frontend:
  - mostrar `label` o `name` como texto principal;
  - dejar `code` como referencia interna secundaria (`Ref.` o metadata);
  - consumir `groupKey` para drill-down de reportes.

## Hitos ya cerrados
### Hito UX-01 - Maintenance locations
- `CreateMaintenanceLocationDto` y `UpdateMaintenanceLocationDto` ya no obligan `code`.
- `MaintenanceLocationService` genera referencia interna automaticamente si falta.
- La UI de mantenimiento ya usa `Referencia interna` opcional.
- En ordenes y listados de mantenimiento se prioriza el nombre del local; el codigo queda secundario.

### Hito UX-02 - Maintenance reports
- `MaintenanceSummaryBreakdownDto` y `MaintenanceSummaryDetailDto` incluyen `groupKey`.
- `MaintenanceReportController` acepta `groupKey` y mantiene `code` por compatibilidad.
- `MaintenanceReportService` ya resuelve detalle por `groupKey`.

### Hito UX-03 - Tours reports
- `TourSummaryBreakdownDto` y `TourSummaryDetailDto` ahora incluyen `groupKey`.
- `TourReportController` acepta `groupKey` y conserva `code` como fallback legacy.
- `TourBookingService.summaryDetails(...)` ya trabaja con `groupKey` como entrada semantica.
- El frontend de tours ya consume `groupKey` para abrir el detalle del resumen.

### Hito UX-04 - Tours visual cleanup
- Las tarjetas operativas de `Tours e Viagens` ahora priorizan `Prestador` y `Hospede / unidade` como contexto de negocio.
- El modal de detalle del resumen ya separa cliente, referencia operacional, prestador y servicio en metadata mas clara.
- El formulario de booking usa `Hospede / unidade` y `Servico do fornecedor` en lugar de etiquetas mas tecnicas o ambiguas.

### Hito UX-05 - Courts / Tennis evaluation
- `courts` no exponia `code` como lenguaje principal visible, pero si lo usaba como clave interna del breakdown.
- El backend de `courts` ahora publica `groupKey` en `CourtSummaryBreakdownDto`, manteniendo `code` por compatibilidad.
- `tennis` ya usa `groupKey` para drill-down local del resumen y seleccion interna de filas.
- El formulario de reservas de `tennis` ahora usa `Hospede / unidade` como etiqueta operacional visible.

## Decision UX-06
- Se formaliza que `groupKey` es la clave primaria para resumenes nuevos.
- `code` queda permitido solo como compatibilidad temporal en payloads y request params legacy.
- Documento fuente: `docs/GROUPKEY_DEPRECATION_PLAN.md`.

## Fase B - Estado
- Iniciada.
- Las pruebas de resumen ya empiezan a validar `groupKey` como asercion principal.
- `code` se mantiene solo en verificaciones minimas de compatibilidad.

### Avance actual
- `TourControllerTest`, `CourtBookingControllerTest` y `MaintenanceControllerTest` ya validan `groupKey` como señal primaria en breakdowns y detalles.
- Las aserciones de `code` quedaron reducidas a compatibilidad minima en los puntos donde la API legacy todavia lo expone.

### Hito UX-07 - Auditoria de remanentes
- La auditoria de documentacion y contratos auxiliares ya distingue entre `code` tecnico valido y `code` mal documentado como clave principal.
- `TOURS_BACKEND_IMPLEMENTATION.md`, `MANTENCION_BACKEND_PLAN.md` y ejemplos del changelog ya fueron alineados para describir `groupKey` como clave primaria de drill-down.
- Inventario de remanentes documentado en `docs/UX_GROUPKEY_AUDIT.md`.

## Verificaciones ejecutadas hasta ahora
- Backend maintenance:
  - `./mvnw -q -Dtest=MaintenanceControllerTest test`
- Frontend maintenance:
  - `flutter test test/features/maintenance/infrastructure/http_maintenance_app_service_test.dart`
  - `flutter analyze lib/features/maintenance/presentation/maintenance_page.dart`

## Verificaciones esperadas para el hito actual
- Backend tours:
  - `./mvnw -q -Dtest=TourControllerTest test`
- Frontend tours:
  - `flutter test test/features/tours/infrastructure/http_tours_app_service_test.dart`
  - `flutter analyze lib/features/tours/presentation/tours_travel_page.dart`
- Backend courts:
  - `./mvnw -q -Dtest=CourtBookingControllerTest test`
- Frontend tennis:
  - `flutter test test/features/tennis/presentation/tennis_rental_page_test.dart`
  - `flutter analyze lib/features/tennis/presentation/tennis_rental_page.dart`

## Nota de continuidad
Si se retoma este trabajo en otra sesion, el siguiente paso recomendado es reducir fixtures y mocks frontend que aun cargan `code` por defecto aunque el consumo principal ya dependa de `groupKey`.
