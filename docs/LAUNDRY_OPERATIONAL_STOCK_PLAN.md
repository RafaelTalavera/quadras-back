# Plan de control operativo y stock de lavanderia

## 1. Objetivo y alcance

Evolucionar el modulo existente de lavanderia para controlar, por proveedor y tipo de
prenda, donde se encuentra fisicamente el stock, que cantidades siguen pendientes y que
cantidades fueron recibidas correctamente y pueden enviarse posteriormente a facturacion.

Este modulo no paga facturas ni registra pagos. Su salida es una base operativa auditable
con cantidades e importes elegibles para que otro sector realice la facturacion.

La operacion debe ser rapida: el usuario trabaja con cantidades agregadas por prenda y no
busca remesas antiguas. El backend conserva la trazabilidad y asigna automaticamente cada
resultado a las obligaciones mas antiguas mediante FIFO.

## 2. Decisiones funcionales confirmadas

- Las prendas son intercambiables dentro del mismo tipo. No se identifica cada pieza.
- Un envio significa que las prendas estan fisicamente en la lavanderia.
- Una recepcion significa que las prendas regresaron fisicamente al hotel.
- El hotel paga solamente las prendas recibidas limpias y aceptadas.
- Una prenda sucia, retenida para remojo, faltante o danada no es elegible para cobro.
- Si una prenda requiere uno o varios relavados, no genera un nuevo cobro por esos intentos.
  Solo se vuelve elegible una vez, cuando finalmente es recibida limpia y aceptada.
- Las prendas rechazadas por el hotel quedan en una cola de relavado dentro del hotel y se
  despachan manualmente cuando la gobernanta lo decida.
- La lavanderia puede retener prendas en sus instalaciones para remojo o relavado. Estas
  continúan pendientes en la lavanderia y no vuelven al stock disponible del hotel.
- Faltantes y danadas se informan separadamente. En esta fase no generan cobro, descuento,
  reposicion ni asiento financiero automatico.
- La lavanderia declara las devoluciones y las prendas retenidas. El hotel confirma o
  corrige; ante cualquier diferencia, la decision final pertenece al hotel.
- El proveedor no puede cambiar cantidades creadas por el hotel. Puede confirmar el
  documento completo o presentar una observacion.
- Se controlara stock total. Antes de activar el nuevo circuito se cargara una existencia
  inicial por tipo de prenda.

## 3. Modelo operativo propuesto

### 3.1 Saldos visibles

La pantalla principal muestra cantidades agregadas, no tickets:

| Saldo | Ubicacion fisica | Disponible para uso | Elegible para cobro |
|---|---|---:|---:|
| `HOTEL_AVAILABLE` | Hotel | Si | No aplica |
| `HOTEL_REWASH_QUEUE` | Hotel, esperando reenvio | No | No |
| `PROVIDER_PROCESSING` | Lavanderia, lavado normal | No | No |
| `PROVIDER_REWASH` | Lavanderia, remojo o relavado | No | No |
| `MISSING` | Ubicacion sin determinar | No | No |
| `DAMAGED` | Hotel o baja operativa pendiente | No | No |

La ubicacion de un faltante o danado debe quedar registrada en el movimiento. En esta fase
ambos saldos se muestran por separado y solo un supervisor puede regularizarlos mediante un
ajuste auditado.

### 3.2 Invariante de stock

Por cada tipo de prenda:

`stock inicial + altas - bajas = disponible + cola relavado hotel + proceso proveedor + relavado proveedor + faltante + danado`

Una confirmacion mueve cantidades entre saldos; nunca crea ni elimina prendas. Las unicas
operaciones capaces de alterar el total son un ajuste de inventario de supervisor con motivo
obligatorio o una carga inicial.

### 3.3 Asignacion FIFO invisible para el usuario

Cada linea de un envio normal crea una obligacion abierta por proveedor, prenda, cantidad y
precio historico. Al confirmar una devolucion, el servidor distribuye cada resultado contra
las obligaciones abiertas mas antiguas del mismo proveedor y prenda.

Ejemplo:

