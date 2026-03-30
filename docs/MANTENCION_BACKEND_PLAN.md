# PLAN BACKEND MANTENCION

Fecha: 2026-03-30
Modulo propuesto: `maintenance`
Estado: `Implementado`

## Estado final implementado
- Se implemento el dominio backend completo bajo `/api/v1/maintenance`.
- Se resolvieron:
  - catalogo editable de ubicaciones
  - catalogo editable de responsables
  - ordenes operativas con agenda y seguimiento
  - historial por ubicacion
  - resumen y detalle de resumen
  - fotos y adjuntos
- Politica confirmada para conflictos de agenda por ubicacion:
  - no bloquear automaticamente
  - informar conflictos existentes
  - dejar la decision al operador
- Validacion ejecutada:
  - `./mvnw -q -Dtest=MaintenanceControllerTest test`
  - `./mvnw -q -DskipTests compile`

## Objetivo
- Crear un dominio backend propio para `mantencion`.
- Permitir registrar, agendar, asignar y seguir ordenes de mantenimiento para:
  - cuartos
  - areas comunes
- Mantener el mismo criterio tecnico que ya usa el sistema en `massages`, `tours` y `courts`:
  - dominio propio
  - controllers y services dedicados
  - reportes separados del CRUD operativo
  - auditoria por usuario autenticado

## Base analizada del sistema actual
- `Massages` ya resuelve:
  - proveedores
  - bookings operativos
  - resumen por prestador
  - detalle del resumen
- `Tours` ya resuelve:
  - proveedores con catalogo
  - agendamiento
  - pago, cancelacion y resumen estandar
- `Courts` ya resuelve:
  - agenda mensual
  - configuraciones editables
  - resumen estandar con breakdowns
- Conclusion:
  - `mantencion` no debe colgarse de `reservations` ni reciclar entidades de otros modulos
  - conviene crear un dominio nuevo equivalente a los anteriores

## Alcance funcional confirmado
- Debe existir una seccion nueva de `mantencion`.
- Debe poder cargarse y editarse la identificacion de:
  - cuartos
  - areas comunes
- Debe poder verse la historia de mantenimiento por cuarto o area comun.
- Deben existir dos tipos de responsables:
  - mantenimiento interno
  - mantenimiento externo
- Para la fase inicial, el catalogo externo debe contemplar al menos:
  - servicio de mantenimiento de aires
  - servicio de mantenimiento de internet
- Cada responsable debe dejar visible que servicio presta.
- Debe existir agendamiento con calendario, equivalente al patron usado en `massages`, `tours` y `courts`.
- Deben existir resumenes en el estandar actual del sistema.

## Decision de arquitectura
- Crear un dominio nuevo bajo `/api/v1/maintenance`.
- Reusar el patron existente:
  - catalogos editables
  - ordenes operativas
  - resumen de periodo
  - historial por entidad
- No modelar esta primera fase como un simple checklist o ticket aislado.
- Modelar `orden de mantenimiento` como entidad principal porque el requerimiento mezcla:
  - ocurrencia
  - responsable
  - agenda
  - seguimiento
  - cierre

## Modelo funcional propuesto

### Catalogo de ubicaciones
- Entidad: `MaintenanceLocation`
- Tipos:
  - `ROOM`
  - `COMMON_AREA`
- Campos minimos:
  - `id`
  - `locationType`
  - `code`
  - `label`
  - `floor` opcional
  - `description` opcional
  - `active`
  - `createdAt`
  - `updatedAt`
  - `createdBy`
  - `updatedBy`

### Catalogo de responsables de mantenimiento
- Entidad: `MaintenanceProvider`
- Tipo de responsable:
  - `INTERNAL`
  - `EXTERNAL`
- Campos minimos:
  - `id`
  - `providerType`
  - `name`
  - `serviceLabel`
  - `scopeDescription`
  - `contact` opcional
  - `active`
  - `createdAt`
  - `updatedAt`
  - `createdBy`
  - `updatedBy`
- Seeds iniciales sugeridos:
  - `Mantenimiento interno`
  - `Servicio de mantenimiento de aires`
  - `Servicio de mantenimiento de internet`

### Orden de mantenimiento
- Entidad: `MaintenanceOrder`
- Estados sugeridos:
  - `OPEN`
  - `SCHEDULED`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `CANCELLED`
- Prioridad sugerida:
  - `LOW`
  - `MEDIUM`
  - `HIGH`
  - `URGENT`
