# Plan de control de lavanderia

## Objetivo

Reemplazar el control manual por una trazabilidad de las prendas del hotel enviadas a una
lavanderia externa. El sistema diferencia envios normales, recibidos y relavados. Cada tipo
se registra como una transaccion independiente. Un
relavado se registra como una transaccion independiente y nunca genera un nuevo cobro.

La planilla `06 - LAVANDERIATABELA CONTROLE 2025.xlsx` se utilizo solo como referencia. Su
contenido controla principalmente lavanderia de huespedes (apartamento, proveedor, fechas,
importe y pago) y no describe cantidades de ropa propia del hotel.

## Alcance implementado (hito 1)

- Catalogo de proveedores y tipos de prenda, con alta y edicion de nombre, unidad, precio y estado.
- Creacion de remesas normales con precio historico por linea.
- Consulta de remesas por estado, proveedor y periodo.
- Registro manual de recibidos con proveedor, fecha, prendas, cantidades y observaciones.
- Creacion manual de remesas de relavado independientes y sin costo.
- Importe facturable cero para todo relavado.
- Recepciones parciales: cada recepcion informa cantidades incrementales.
- Cancelacion de remesas que todavia no tengan recepciones.
- Auditoria de creacion, recepcion y cancelacion.
- Control optimista de concurrencia mediante `version`.

## Reglas de negocio

Para cada linea se cumple:

`cantidad enviada = aceptada + relavada + faltante + danada + pendiente`

- Ninguna clasificacion puede ser negativa.
- Una recepcion no puede superar la cantidad pendiente.
- Una remesa queda `RECEIVED` cuando todas sus lineas estan conciliadas.
- Si queda saldo pasa a `PARTIALLY_RECEIVED`.
- Una remesa `REWASH` manual no depende de una recepcion ni de una linea de origen.
- El servidor fija en cero el precio y el importe de relavados; no confia en el cliente.
- No se permite cancelar una remesa que ya tenga cantidades recibidas o clasificadas.
- `sentAt` es obligatorio para remesas normales y para confirmar un despacho. Es `null` solamente
  mientras un relavado esta en `PENDING_DISPATCH`. El offset recibido se usa para conservar exactamente el instante
  indicado por el cliente. Se permiten fechas pasadas, actuales y futuras para poder registrar
  remesas con posterioridad o anticipacion; el servidor no reemplaza el valor recibido por su
  hora local. La respuesta puede normalizar un offset equivalente a UTC (`Z`).
- Los cambios de nombre, unidad o precio de un item solo afectan envios futuros. Cada linea
  conserva los valores historicos enviados mediante snapshots.
- Los items que ya no deben utilizarse se desactivan; no se eliminan para conservar la
  integridad de los envios historicos.

## Interfaz Flutter propuesta

El cliente Flutter se encuentra en otro repositorio y no forma parte de esta implementacion.
La pantalla recomendada tiene tres pestanas: **Pendientes**, **Nuevo envio** e **Historial**.
El alta usa un selector de proveedor y controles `- / cantidad / +` por prenda. La recepcion
abre solo las lineas pendientes y solicita cuatro cantidades. Los relavados se identifican
con la etiqueta `RELAVADO - SIN COSTO`.

Estados de pantalla obligatorios: carga, contenido, vacio y error. Antes de integrar debe
revisarse el gestor de estado, navegacion y cliente HTTP adoptados por ese repositorio.

## Contrato REST

Base: `/api/v1/laundry`

- `GET/POST /providers`
- `GET/POST /items`
- `PUT /items/{id}`
- `POST /shipments`
- `GET /shipments?status=&kind=&providerId=&dateFrom=&dateTo=`
- `GET /shipments/{id}`
- `POST /shipments/{id}/receive`
- `POST /shipments/{id}/dispatch`
- `PATCH /shipments/{id}/cancel`
- `GET /shipments/{id}/audit`

Los operadores pueden usar el modulo. El catalogo queda disponible en el mismo permiso para
no bloquear la operacion inicial; una fase posterior puede reservar su administracion al
supervisor si la experiencia del cliente permite distinguir acciones por rol.

### Alta y edicion de items

El alta y la edicion usan el mismo cuerpo. La edicion reemplaza todos los campos editables:

```json
{
  "name": "Alfombra de bano",
  "unitType": "PIECE",
  "currentUnitPrice": 8.50,
  "active": true
}
```

- `name`: obligatorio, maximo 120 caracteres y unico sin distinguir mayusculas.
- `unitType`: obligatorio; actualmente admite `PIECE`.
- `currentUnitPrice`: obligatorio, mayor o igual que cero y con hasta dos decimales.
- `active`: obligatorio en el contrato funcional; `false` impide usar el item en nuevos envios.
- `POST /items` responde `201 Created`.
- `PUT /items/{id}` responde `200 OK`, `404 Not Found` si no existe y `409 Conflict` si el
  nombre pertenece a otro item.

Respuesta de ambos endpoints:

```json
{
  "id": 6,
  "name": "Alfombra de bano",
  "unitType": "PIECE",
  "currentUnitPrice": 8.50,
  "active": true
}
```