1. Hay envios abiertos de 6 y 4 toallas al mismo proveedor.
2. La lavanderia devuelve 7 limpias y retiene 3 para relavado.
3. El backend asigna 6 limpias al primer envio, 1 limpia al segundo y mantiene 3 unidades
   del segundo en `PROVIDER_REWASH`.
4. El usuario solo ve 7 aceptadas y 3 en relavado; no selecciona los envios.

Reglas FIFO:

- La clave de asignacion es `providerId + itemId`.
- Se ordena por fecha efectiva del envio, fecha de creacion e identificador.
- Una asignacion nunca cruza proveedores ni tipos de prenda.
- Si la devolucion supera el saldo abierto, la operacion se rechaza con `409 Conflict` y se
  devuelve el saldo disponible por prenda.
- La asignacion y todos los movimientos de stock se guardan en una sola transaccion.
- Se bloquean las obligaciones candidatas o se usa version optimista para impedir que dos
  recepciones consuman el mismo saldo.
- El detalle FIFO queda disponible en auditoria, pero no se exige en el formulario diario.

## 4. Circuitos

### 4.1 Envio normal

1. El hotel crea un borrador agregado por proveedor y prenda.
2. Al despacharlo, las cantidades pasan de `HOTEL_AVAILABLE` a `PROVIDER_PROCESSING`.
3. La lavanderia confirma el documento completo o lo observa; no puede editarlo.
4. Una observacion deja el documento en `OBSERVED`, pero no revierte el movimiento fisico.
5. El hotel corrige mediante un ajuste o resuelve la observacion. No se modifica
   silenciosamente el documento original.

Estados del documento: `DRAFT`, `SUBMITTED`, `CONFIRMED`, `OBSERVED`, `RESOLVED`,
`CANCELLED`.

Un documento despachado solo puede cancelarse mediante una operacion compensatoria auditada.

### 4.2 Devolucion declarada por la lavanderia

La lavanderia crea una devolucion con cantidades agregadas por prenda:

- `cleanQuantity`: vuelve limpia al hotel;
- `retainedRewashQuantity`: permanece fisicamente en la lavanderia;
- `notes`: observacion opcional.

La lavanderia puede guardar borradores y, al enviar, ya no puede editar las cantidades. El
hotel revisa y confirma o corrige el resultado final agregando, por prenda:

- limpias aceptadas;
- sucias rechazadas y recibidas en el hotel;
- retenidas en relavado dentro de la lavanderia;
- faltantes;
- danadas recibidas;
- observacion obligatoria cuando corrige lo declarado.

Al confirmar el hotel:

- limpia: `PROVIDER_PROCESSING/PROVIDER_REWASH -> HOTEL_AVAILABLE`;
- sucia recibida: `PROVIDER_PROCESSING/PROVIDER_REWASH -> HOTEL_REWASH_QUEUE`;
- retenida: `PROVIDER_PROCESSING -> PROVIDER_REWASH`;
- faltante: saldo proveedor -> `MISSING`;
- danada recibida: saldo proveedor -> `DAMAGED`.

Solamente la cantidad limpia aceptada crea elegibilidad de cobro. La confirmacion del hotel
es el hecho operativo que fija esa elegibilidad.

Para no detener la operacion si el proveedor no usa el portal, el hotel puede crear una
devolucion en nombre de la operacion. Esta queda `CONFIRMED_BY_HOTEL` y visible al proveedor
como documento para consulta u observacion posterior; una observacion no revierte el stock.

### 4.3 Relavado rechazado en el hotel

1. Las prendas sucias confirmadas quedan en `HOTEL_REWASH_QUEUE`.
2. La gobernanta crea manualmente un envio de relavado tomando cantidades de esa cola.
3. El backend aplica FIFO a las unidades rechazadas y conserva su obligacion y precio
   historico originales.
4. Al despachar pasan a `PROVIDER_REWASH`.
5. La lavanderia confirma u observa el envio sin cambiar cantidades.
6. Una devolucion posterior puede aceptarlas, rechazarlas nuevamente o mantenerlas en
   relavado.

El despacho de relavado tiene importe cero. Cuando una unidad finalmente se acepta limpia,
genera una unica elegibilidad vinculada a la obligacion normal original; nunca al documento
de relavado.

### 4.4 Relavado retenido en la lavanderia