- Campos minimos:
  - `id`
  - `locationId`
  - `locationTypeSnapshot`
  - `locationCodeSnapshot`
  - `locationLabelSnapshot`
  - `providerId`
  - `providerTypeSnapshot`
  - `providerNameSnapshot`
  - `serviceLabelSnapshot`
  - `title`
  - `description`
  - `priority`
  - `status`
  - `scheduledStartAt`
  - `scheduledEndAt`
  - `reportedAt`
  - `startedAt`
  - `completedAt`
  - `cancellationNotes`
  - `resolutionNotes`
  - `createdAt`
  - `updatedAt`
  - `cancelledAt`
  - `createdBy`
  - `updatedBy`
  - `cancelledBy`

### Historial de la orden
- En la fase 1 no hace falta una tabla de eventos separada.
- La historia por cuarto o area comun puede reconstruirse desde `MaintenanceOrder` filtrando por `locationId`.
- Si luego se requiere trazabilidad mas fina, se puede agregar:
  - `MaintenanceOrderEvent`

### Fotos y adjuntos
- La fase inicial debe soportar fotos y adjuntos.
- Recomendacion de modelo:
  - entidad `MaintenanceOrderAttachment`
- Campos minimos:
  - `id`
  - `maintenanceOrderId`
  - `fileName`
  - `contentType`
  - `storagePath` o `storageKey`
  - `fileSize`
  - `uploadedAt`
  - `uploadedBy`
- Tipos iniciales sugeridos:
  - `PHOTO`
  - `ATTACHMENT`
- Uso esperado:
  - evidencia de la ocurrencia
  - evidencia del trabajo realizado
  - respaldo tecnico o factura del proveedor externo

## Reglas de negocio sugeridas
- Una orden debe quedar asociada obligatoriamente a una ubicacion.
- Una orden debe quedar asociada obligatoriamente a un responsable interno o externo.
- Los snapshots de ubicacion y responsable deben persistirse en la orden.
- Motivo:
  - si el catalogo cambia despues, el historico no debe reescribirse solo
- Una orden `COMPLETED` o `CANCELLED` no puede volver a editarse como si estuviera abierta.
- Para marcar `COMPLETED`, debe exigirse:
  - `completedAt`
  - `resolutionNotes`
- Para marcar `CANCELLED`, debe exigirse:
  - `cancellationNotes`
- `scheduledEndAt` debe ser mayor que `scheduledStartAt` cuando ambos existan.
- Debe permitirse crear ordenes no calendarizadas aun:
  - `OPEN` sin agenda completa
- No se recomienda bloquear solapamientos en fase 1.
- Motivo:
  - dos trabajos distintos pueden existir el mismo dia
  - la restriccion por responsable o ubicacion necesita decision operativa adicional

## Persistencia sugerida

### Tabla `maintenance_locations`
- `id`
- `location_type`
- `code`
- `label`
- `floor`
- `description`
- `active`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`

### Tabla `maintenance_providers`
- `id`
- `provider_type`
- `name`
- `service_label`
- `scope_description`
- `contact`
- `active`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`

### Tabla `maintenance_orders`
- `id`
- `location_id`
- `location_type_snapshot`
- `location_code_snapshot`
- `location_label_snapshot`
- `provider_id`
- `provider_type_snapshot`
- `provider_name_snapshot`
- `service_label_snapshot`
- `title`
- `description`
- `priority`
- `status`
- `reported_at`
- `scheduled_start_at`
- `scheduled_end_at`
- `started_at`
- `completed_at`
- `resolution_notes`
- `cancellation_notes`
- `created_at`
- `updated_at`
- `cancelled_at`
- `created_by`
- `updated_by`
- `cancelled_by`

## Endpoints propuestos

### Ubicaciones
- `GET /api/v1/maintenance/locations`
- `POST /api/v1/maintenance/locations`
- `PUT /api/v1/maintenance/locations/{locationId}`
- `GET /api/v1/maintenance/locations/{locationId}/history`

### Responsables
- `GET /api/v1/maintenance/providers`
- `POST /api/v1/maintenance/providers`
- `PUT /api/v1/maintenance/providers/{providerId}`

### Ordenes
- `GET /api/v1/maintenance/orders`
- Filtros sugeridos:
  - `dateFrom`
  - `dateTo`
  - `locationId`
  - `providerId`
  - `providerType`
  - `status`
  - `priority`
- `POST /api/v1/maintenance/orders`
- `PUT /api/v1/maintenance/orders/{orderId}`
- `PATCH /api/v1/maintenance/orders/{orderId}/start`
- `PATCH /api/v1/maintenance/orders/{orderId}/complete`
- `PATCH /api/v1/maintenance/orders/{orderId}/cancel`
- `POST /api/v1/maintenance/orders/{orderId}/attachments`
- `GET /api/v1/maintenance/orders/{orderId}/attachments`
- `DELETE /api/v1/maintenance/orders/{orderId}/attachments/{attachmentId}`

