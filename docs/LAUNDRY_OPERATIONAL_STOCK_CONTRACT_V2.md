# Contrato V2 - Stock operativo de lavanderia

## 1. Estado y alcance

Este documento congela el contrato funcional y HTTP de la Fase 0 del plan
`LAUNDRY_OPERATIONAL_STOCK_PLAN.md`.

- Fase 0: contrato, ejemplos JSON, matriz de transiciones, errores e idempotencia.
- Fase 1: libro de stock, apertura inicial, ajustes auditados y tablero.
- Fases 2 a 5: documentos, FIFO, devoluciones, portal del proveedor y corte.

Reglas de convivencia:

- `/api/v1/laundry/**` se conserva sin cambios de contrato.
- Las operaciones nuevas usan exclusivamente `/api/v2/laundry/**`.
- V1 queda como lectura historica y conserva `RECEIPT`.
- No se modifica ninguna migracion existente. La primera migracion nueva es `V43`.

## 2. Convenciones comunes

### 2.1 Base y autenticacion

- Base: `/api/v2/laundry`.
- Autenticacion: `Authorization: Bearer <jwt>`.
- Roles: `OPERATOR`, `SUPERVISOR`; `PROVIDER` se incorpora en Fase 4.
- `SUPERVISOR` incluye los permisos de `OPERATOR`.
- `PROVIDER` nunca recibe permisos de operador.

### 2.2 Cabeceras

| Cabecera | Obligatoria | Uso |
| --- | --- | --- |
| `Authorization` | Si | JWT del usuario |
| `Content-Type` | Si en comandos | `application/json` |
| `Idempotency-Key` | Si en comandos | 1 a 120 caracteres `[A-Za-z0-9._:-]` |
| `Idempotent-Replay` | Respuesta | `true` cuando se devuelve una respuesta almacenada |

La clave de idempotencia se evalua por actor. Dos usuarios pueden usar la misma clave sin
colisionar.

### 2.3 Formato

- Fechas y horas: ISO-8601 con offset, por ejemplo `2026-11-03T09:15:00-03:00`.
- Cantidades: decimal con hasta 3 decimales. Se normalizan a escala 3.
- Precios e importes: decimal con 2 decimales, redondeo `HALF_UP`.
- Enums: `UPPER_SNAKE_CASE`.
- Listas paginadas: `page` (base 0, default 0), `size` (default 20, maximo 100),
  `hasNext` e `items`.
- Orden estable: el definido por cada recurso. Ante empate se usa `id` ascendente.

### 2.4 Errores

Se conserva la estructura comun `ApiError`:

```json
{
  "timestamp": "2026-11-03T09:15:00-03:00",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient HOTEL_AVAILABLE balance for item 4: available 2.000, requested 3.000",
  "path": "/api/v2/laundry/stock-adjustments"
}
```

Tabla de estados:

| Estado | Uso |
| --- | --- |
| `400` | JSON invalido, enum invalido, validacion de campos, cabecera idempotente invalida |
| `401` | Sin token o token invalido |
| `403` | Rol insuficiente u ownership de proveedor |
| `404` | Recurso inexistente o no visible para el actor |
| `409` | Version desactualizada, saldo insuficiente, apertura ya aprobada, transicion invalida, clave idempotente reutilizada o en curso |
| `422` | Peticion valida pero no reconciliable automaticamente; requiere decision humana |
| `500` | Error inesperado |

`422` se reserva para Fases 2 y 3. Ejemplos previstos:

- una devolucion cuyos resultados no pueden asignarse a obligaciones abiertas despues de FIFO;
- resolver una discrepancia que ya no esta abierta;
- confirmar un documento con lineas que no corresponden al documento original.

### 2.5 Idempotencia

- Obligatoria en `POST`, `PUT` y `PATCH`.
- Primera ejecucion: se persiste la clave, se ejecuta el comando y se guarda la respuesta.
- Reintento con misma clave y mismo payload: se devuelve el estado y cuerpo originales con
  `Idempotent-Replay: true`.