Las prendas declaradas como retenidas pasan a `PROVIDER_REWASH` sin registrar una salida y
entrada ficticias. Permanecen visibles como saldo pendiente por proveedor, prenda y antiguedad.
La lavanderia las incluye en una devolucion futura. No existe fecha limite obligatoria, pero
el sistema debe mostrar la edad del saldo para evitar prendas olvidadas.

## 5. Cobro operativo

La unidad de elegibilidad es una aceptacion limpia confirmada por el hotel. Debe guardar:

- proveedor y prenda;
- cantidad aceptada;
- precio historico de la obligacion normal asignada;
- importe calculado;
- devolucion y linea que originaron la aceptacion;
- obligacion original asignada por FIFO;
- fecha de confirmacion y usuario del hotel;
- estado de exportacion: `PENDING_EXPORT` o `EXPORTED` para una fase futura.

El importe se calcula en backend. No se acepta desde Flutter ni desde el usuario proveedor.
Una restriccion unica sobre la asignacion aceptada debe impedir generar dos veces el mismo
cobro. Los reportes de relavado siempre muestran importe cero; el importe aparece solamente
en la aceptacion limpia definitiva.

## 6. Persistencia recomendada

No editar `V41` ni `V42`. Crear migraciones nuevas a partir de `V43`.

### 6.1 Evolucion de seguridad

- Agregar `PROVIDER` a `AppUserRole` y al `CHECK` de `app_users`.
- Agregar `laundry_provider_id` nullable en `app_users`, con clave foranea.
- Exigir proveedor para rol `PROVIDER` y prohibirlo para roles internos mediante servicio y
  pruebas; usar restriccion SQL si MySQL y el despliegue permiten expresarla con claridad.
- Incluir `providerId` en el principal autenticado para aplicar ownership en servidor.

### 6.2 Nuevas tablas sugeridas

- `laundry_stock_balances`: saldo actual por `item_id + bucket + provider_id nullable` y
  `version` para concurrencia.
- `laundry_stock_movements`: libro inmutable con origen, destino, cantidad, fecha efectiva,
  documento, actor y correlacion.
- `laundry_obligations`: saldo normal abierto por linea original, precio historico y version.
- `laundry_return_manifests` y `laundry_return_manifest_lines`: declaracion del proveedor,
  decision final del hotel, estados, versiones y autores.
- `laundry_allocations`: asignacion FIFO entre una linea de resultado y una obligacion.
- `laundry_billable_events`: aceptaciones limpias elegibles, con clave de idempotencia.
- `laundry_document_confirmations`: confirmacion u observacion inmutable por actor y rol.
- `laundry_opening_stock`: carga inicial aprobada por supervisor.

Se puede reutilizar `laundry_shipments` para envios normales y de relavado, agregando los
estados y metadatos necesarios. No conviene seguir usando `RECEIPT` como una remesa aislada:
las nuevas recepciones deben ser manifiestos vinculados mediante asignaciones FIFO.

Todos los campos de cantidad conservan `DECIMAL(12,3)` y los importes `DECIMAL(12,2)`, como
el modelo actual. Agregar indices para:

- obligaciones abiertas por proveedor, prenda y fecha FIFO;
- saldos por prenda, ubicacion y proveedor;
- manifiestos por proveedor, estado y fecha;
- movimientos por documento y por fecha;
- eventos cobrables por proveedor y fecha de aceptacion.

## 7. Contrato REST aditivo

Conservar temporalmente `/api/v1/laundry` para clientes existentes. Implementar el nuevo
flujo bajo `/api/v2/laundry` o con recursos nuevos que no cambien la semantica de los
endpoints actuales.

### 7.1 Consultas

- `GET /api/v2/laundry/dashboard?providerId=`: saldos agregados y antiguedad.
- `GET /api/v2/laundry/stock?itemId=&bucket=&providerId=`: detalle de stock.
- `GET /api/v2/laundry/documents?type=&status=&providerId=&from=&to=&page=`.
- `GET /api/v2/laundry/documents/{id}`: documento, confirmaciones y asignaciones.
- `GET /api/v2/laundry/billable?providerId=&from=&to=&exportStatus=`.

