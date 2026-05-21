# Plan de compatibilidad y deprecacion de `code` en resúmenes

Ultima actualizacion: 2026-05-19

## Objetivo
Definir una salida ordenada para que los contratos de resumen usen `groupKey` como clave semantica principal y `code` quede solo como compatibilidad temporal donde aplique.

## Alcance
Este plan aplica solo a contratos de resumen agregados y drill-down asociados.

Incluye:
- `maintenance/reports/summary`
- `maintenance/reports/summary/details`
- `tours/reports/summary`
- `tours/reports/summary/details`
- `courts/bookings/summary`

No incluye:
- identificadores operativos reales como materiales de quadra (`BALL`, `RACKET`),
- referencias internas de locales de mantenimiento,
- campos de integracion o exportacion donde `code` siga siendo dato funcional.

## Estado actual
### Maintenance
- Breakdown y detail ya publican `groupKey`.
- `summary/details` acepta `groupKey` y mantiene `code` como fallback legacy.

### Tours
- Breakdown y detail ya publican `groupKey`.
- `summary/details` acepta `groupKey` y mantiene `code` como fallback legacy.

### Courts
- El breakdown del summary ya publica `groupKey`.
- No existe endpoint de detail remoto por `code`; el frontend resuelve detalle localmente.

## Politica de compatibilidad
### Regla 1
Todo consumidor nuevo debe leer y enviar `groupKey`.

### Regla 2
`code` puede seguir presente en respuesta mientras:
- haya pruebas legacy que lo verifiquen,
- haya consumidores locales o externos no migrados,
- o el valor siga siendo util para soporte e inspeccion.

### Regla 3
No se deben crear nuevas dependencias funcionales sobre `code` en resumenes.

## Plan por fases
### Fase A - Actual
- Backend responde `groupKey` y `code` en breakdowns/details.
- Frontend usa `groupKey`.
- `code` sigue como compatibilidad y metadato tecnico.

### Fase B - Endurecimiento interno
- Mover pruebas nuevas a `groupKey` como asercion principal.
- Mantener una verificacion minima de `code` solo para compatibilidad.
- Evitar nuevos mocks/fixtures que dependan solo de `code`.

### Fase C - Deprecacion declarada
- Documentar en changelog que `code` queda oficialmente deprecated en resumenes.
- Si se publica especificacion de API, marcar `code` como deprecated.
- Mantener el fallback de query param `code` solo mientras existan clientes legacy identificados.

### Fase D - Retiro controlado
- Quitar `code` de request params legacy en endpoints de detail.
- Evaluar si `code` sigue siendo necesario en payloads de respuesta.
- Hacerlo solo cuando todos los consumidores activos esten migrados.

## Decision operativa actual
No se retira `code` todavia.

Motivos:
- evita ruptura innecesaria,
- conserva trazabilidad tecnica,
- y hoy no aporta costo alto mantenerlo en paralelo a `groupKey`.

La prioridad correcta en este momento es:
- `groupKey` obligatorio para nuevos desarrollos,
- `code` tolerado como compatibilidad temporal.

## Impacto esperado si en el futuro se retira `code`
- tests backend que hoy lo asertan,
- tests frontend con fixtures legacy,
- herramientas de soporte que inspeccionen payloads raw,
- cualquier integracion externa no inventariada.

## Criterio para habilitar retiro
Se puede abrir la fase de retiro solo si:
1. no quedan consumidores internos usando `code` para resumenes;
2. la suite principal valida `groupKey` como clave primaria;
3. el cambio queda comunicado en documentacion y release notes.