- Misma clave con payload distinto: `409`.
- Clave en ejecucion: `409`.
- Solo se almacenan respuestas `2xx`. Un `4xx` o `5xx` libera la clave para reintento.
- Vigencia: 24 horas. Una clave vencida se elimina de forma perezosa y se trata como nueva.
- La clave no reemplaza el control de version. Una clave nueva con version vieja sigue
  respondiendo `409`.

Huella de payload: SHA-256 de `metodo + "\n" + ruta + "\n" + cuerpo JSON normalizado`.

## 3. Enums congelados

### 3.1 Ubicaciones de stock

| Valor | Descripcion | `providerId` |
| --- | --- | --- |
| `HOTEL_AVAILABLE` | Prenda disponible en hotel | Debe ser `null` |
| `HOTEL_REWASH_QUEUE` | Prenda sucia en cola de relavado del hotel | Obligatorio |
| `PROVIDER_PROCESSING` | Prenda en poder del proveedor | Obligatorio |
| `PROVIDER_REWASH` | Prenda retenida en proveedor para relavado | Obligatorio |
| `MISSING` | Faltante atribuido al proveedor | Obligatorio |
| `DAMAGED` | Prenda danada atribuida al proveedor | Obligatorio |

`HOTEL_REWASH_QUEUE`, `MISSING` y `DAMAGED` conservan el proveedor de origen para preservar
trazabilidad y FIFO. La ubicacion fisica se registra en el movimiento y en el documento.

### 3.2 Tipos de movimiento

`OPENING`, `DISPATCH`, `RETURN_CLEAN`, `RETURN_REWASH_QUEUE`, `RETENTION`, `MISSING`,
`DAMAGED`, `ADJUSTMENT_INCREASE`, `ADJUSTMENT_DECREASE`, `TRANSFER`, `REGULARIZATION`.

### 3.3 Tipos de documento

`DISPATCH`, `REWASH_DISPATCH`, `RETURN`, `OPENING_STOCK`, `STOCK_ADJUSTMENT`.

### 3.4 Estados de documento de envio

`DRAFT`, `SUBMITTED`, `CONFIRMED`, `OBSERVED`, `RESOLVED`, `CANCELLED`.

### 3.5 Estados de manifiesto de devolucion

`DRAFT`, `SUBMITTED`, `CONFIRMED`, `CONFIRMED_BY_HOTEL`, `OBSERVED`, `RESOLVED`, `CANCELLED`.

### 3.6 Tipos de resultado de devolucion

`CLEAN`, `DIRTY`, `RETAINED`, `MISSING`, `DAMAGED`.

### 3.7 Estados de evento cobrable

`PENDING_EXPORT`, `EXPORTED`.

### 3.8 Operaciones de ajuste

`INCREASE`, `DECREASE`, `TRANSFER`.

## 4. Fase 1 - endpoints implementables

### 4.0 Catalogo de proveedores

`GET /api/v2/laundry/providers`

- Devuelve proveedores activos e inactivos, ordenados por nombre, para permitir administracion e historicos.

`POST /api/v2/laundry/providers`

`PUT /api/v2/laundry/providers/{id}`

Payload comun:

```json
{
  "name": "Lavanderia Central",
  "contact": "+55 11 99999-0000",
  "active": true
}
```

- `name`: obligatorio, maximo 120 caracteres y unico sin distinguir mayusculas.
- `contact`: opcional, maximo 160 caracteres.
- `active`: controla la disponibilidad para nuevas operaciones.
- Un proveedor inactivo permanece disponible para administracion e historicos, pero no puede recibir nuevos envios.
- Respuestas: `201` al crear, `200` al actualizar, `404` si el identificador no existe y `409` si el nombre ya esta en uso.

### 4.1 Tablero

`GET /api/v2/laundry/dashboard?providerId=`

- Sin `providerId`: devuelve saldos globales del hotel y el desglose por proveedor.
- Con `providerId`: devuelve solo saldos de ese proveedor; las ubicaciones de hotel quedan en
  `0` y `providers` contiene solo ese proveedor.
- `providerId` inexistente: `404`.
- `oldest*At` es la fecha efectiva del lote mas antiguo con cantidad remanente.
- `total` es la suma de todas las ubicaciones del alcance consultado.

Respuesta `200`:

```json
{
  "generatedAt": "2026-11-03T09:15:00-03:00",
  "providerId": null,
  "totals": {
    "hotelAvailable": 12.000,
    "hotelRewashQueue": 1.000,
    "providerProcessing": 10.000,
    "providerRewash": 3.000,
    "missing": 0.000,
    "damaged": 1.000,
    "total": 27.000
  },
  "items": [
    {
      "itemId": 4,
      "itemName": "Toalha de banho",
      "unitType": "PIECE",
      "hotelAvailable": 12.000,
      "hotelRewashQueue": 1.000,
      "providerProcessing": 10.000,
      "providerRewash": 3.000,
      "missing": 0.000,
      "damaged": 1.000,
      "total": 27.000,
      "oldestHotelRewashQueueAt": "2026-11-01T10:00:00-03:00",
      "oldestProviderProcessingAt": "2026-10-28T08:00:00-03:00",
      "oldestProviderRewashAt": "2026-10-30T08:00:00-03:00",
      "oldestMissingAt": null,
      "oldestDamagedAt": "2026-11-02T16:00:00-03:00"
    }
  ],
  "providers": [
    {
      "providerId": 1,
      "providerName": "Lavanderia Central",
      "providerProcessing": 10.000,
      "providerRewash": 3.000,
      "missing": 0.000,
      "damaged": 1.000,
      "total": 14.000
    }
  ]
}
```

Invariante: `totals.total = hotelAvailable + hotelRewashQueue + providerProcessing +
providerRewash + missing + damaged`.

### 4.2 Detalle de saldos

`GET /api/v2/laundry/stock?itemId=&bucket=&providerId=&page=&size=`

- `bucket` opcional.
- `providerId` opcional; si se informa, solo devuelve filas de ese proveedor.
- Si `bucket` es de hotel y se informa `providerId`: `400`.
- `ageDays` es la diferencia entera en dias entre `oldestEffectiveAt` y la hora del servidor.
- `version` es la version optimista del saldo.

Respuesta `200`:

```json
{
  "page": 0,
  "size": 20,
  "hasNext": false,
  "items": [
    {
      "balanceId": 12,
      "itemId": 4,
      "itemName": "Toalha de banho",
      "unitType": "PIECE",
      "bucket": "PROVIDER_PROCESSING",
      "providerId": 1,
      "providerName": "Lavanderia Central",
      "quantity": 10.000,
      "oldestEffectiveAt": "2026-10-28T08:00:00-03:00",
      "ageDays": 6,
      "version": 3
    }
  ]
}
```

### 4.3 Apertura inicial

`POST /api/v2/laundry/opening-stock`

Permitido solo a `SUPERVISOR`. Body:

```json
{
  "version": 0,
  "effectiveAt": "2026-11-03T08:00:00-03:00",
  "notes": "Carga inicial aprobada por gobernanta",
  "lines": [
    { "itemId": 4, "quantity": 20.000 },
    { "itemId": 5, "quantity": 0.000 }
  ]
}
```

Reglas:

- `version` debe ser `0`.
- `effectiveAt` obligatorio; no puede superar la hora del servidor en mas de 5 minutos.
- `lines` no vacio e items unicos.
- Cada item debe existir y estar activo.
- `quantity >= 0`; al menos una linea debe ser mayor que cero.
- Los items activos omitidos quedan en cero y se corrigen con ajustes auditados.
- Solo existe una apertura aprobada. Un segundo intento responde `409`.
- Cada linea positiva genera un movimiento `OPENING` hacia `HOTEL_AVAILABLE`.
- La operacion completa es atomica.

Respuesta `201`:

```json
{
  "id": 1,
  "reference": "OPEN-20261103-AB12CD",
  "status": "APPROVED",
  "approvedBy": "supervisor.demo",
  "approvedAt": "2026-11-03T08:00:01-03:00",
  "effectiveAt": "2026-11-03T08:00:00-03:00",
  "notes": "Carga inicial aprobada por gobernanta",
  "correlationId": "8a5b2f1e-6d0e-4a3b-a0f6-2b6f2f4f9d1a",
  "lines": [
    {
      "itemId": 4,
      "itemName": "Toalha de banho",
      "unitType": "PIECE",
      "quantity": 20.000
    }
  ]
}
```