Las listas deben ser paginadas, con orden estable y DTO/proyecciones; no cargar todo el
historial ni serializar entidades JPA.

### 7.2 Comandos del hotel

- `POST /api/v2/laundry/dispatches`
- `POST /api/v2/laundry/dispatches/{id}/submit`
- `POST /api/v2/laundry/rewash-dispatches`
- `POST /api/v2/laundry/returns/{id}/confirm`
- `POST /api/v2/laundry/returns/manual`
- `POST /api/v2/laundry/discrepancies/{id}/resolve`
- `POST /api/v2/laundry/opening-stock`
- `POST /api/v2/laundry/stock-adjustments`

### 7.3 Comandos del proveedor

- `POST /api/v2/laundry/dispatches/{id}/acknowledgements`
- `POST /api/v2/laundry/returns`
- `PUT /api/v2/laundry/returns/{id}` solo mientras sea borrador propio
- `POST /api/v2/laundry/returns/{id}/submit`
- `POST /api/v2/laundry/documents/{id}/observations`

Cada comando de mutacion debe recibir:

- `Idempotency-Key` para evitar duplicados por reintento;
- `version` para control optimista;
- fecha efectiva ISO 8601 con offset;
- cantidades estrictamente positivas y maximo tres decimales.

Errores esperados: `400` validacion, `403` rol/ownership, `404` recurso no visible, `409`
version o saldo incompatible y `422` documento valido pero no conciliable. Mantener la
estructura comun de errores del proyecto.

## 8. Autorizacion

### Hotel

- `OPERATOR`: crear y confirmar movimientos operativos, resolver una diferencia ordinaria.
- `SUPERVISOR`: todas las acciones anteriores, carga inicial, ajustes de stock y reapertura
  excepcional.

### Proveedor

- Solo ve documentos y saldos de su `laundryProviderId`.
- Puede crear y enviar devoluciones propias.
- Puede confirmar u observar envios del hotel.
- No puede cambiar cantidades del hotel, confirmar en nombre del hotel, consultar precios,
  importes, otros proveedores, stock disponible interno ni usuarios.

No basta con ocultar pantallas. El servicio debe obtener el proveedor desde el principal y
no aceptar un `providerId` arbitrario en comandos del proveedor. Separar las reglas actuales
de `SecurityConfig`, que hoy exigen `OPERATOR` para todo `/api/v1/laundry/**`, y probar tanto
el rol como el ownership.

## 9. Flutter

Mantener la arquitectura actual `domain/application/infrastructure/presentation` y el cliente
HTTP existente. No agregar gestor de estado ni dependencias para esta funcionalidad.

### 9.1 Experiencia del hotel

- Tablero inicial con tarjetas: disponible, en lavanderia, relavado en hotel, relavado en
  lavanderia, faltante y danado.
- Acciones principales: `Enviar`, `Recibir/confirmar` y `Enviar a relavado`.
- Formularios agregados con controles de cantidad por prenda y una sola llamada al guardar.
- Bandeja de pendientes: devoluciones por confirmar, documentos observados y saldos antiguos.
- El detalle FIFO se muestra solo en una vista secundaria de trazabilidad.
- Estados explicitos de carga, vacio, error, guardado y conflicto `409`.

### 9.2 Experiencia del proveedor

- Shell y rutas limitados a lavanderia.
- Bandejas `Envios por confirmar`, `Crear devolucion`, `En relavado` e `Historial`.
- Confirmar u observar el documento completo; no renderizar controles editables sobre
  cantidades creadas por el hotel.
- No mostrar precios ni importes.

La app debe adaptar menu y rutas por rol, pero la autorizacion efectiva permanece en backend.

## 10. Compatibilidad y migracion de datos

Los movimientos actuales presentan recepciones `RECEIPT` y relavados independientes que no
siempre conservan origen. No asignarlos automaticamente a envios historicos porque podria
crear stock o cobros falsos.

Plan de corte:

1. Desplegar tablas y endpoints nuevos sin retirar V1.
2. Inventariar por proveedor y prenda los envios V1 abiertos, recepciones y relavados.
3. Mostrar un reporte de diferencias para revision de la gobernanta.
4. Cargar y aprobar el stock inicial y los saldos fisicos existentes en hotel/lavanderia.
5. Importar como obligaciones iniciales solo los saldos confirmados expresamente.
6. Marcar documentos V1 como `LEGACY`, solo consulta, sin generar eventos cobrables nuevos.
7. Activar V2 para nuevas operaciones.
8. Retirar las acciones V1 de Flutter cuando no queden clientes antiguos; conservar lectura
   historica.

La migracion debe ser repetible en un entorno de prueba, emitir conteos antes/despues y no
modificar V41/V42. Realizar respaldo antes del corte.

## 11. Fases de implementacion

### Fase 0: caracterizacion y contrato

- Congelar este documento como regla funcional.
- Crear ejemplos JSON y matriz de transiciones.
- Registrar snapshot de respuestas V1 para pruebas de compatibilidad.
- Definir el formato comun de error e idempotencia.

Salida: contrato revisado y pruebas de contrato inicialmente fallidas.

### Fase 1: libro de stock y apertura

- Migraciones desde V43 para saldos, movimientos, obligaciones y apertura.
- Servicios transaccionales de movimientos con invariantes.
- Carga inicial exclusiva de supervisor.
- Consultas agregadas del tablero.

Salida: stock total conciliado sin cambiar aun la interfaz operativa.

### Fase 2: envios y FIFO

- Adaptar nuevos envios normales para crear obligaciones.
- Implementar despacho agregado y movimiento a proveedor.
- Implementar asignador FIFO con bloqueo/concurrencia e idempotencia.
- Implementar envio manual desde `HOTEL_REWASH_QUEUE`.

Salida: ninguna prenda puede enviarse sin saldo y cada unidad pendiente conserva origen.

### Fase 3: devoluciones y elegibilidad

- Manifiesto de devolucion, confirmacion/correccion del hotel y movimientos por resultado.
- Retencion en lavanderia y multiples ciclos de relavado.
- Eventos cobrables solo para limpias aceptadas.
- Reportes de faltantes, danadas, antiguedad y elegibles para facturacion.

Salida: circuito hotel completo, aun utilizable sin cuenta del proveedor.

### Fase 4: portal de proveedor y doble confirmacion

- Rol `PROVIDER`, asociacion obligatoria y ownership.
- Rutas/DTO especificos sin precios.
- Confirmaciones, observaciones y resolucion final del hotel.
- Navegacion Flutter por rol.

Salida: colaboracion bilateral sin conceder al proveedor autoridad sobre stock o cobro.

### Fase 5: corte y observabilidad

- Reconciliacion y migracion controlada de saldos.
- Metricas: stock por ubicacion, pendientes por edad, discrepancias, tasa de relavado y
  aceptaciones limpias.
- Logs con identificador de correlacion sin datos sensibles.
- Desactivar escritura V1 y ejecutar validacion punta a punta con datos representativos.

## 12. Pruebas minimas obligatorias

### Backend

- Invariantes de stock en cada transicion.
- FIFO con varias remesas, recepcion parcial y multiples precios historicos.
- Dos recepciones concurrentes no consumen el mismo saldo.
- Reintento con la misma clave idempotente no duplica movimiento ni cobro.
- Prenda retenida permanece en proveedor y no genera cobro.
- Prenda rechazada entra en cola, puede relavarse varias veces y genera un solo evento al
  aceptarse limpia.
- Exceso sobre saldo responde conflicto sin cambios parciales.
- Proveedor no ve ni modifica otro proveedor, precios o acciones del hotel.
- Observacion no altera cantidades ni stock.
- Solo supervisor ajusta stock y todo ajuste conserva motivo/auditoria.
- Compatibilidad de lectura V1 y migraciones Flyway sobre esquema vacio y esquema V42.

### Flutter

- Tablero por saldo y estados carga/vacio/error.
- Formularios agregados y prevencion de doble envio.
- Conflicto de version recarga datos sin perder silenciosamente la operacion.
- Operador y supervisor ven acciones correctas.
- Proveedor solo ve su bandeja, confirma u observa sin editar cantidades.
- Precios e importes no aparecen en vistas de proveedor.
- Pruebas de serializacion de enums, fechas, cantidades y errores.

