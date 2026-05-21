# Auditoria UX-07 - Remanentes de `code` fuera del flujo principal

Ultima actualizacion: 2026-05-19

## Objetivo
Separar los usos de `code` que siguen siendo validos de los que todavia generan ruido documental o contractual.

## Remanentes aceptados
- Catalogos tecnicos donde `code` es parte real del dominio:
  - materiales de `courts`
  - referencias internas de `maintenance locations`
- Payloads de resumen donde `code` sigue presente solo por compatibilidad temporal.
- Fixtures y mocks que todavia incluyen `code` para probar backward compatibility.

## Remanentes corregidos en este hito
- Documentacion de `tours` actualizada para describir `groupKey` como clave principal de drill-down.
- Plan backend de `maintenance` actualizado para describir `groupKey` en `summary/details`.
- Changelog tecnico ajustado para que los ejemplos HTTP de detalle usen `groupKey`.

## Remanentes a revisar despues
- Documentacion funcional o planes historicos que aun nombren `code` como parametro principal en reportes.
- Fixtures frontend que hoy incluyen `code` aunque el consumo real ya dependa de `groupKey`.
- Eventuales clientes externos no versionados que sigan llamando `summary/details` con `code`.

## Criterio actual
- En reportes nuevos:
  - `groupKey` es la clave primaria.
  - `code` no debe aparecer como instruccion principal para integradores.
- En integraciones legacy:
  - `code` puede mantenerse mientras exista compatibilidad explicita documentada.