### 4.4 Ajuste de stock

`POST /api/v2/laundry/stock-adjustments`

Permitido solo a `SUPERVISOR`. Body:

```json
{
  "version": 3,
  "itemId": 4,
  "operation": "TRANSFER",
  "fromBucket": "MISSING",
  "toBucket": "HOTEL_AVAILABLE",
  "providerId": 1,
  "quantity": 1.000,
  "reason": "Prenda encontrada no enxoval",
  "effectiveAt": "2026-11-03T09:30:00-03:00"
}
```

Reglas por operacion:

| Operacion | `fromBucket` | `toBucket` | `version` |
| --- | --- | --- | --- |
| `INCREASE` | `null` | Obligatorio | `0` |
| `DECREASE` | Obligatorio | `null` | Version actual del saldo origen |
| `TRANSFER` | Obligatorio | Obligatorio y distinto | Version actual del saldo origen |

- `reason` obligatorio, entre 5 y 500 caracteres.
- `quantity > 0`.
- `providerId` obligatorio para toda ubicacion distinta de `HOTEL_AVAILABLE`.
- `HOTEL_AVAILABLE` exige `providerId = null`.
- Disminucion o transferencia sin saldo suficiente: `409`, sin cambios parciales.
- Version distinta a la actual: `409`.
- Cada ajuste conserva actor, rol, motivo, fecha efectiva y `correlationId`.
- Solo modifica el total en `INCREASE` y `DECREASE`; `TRANSFER` conserva el total.

Respuesta `201`:

```json
{
  "id": 7,
  "itemId": 4,
  "itemName": "Toalha de banho",
  "operation": "TRANSFER",
  "fromBucket": "MISSING",
  "toBucket": "HOTEL_AVAILABLE",
  "providerId": 1,
  "quantity": 1.000,
  "reason": "Prenda encontrada no enxoval",
  "effectiveAt": "2026-11-03T09:30:00-03:00",
  "actorUsername": "supervisor.demo",
  "actorRole": "SUPERVISOR",
  "correlationId": "4f0c1d7e-2a88-4b4d-9e47-0f4f1f2a6c31",
  "createdAt": "2026-11-03T09:30:02-03:00"
}
```

## 5. Fases 2 a 4 - contrato resumido

### 5.1 Documentos

- `GET /api/v2/laundry/documents?type=&status=&providerId=&from=&to=&page=&size=`
- `GET /api/v2/laundry/documents/{id}`
- Documento de envio: `POST /api/v2/laundry/dispatches`
- Envio desde cola de relavado: `POST /api/v2/laundry/rewash-dispatches`
- Enviar documento: `POST /api/v2/laundry/documents/{id}/submit`
- Confirmar documento: `POST /api/v2/laundry/documents/{id}/confirm`
- Observar documento: `POST /api/v2/laundry/documents/{id}/observations`
- Resolver discrepancia: `POST /api/v2/laundry/discrepancies/{id}/resolve`
- Manifiesto de devolucion: `POST /api/v2/laundry/returns`
- Enviar devolucion: `POST /api/v2/laundry/returns/{id}/submit`
- Confirmar/corregir devolucion: `POST /api/v2/laundry/returns/{id}/confirm`
- Eventos cobrables: `GET /api/v2/laundry/billable-events?providerId=&status=&from=&to=&page=&size=`
- Exportar: `POST /api/v2/laundry/billable-events/export`
- Reportes: `GET /api/v2/laundry/reports/missing`, `damaged`, `aging`, `billable`

### 5.2 Envio agregado

```json
{
  "version": 0,
  "providerId": 1,
  "effectiveAt": "2026-11-03T10:00:00-03:00",
  "notes": "Envio semanal",
  "lines": [
    { "itemId": 4, "quantity": 10.000 }
  ]
}
```

- Solo descuenta `HOTEL_AVAILABLE`.
- Crea una obligacion por linea con precio historico.
- Si no hay saldo: `409`.
- El documento puede guardarse como `DRAFT`; al enviarse pasa a `SUBMITTED`.
- Un documento `SUBMITTED` ya no se edita.