### Validacion punta a punta

Escenario obligatorio:

1. Stock inicial de 20 toallas.
2. Hotel envia 10; quedan 10 disponibles y 10 en proveedor.
3. Lavanderia declara 7 limpias y 3 retenidas.
4. Hotel acepta: 7 vuelven disponibles, 3 quedan en relavado proveedor y solo 7 son
   elegibles para cobro.
5. Luego recibe 2 limpias y 1 sucia: 2 generan elegibilidad una vez y 1 entra en cola hotel.
6. Hotel acumula y reenvia esa unidad manualmente sin costo.
7. Tras aceptarla limpia, el total elegible del envio original es 10, nunca 11.
8. La suma de todas las ubicaciones sigue siendo 20 en cada paso.

## 13. Criterios de finalizacion

- El usuario opera por cantidades agregadas sin localizar tickets.
- Toda cantidad visible puede explicarse mediante movimientos inmutables.
- El tablero responde con consultas agregadas y paginacion, sin cargar el historial completo.
- Ninguna aceptacion sucia, faltante, danada o retenida genera importe.
- Una unidad aceptada limpia genera como maximo un evento cobrable.
- El proveedor esta aislado por ownership y no puede cambiar decisiones del hotel.
- Los datos V1 permanecen consultables y no se convierten en cobros sin conciliacion humana.
- Pasan `mvnw.cmd test`, `flutter analyze` y `flutter test`, ademas del escenario punta a
  punta documentado.

## 14. Orden sugerido para un agente de implementacion

Entregar una fase por vez. En cada fase, el agente debe:

1. Leer este documento, `AGENTS.md` si existe y la implementacion actual completa de
   lavanderia y seguridad.
2. Informar diagnostico, archivos a modificar, compatibilidad y riesgos antes de editar.
3. Implementar primero backend, migracion y pruebas; despues contrato y Flutter.
4. No modificar migraciones existentes ni dependencias salvo autorizacion expresa.
5. Mantener DTO, JSON, enums, fechas y errores alineados entre Java y Dart.
6. Ejecutar la validacion minima de la fase y no avanzar si falla.
7. Actualizar este plan marcando evidencia real, no estimaciones, y hacer un commit cohesivo
   por fase.

No implementar todas las fases en un unico cambio. Las fases 1 a 3 forman el nucleo de
control operativo; la fase 4 agrega el acceso externo una vez estabilizadas las invariantes.

## 15. Registro de avance

### Fase 0 - contrato congelado (2026-11-03)

- Evidencia funcional:
  - contrato HTTP completo en `docs/LAUNDRY_OPERATIONAL_STOCK_CONTRACT_V2.md`;
  - ejemplos JSON parseables en `docs/laundry/v2-contract-examples.json`;
  - matriz de transiciones, errores, idempotencia, permisos y reglas FIFO congeladas.
- Decisiones registradas:
  - se usan tablas nuevas V2 y no se reutiliza `laundry_shipments` para el flujo nuevo;
  - la cabecera comun de documentos sera `laundry_documents` en Fase 2;
  - la apertura inicial es unica y exclusiva de supervisor;
  - los ajustes requieren supervisor, motivo y version del saldo origen;
  - `Idempotency-Key` es obligatoria en todo comando;
  - los errores conservan la estructura `ApiError` sin campos adicionales;
  - `422` queda reservado para conflictos no reconciliables de Fases 2 y 3;
  - V1 permanece intacto y consultable.
- Evidencia de compatibilidad:
  - `src/test/resources/laundry/v1-contract.json` congela los campos de proveedor, prenda,
    envio, linea, resumen, auditoria y error;
  - `LaundryV1CompatibilitySnapshotTest` valida esos campos contra respuestas reales V1,
    incluida una lectura `RECEIPT`;
  - `mvnw.cmd -B -Dtest=LaundryV1CompatibilitySnapshotTest test`: 1 prueba OK.
- Validacion de ejemplos: `docs/laundry/v2-contract-examples.json` parsea como JSON valido.
- Commits asociados:
  - `docs(laundry-stock): freeze V2 contract and examples`;
  - `test(laundry-stock): snapshot V1 compatibility contract`.