### Reportes
- `GET /api/v1/maintenance/reports/summary`
- `GET /api/v1/maintenance/reports/summary/details`

## Contrato de resumen estandar sugerido

### `GET /api/v1/maintenance/reports/summary`
- Parametros:
  - `dateFrom`
  - `dateTo`
- Respuesta tipo `MaintenanceSummaryReportDto` con:
  - `openCount`
  - `scheduledCount`
  - `inProgressCount`
  - `completedCount`
  - `cancelledCount`
  - `internalCount`
  - `externalCount`
  - `roomsCount`
  - `commonAreasCount`
  - `urgentCount`
  - `averageResolutionHours`
  - `providerBreakdown`
  - `providerTypeBreakdown`
  - `locationTypeBreakdown`
  - `statusBreakdown`

### `GET /api/v1/maintenance/reports/summary/details`
- Parametros:
  - `groupBy`
  - `code`
  - `dateFrom`
  - `dateTo`
- Grupos sugeridos:
  - `PROVIDER`
  - `PROVIDER_TYPE`
  - `LOCATION_TYPE`
  - `STATUS`
- Respuesta:
  - metadata del grupo
  - resumen del grupo
  - lista de ordenes que explican esa fila

## DTOs minimos sugeridos

### Catalogos
- `MaintenanceLocationDto`
- `CreateMaintenanceLocationDto`
- `UpdateMaintenanceLocationDto`
- `MaintenanceProviderDto`
- `CreateMaintenanceProviderDto`
- `UpdateMaintenanceProviderDto`

### Operacion
- `MaintenanceOrderDto`
- `CreateMaintenanceOrderDto`
- `UpdateMaintenanceOrderDto`
- `StartMaintenanceOrderDto`
- `CompleteMaintenanceOrderDto`
- `CancelMaintenanceOrderDto`

### Reportes
- `MaintenanceSummaryReportDto`
- `MaintenanceSummaryBreakdownDto`
- `MaintenanceSummaryDetailDto`
- `MaintenanceSummaryDetailItemDto`

## Fases recomendadas de implementacion
1. Crear migracion Flyway para `maintenance_locations`, `maintenance_providers` y `maintenance_orders`.
2. Crear soporte de adjuntos para ordenes:
   - metadata en base de datos
   - estrategia de almacenamiento local o externo
3. Sembrar providers iniciales:
   - mantenimiento interno
   - aires
   - internet
4. Crear enums, entidades y repositories.
5. Implementar servicios de catalogos.
6. Implementar servicio de ordenes con snapshots y validaciones.
7. Implementar carga y consulta de adjuntos.
8. Implementar reportes estandar y historial por ubicacion.
9. Exponer controllers y DTOs.
10. Cubrir con tests de controller y servicio.
11. Validar integracion real con `quedras-front`.

## Tests minimos
- Alta y edicion de ubicacion `ROOM`.
- Alta y edicion de ubicacion `COMMON_AREA`.
- Alta y edicion de provider interno.
- Alta y edicion de provider externo.
- Creacion de orden `OPEN`.
- Creacion de orden `SCHEDULED`.
- Inicio de orden `SCHEDULED -> IN_PROGRESS`.
- Cierre de orden `IN_PROGRESS -> COMPLETED`.
- Cancelacion con nota obligatoria.
- Carga de foto en una orden.
- Carga de adjunto tecnico o factura.
- Historial por ubicacion.
- Resumen por rango con breakdown por tipo de responsable.
- Resumen por rango con breakdown por tipo de ubicacion.
- Snapshot preservado si cambia el nombre del area o del provider.

## Riesgos y decisiones abiertas
- Confirmar el label visible en UI:
  - `Mantencao`
  - `Mantencion`
- Confirmar si la agenda debe permitir horas vacias para ordenes solo abiertas.
- Confirmar si una orden puede quedar sin proveedor asignado al crearla.
- Confirmar si debe existir SLA o fecha limite separada de la agenda.
- Confirmar si hay que impedir dos ordenes activas para la misma ubicacion y franja.

## Criterio de cierre
- Existe dominio backend propio de `maintenance`.
- El backend permite:
  - administrar cuartos y areas comunes
  - administrar responsables internos y externos
  - crear y seguir ordenes
  - consultar historia por ubicacion
  - consumir un resumen estandar por periodo
- El contrato queda listo para montar la UI con el mismo patron operativo del sistema.