### 5.3 Relavado manual

```json
{
  "version": 0,
  "providerId": 1,
  "effectiveAt": "2026-11-03T10:00:00-03:00",
  "notes": "Reenvio de prenda retenida",
  "lines": [
    { "itemId": 4, "quantity": 1.000 }
  ]
}
```

- Solo descuenta `HOTEL_REWASH_QUEUE`.
- Precio `0.00` y `billable = false`.
- Conserva la obligacion y el precio historico original.
- Si no hay saldo en cola: `409`.

### 5.4 Devolucion

```json
{
  "version": 0,
  "providerId": 1,
  "effectiveAt": "2026-11-04T09:00:00-03:00",
  "notes": "Devolucion semanal",
  "lines": [
    {
      "itemId": 4,
      "declaredClean": 7.000,
      "declaredDirty": 3.000,
      "declaredRetained": 0.000,
      "declaredMissing": 0.000,
      "declaredDamaged": 0.000
    }
  ]
}
```

Resultado final por linea:

```json
{
  "itemId": 4,
  "acceptedClean": 7.000,
  "returnedDirty": 3.000,
  "retained": 0.000,
  "missing": 0.000,
  "damaged": 0.000
}
```

- El hotel confirma con cantidades finales.
- Si la devolucion la crea el hotel: `CONFIRMED_BY_HOTEL`.
- Si la crea el proveedor: `DRAFT -> SUBMITTED -> CONFIRMED` o `OBSERVED`.
- Una cantidad sucia, retenida, faltante o danada nunca genera cobro.
- Una limpieza aceptada genera como maximo un evento cobrable.

## 6. Matriz de transiciones

### 6.1 Documento de envio

| Desde | Accion | Hacia | Actor |
| --- | --- | --- | --- |
| `DRAFT` | Guardar | `DRAFT` | Hotel |
| `DRAFT` | Enviar | `SUBMITTED` | Hotel |
| `DRAFT` | Cancelar | `CANCELLED` | Hotel |
| `SUBMITTED` | Confirmar | `CONFIRMED` | Proveedor |
| `SUBMITTED` | Observar | `OBSERVED` | Proveedor |
| `OBSERVED` | Resolver | `RESOLVED` | Hotel |
| `CONFIRMED` | - | Final | - |
| `CANCELLED` | - | Final | - |

Un documento ya despachado no se cancela directamente. La correccion se realiza con un
movimiento compensatorio auditado.

### 6.2 Manifiesto de devolucion creado por proveedor

| Desde | Accion | Hacia | Actor |
| --- | --- | --- | --- |
| `DRAFT` | Guardar | `DRAFT` | Proveedor |
| `DRAFT` | Enviar | `SUBMITTED` | Proveedor |
| `DRAFT` | Cancelar | `CANCELLED` | Proveedor |
| `SUBMITTED` | Confirmar sin correccion | `CONFIRMED` | Hotel |
| `SUBMITTED` | Confirmar con correccion | `CONFIRMED` | Hotel |
| `SUBMITTED` | Observar | `OBSERVED` | Hotel |
| `OBSERVED` | Resolver | `RESOLVED` | Hotel |
| `CONFIRMED` | - | Final | - |
| `RESOLVED` | - | Final | - |

### 6.3 Manifiesto de devolucion creado por hotel

| Desde | Accion | Hacia | Actor |
| --- | --- | --- | --- |
| `DRAFT` | Confirmar | `CONFIRMED_BY_HOTEL` | Hotel |
| `CONFIRMED_BY_HOTEL` | - | Final | - |

Los estados finales no admiten nuevas mutaciones. Una observacion no altera cantidades ni
stock.

## 7. Reglas de stock y FIFO

- Una ubicacion de hotel (`HOTEL_AVAILABLE`, sin proveedor) es un pool intercambiable por item.
- Las demas ubicaciones siempre tienen proveedor.
- Cada movimiento registra origen, destino, cantidad, fecha efectiva, actor, rol y motivo.
- Los movimientos son inmutables; no se editan ni eliminan desde la aplicacion.
- Los saldos se derivan de movimientos y lotes:
  - `saldo(item, bucket, provider) = suma(entradas) - suma(salidas)`;
  - `saldo(item, bucket, provider) = suma(lotes remanentes)`.