## Prompt para implementar la administracion en el frontend

Implementa una ventana de administracion de items de lavanderia siguiendo la arquitectura,
componentes, gestion de estado, navegacion y cliente HTTP existentes en el frontend. No
introduzcas nuevas dependencias ni un nuevo gestor de estado.

La pantalla debe consumir `GET /api/v1/laundry/items`, mostrar nombre, tipo de unidad, precio
actual y estado, y permitir buscar o filtrar items. Incluye acciones **Nuevo item** y
**Editar**. Usa un formulario reutilizable para ambas operaciones con los campos `name`,
`unitType`, `currentUnitPrice` y `active`. Para alta usa `POST /api/v1/laundry/items`; para
edicion usa `PUT /api/v1/laundry/items/{id}`. Envia el precio como numero con un maximo de dos
decimales y usa inicialmente la opcion de unidad `PIECE`.

Valida nombre obligatorio de hasta 120 caracteres, unidad obligatoria y precio mayor o igual
que cero. Evita doble envio mientras se guarda. Gestiona estados de carga, lista vacia, error
y guardado. Ante `409`, informa que ya existe otro item con ese nombre; ante `404`, informa
que el item ya no existe y recarga la lista; muestra los demas errores con el mecanismo comun
del proyecto. Tras guardar, actualiza la lista y confirma la operacion. No implementes borrado:
la baja se realiza editando `active=false`. Mantiene accesibilidad, diseno responsive y pruebas
de listado, alta, edicion, validaciones y errores `409/404` segun la infraestructura existente.

## Ejemplo de envio

`sentAt` usa ISO 8601 con offset. Por ejemplo, el siguiente cuerpo permite registrar hoy una
remesa que realmente fue enviada el dia anterior:

```json
{
  "providerId": 1,
  "sentAt": "2026-10-29T12:00:00Z",
  "notes": "Retiro registrado al dia siguiente",
  "lines": [
    { "itemId": 1, "quantity": 20 }
  ]
}
```

## Ejemplo de recepcion

```json
{
  "receivedAt": "2026-10-31T15:00:00Z",
  "lines": [
    {
      "lineId": 10,
      "acceptedQuantity": 16,
      "rewashQuantity": 2,
      "missingQuantity": 1,
      "damagedQuantity": 1,
      "rewashReason": "INCOMPLETE_WASH"
    }
  ]
}
```

La recepcion no genera relavados y `generatedRewashShipmentId` permanece nulo por compatibilidad.

## Registro de relavados

El hotel registra el relavado directamente con `POST /shipments`, usando `kind: "REWASH"`.
La remesa nace en `SENT` con la fecha indicada y el backend fuerza precio e importe cero.
La recepcion no crea relavados y rechaza `rewashQuantity` mayor que cero.

El endpoint historico `POST /shipments/{id}/dispatch` permanece disponible para remesas antiguas
en `PENDING_DISPATCH`:

```json
{
  "sentAt": "2026-10-30T18:00:00-03:00",
  "version": 0,
  "notes": "Entregado al retiro de la tarde"
}
```

La respuesta es la remesa actualizada en `SENT`, con la version optimista incrementada. Solo se
despachan relavados pendientes; estado o version incompatibles responden `409`. Una remesa
pendiente no puede recibirse. Despues del despacho sigue el circuito normal:
`SENT -> PARTIALLY_RECEIVED -> RECEIVED`.

Un relavado `PENDING_DISPATCH` puede cancelarse con motivo mediante el endpoint existente. Se
conserva fisicamente y queda auditado. Los eventos `CREATE_REWASH` y `DISPATCH_REWASH` son
independientes.

La migracion `V42__allow_pending_laundry_rewash_dispatch.sql` hace nullable `sent_at` sin
modificar `V41`. Los relavados historicos en `SENT` no se reinterpretan; el nuevo estado solo se
aplica a registros creados despues del despliegue. El frontend Flutter ya implementa este contrato.

## Fases siguientes

1. Conciliacion de factura del proveedor, ajustes exclusivos de supervisor y reporte mensual.
2. Alertas por demora, faltantes recurrentes y tasa de relavado por proveedor/prenda.
3. Importacion controlada del historico util del Excel, sin asumir datos inexistentes.

## Criterios de aceptacion

- No se puede clasificar mas que el saldo pendiente.
- Un relavado se crea manualmente, sin depender de una recepcion previa.
- El importe facturable de un relavado es siempre cero.
- Dos recepciones concurrentes no pueden sobrescribirse silenciosamente.
- Tarifas nuevas no alteran remesas historicas.
- Un item puede crearse y editarse sin permitir nombres duplicados.
- Un item desactivado permanece consultable, pero no puede incluirse en nuevos envios.
- Toda mutacion operativa queda auditada.

## Validaciones ejecutadas

- Compilacion Maven del backend.
- Pruebas de dominio y contrato HTTP de lavanderia, incluyendo migraciones Flyway y validacion JPA.
- Suite Maven completa antes de entregar el cambio.