- Los lotes conservan la fecha efectiva para calcular antiguedad.
- Un `TRANSFER` conserva el total. `INCREASE` y `DECREASE` lo modifican.
- FIFO se aplica por `providerId + itemId`, ordenando por fecha efectiva, creacion e id.
- Una devolucion no puede consumir mas saldo abierto que el disponible: `409`.
- La asignacion FIFO se registra por linea de devolucion y obligacion.
- Una asignacion limpia genera un evento cobrable unico.
- Un evento cobrable no se duplica ante reintentos.

Precio:

- Se toma `laundry_items.current_unit_price` al crear el envio normal.
- Se congela por linea.
- El relavado usa `0.00`.
- Un evento cobrable usa el precio congelado de la obligacion.

Redondeo:

- Cantidades: escala 3, `HALF_UP`.
- Importe por linea: `round(cantidad * precio, 2)`.
- La suma de eventos cobrables es la suma de importes de linea.

## 8. Antiguedad

- Se calcula con el lote remanente mas antiguo de cada `item + bucket + provider`.
- `oldestEffectiveAt` es la fecha efectiva de ese lote.
- `ageDays = max(0, dias completos entre oldestEffectiveAt y now)`.
- Sin saldo: `null`.
- La antiguedad se conserva al transferir un lote entre ubicaciones.
- El tablero muestra la antiguedad solo para ubicaciones pendientes; la disponibilidad se
  muestra como saldo, sin semaforo de olvido.

## 9. Compatibilidad V1

- `/api/v1/laundry/**` no cambia.
- `GET /api/v1/laundry/shipments` sigue devolviendo `RECEIPT` historicos.
- Las acciones V1 siguen operativas durante Fases 1 a 4.
- V2 no convierte automaticamente recepciones ni relavados V1 en obligaciones.
- La conciliacion V1 es humana y queda documentada en Fase 5.
- Los campos y enums existentes no se eliminan ni renombran.
- Cualquier cambio accidental de forma se detecta con la prueba de compatibilidad V1.

## 10. Permisos

| Recurso | `OPERATOR` | `SUPERVISOR` | `PROVIDER` |
| --- | --- | --- | --- |
| Tablero y saldos | Si | Si | No |
| Apertura inicial | No | Si | No |
| Ajustes de stock | No | Si | No |
| Documentos del hotel | Si | Si | Solo propios (Fase 4) |
| Confirmar envio | No | No | Si, propio (Fase 4) |
| Crear devolucion | Si | Si | Si, propia (Fase 4) |
| Confirmar devolucion | Si | Si | No |
| Observar documento | No | No | Si, propio (Fase 4) |
| Resolver discrepancia | Si | Si | No |
| Eventos cobrables | No | Si | No |

La autorizacion efectiva vive en backend. Flutter solo oculta rutas y acciones.

## 11. Decisiones de Fase 0

1. Se usan tablas nuevas V2. No se reutiliza `laundry_shipments` para el flujo nuevo.
2. `laundry_documents` sera la cabecera comun de envios, relavados y devoluciones en Fase 2.
3. La apertura inicial se ejecuta una sola vez y requiere supervisor.
4. Los ajustes de stock requieren supervisor, motivo y version del saldo origen.
5. El tablero y los saldos usan paginacion y consultas agregadas.
6. `Idempotency-Key` es obligatoria en todo comando.
7. Los errores conservan `ApiError` sin campos adicionales.
8. `422` queda reservado para conflictos no reconciliables de Fases 2 y 3.
9. V1 permanece intacto y consultable.
10. Las pruebas de contrato V1 se ejecutan antes de habilitar V2.

## 12. Validacion de Fase 0

- `mvnw.cmd test` debe pasar con la prueba de compatibilidad V1.
- `flutter analyze` debe seguir sin issues.
- `flutter test` debe seguir pasando.
- Los ejemplos JSON deben ser parseables.
- La matriz de transiciones debe cubrir todos los estados finales.
- Toda migracion nueva debe aplicar sobre esquema vacio y sobre esquema V42.
