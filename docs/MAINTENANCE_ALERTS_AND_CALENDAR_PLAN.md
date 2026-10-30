# PLAN INTEGRADO - AGENDA Y ALERTAS DE MANTENIMIENTO

Fecha: 2026-10-28  
Estado: `En implementacion`
Tipo: `Cross-repo`  
Backend: `C:/Users/Public/Documents/Proyectos/quadras`  
Frontend Flutter: `C:/Users/Public/Documents/Proyectos/quedras-front`  
Documento frontend asociado: `docs/MAINTENANCE_ALERTS_FRONTEND_PLAN.md`

## Objetivo

Completar el modulo de mantenimiento con una agenda temporal operativa y un
sistema confiable de avisos de tareas pendientes. El operador debe conocer que
una orden llego a su horario de inicio, consultar avisos aun si perdio el push y
resolverlos mediante reagendamiento o cierre exitoso.

Google Calendar queda expresamente fuera de este alcance. El sistema Costa
Norte conserva la autoridad sobre ordenes, horarios, estados y auditoria.

## Linea base comprobada

El sistema ya dispone de:

- ordenes con `scheduledStartAt` y `scheduledEndAt`;
- estados `OPEN`, `SCHEDULED`, `IN_PROGRESS`, `COMPLETED` y `CANCELLED`;
- alta, edicion, inicio, cierre y cancelacion;
- deteccion no bloqueante de conflictos por ubicacion;
- resumen diario con pendientes anteriores;
- planes preventivos con vencimiento y generacion manual de ordenes;
- sincronizacion de pantallas mediante eventos del sistema;
- auditoria de cambios de la orden.

No existe hoy:

- agenda diaria, semanal y mensual propia de mantenimiento;
- bandeja persistente ni campana global de notificaciones;
- deteccion automatica de inicio vencido o fin atrasado;
- push remoto ni registro de dispositivos/navegadores;
- notificacion del sistema con la aplicacion cerrada.

El documento historico `MANTENCION_FRONT_BACK_PLAN.md` declara un calendario
mensual implementado, pero esa afirmacion no coincide con la presentacion
actual y no debe utilizarse como criterio de cierre de este proyecto.

## Decisiones funcionales cerradas

### Momento de los avisos

Se generan dos tipos de aviso:

1. `DUE_TO_START`: al alcanzar `scheduledStartAt` si la orden continua
   `SCHEDULED`.
2. `OVERDUE`: al superar `scheduledEndAt` si la orden continua `SCHEDULED` o
   `IN_PROGRESS` y no fue completada ni cancelada.

El aviso inicial de esta funcionalidad es `DUE_TO_START`. El aviso `OVERDUE`
completa el seguimiento y evita que una orden iniciada quede abierta sin
control.

### Destinatarios

Cada aviso se dirige a ambos grupos:

- responsable asignado, cuando `assignedUsername` identifica una cuenta
  autenticada activa;
- usuarios activos con rol operativo de mantenimiento y supervisores
  autorizados.

La deduplicacion se realiza por usuario: si el responsable tambien pertenece al
grupo operativo recibe una sola notificacion. Un prestador externo sin cuenta
autenticada ni suscripcion push no puede recibir push de la aplicacion; sus
canales externos permanecen fuera de este alcance.

### Resolucion presentada al operador

Al pulsar el push o un elemento de la campana se abre la orden dentro de la
aplicacion. El dialogo presenta dos acciones principales:

- `Reagendar`: nueva fecha, hora inicial, hora final y motivo obligatorio.
- `Cerrar exitosamente`: confirmacion, fecha real de cierre y notas de
  resolucion obligatorias.

La notificacion del sistema no modifica directamente la orden. Esto evita
acciones accidentales, permite validar el estado autoritativo y mantiene la
misma experiencia entre plataformas. La accion existente `Iniciar trabajo`
permanece disponible en el detalle normal de la orden, pero no es una tercera
accion principal del aviso.

### Estado y ciclo de vida del aviso

Estados de notificacion:

- `UNREAD`: creada y pendiente de lectura;
- `READ`: visualizada por el usuario;
- `RESOLVED`: la orden fue reagendada, completada, iniciada cuando corresponda,
  cancelada o dejo de satisfacer la condicion;
- `DISMISSED`: descartada visualmente sin alterar la orden.

Descartar no resuelve la orden. El aviso permanece accesible en el historial,
pero no reaparece por la misma condicion `DUE_TO_START`. Si la orden alcanza
`scheduledEndAt` sin resolverse, la nueva condicion `OVERDUE` genera un aviso
independiente. Los recordatorios posteriores de atraso respetan la politica de
repeticion y nunca cambian el estado de la orden.

### Repeticion

- `DUE_TO_START`: un push por ocurrencia de agenda.
- `OVERDUE`: un push inicial al vencer y, como maximo, un recordatorio cada 60
  minutos durante la jornada.
- no se generan avisos fuera de la ventana operativa configurable;
- no se envia un nuevo push si la misma ocurrencia ya tiene uno pendiente de
  entrega;
- un reagendamiento crea una nueva ocurrencia y resuelve los avisos anteriores.

Valores y ventana operativa deben residir en configuracion de backend, no en el
cliente. La primera entrega fija 60 minutos y `06:00-23:00` en la zona
`America/Sao_Paulo` mientras se prepara la pantalla de configuracion.

## Agenda de mantenimiento

El frontend debe reutilizar el patron temporal ya usado por Quadras, Massagens
y Tours y el widget compartido `operational_timeline.dart`.

Vistas:

- diaria: linea temporal y detalle operativo del dia;
- semanal: vista predeterminada, con bloques proporcionales a inicio y fin;
- mensual: distribucion por dia y acceso al detalle diario.

Reglas:

- rango operativo inicial `06:00-23:00`;
- navegacion por dia, semana y mes;
- filtros por estado, prestador, ubicacion y prioridad;
- colores consistentes para agendada, en curso, atrasada, completada y
  cancelada;
- las ordenes sin agenda aparecen en un backlog separado, no en una hora
  ficticia;
- seleccionar un bloque abre el detalle y sus acciones;
- arrastrar una orden propone un reagendamiento, valida conflictos y requiere
  confirmacion antes de persistir;
- la agenda consulta por rango y no carga el historico completo.

## Arquitectura de notificaciones

La notificacion persistente del backend es la fuente de verdad. Push, SSE y la
campana son canales de entrega de la misma notificacion, no registros
independientes.

Flujo:

```text
Detector de vencimientos
        |
        v
maintenance_notifications + outbox (misma transaccion)
        |                         |
        |                         +--> proveedor push
        +--> API campana
        +--> evento SSE de invalidacion
```

### Backend

Componentes previstos:

- `MaintenanceNotificationDetector`: detecta candidatos por lotes;
- `MaintenanceNotificationService`: crea, lee y resuelve avisos;
- `MaintenanceNotificationOutboxDispatcher`: entrega push con reintentos;
- `PushProvider`: puerto desacoplado del proveedor concreto;
- adaptador Firebase Cloud Messaging para web, Android e iOS;
- registro de instalaciones y suscripciones push por usuario;
- evento SSE liviano para refrescar campana y agenda mientras la aplicacion esta
  abierta.

El detector debe ejecutarse con intervalo configurable y bloqueo que impida dos
ejecuciones concurrentes entre instancias. No debe recorrer todas las ordenes:
usara consultas indexadas por estado y fecha.

### Plataformas

- Flutter web/PWA: push remoto mediante FCM y service worker.
- Android/iOS: FCM cuando esas distribuciones se activen, con permisos nativos.
- Windows abierto: campana, SSE y toast local desde el cliente.
- Windows cerrado: requiere un adaptador nativo de push/WNS y empaquetado MSIX;
  queda fuera de la primera salida y solo se implementara si la operacion
  demuestra que el push web/PWA y la campana no cubren el uso real.

La campana persistente funciona en todas las plataformas y es el fallback
obligatorio cuando el usuario rechaza permisos o un proveedor push falla.

## Persistencia propuesta

### `maintenance_notifications`

- `id`
- `order_id`
- `recipient_username`
- `notification_type`
- `schedule_version`
- `title`
- `body`
- `status`
- `due_at`
- `read_at`
- `resolved_at`
- `dismissed_at`
- `created_at`
- `updated_at`

Restriccion unica recomendada:

`(order_id, notification_type, schedule_version, recipient_username)`.

### `push_subscriptions`

- `id`
- `username`
- `installation_id`
- `platform`
- `provider`
- token o suscripcion cifrada segun proveedor;
- `active`
- `last_seen_at`
- `created_at`
- `updated_at`

No se registran tokens completos en logs, auditoria ni respuestas de consulta.

### `notification_outbox`

- referencia a la notificacion;
- canal y destino logico;
- estado de entrega;
- contador de intentos;
- proximo intento;
- ultimo error sanitizado;
- clave de idempotencia;
- timestamps de creacion, entrega y abandono.

El evento se inserta en la misma transaccion que la notificacion. Una caida del
proveedor push nunca revierte ni bloquea la orden de mantenimiento.

### Version de agenda

Agregar `scheduleVersion` a `MaintenanceOrder`, incrementandolo cuando cambie
inicio o fin. Esta version permite deduplicar avisos y resolver de forma segura
las notificaciones de una agenda anterior.

## Contratos REST propuestos

### Bandeja

- `GET /api/v1/notifications?module=maintenance&status=UNREAD&page=0&size=20`
- `GET /api/v1/notifications/unread-count?module=maintenance`
- `PATCH /api/v1/notifications/{notificationId}/read`
- `PATCH /api/v1/notifications/{notificationId}/dismiss`

Las consultas se limitan al usuario autenticado salvo permisos de supervisor.

### Suscripciones push

- `POST /api/v1/push/subscriptions`
- `DELETE /api/v1/push/subscriptions/{installationId}`
- `PATCH /api/v1/push/subscriptions/{installationId}/heartbeat`

El alta es idempotente por usuario e `installationId`.

### Acciones de orden

- reutilizar `PUT /api/v1/maintenance/orders/{orderId}` para edicion general;
- agregar `PATCH /api/v1/maintenance/orders/{orderId}/reschedule` para expresar
  explicitamente nuevo horario, motivo y control de concurrencia;
- reutilizar `PATCH /api/v1/maintenance/orders/{orderId}/complete` para cierre.

`reschedule` debe recibir:

- `scheduledStartAt`
- `scheduledEndAt`
- `reason`
- `expectedScheduleVersion`

Si la version cambio, responde `409 Conflict` y el cliente recarga la orden. El
backend resuelve los avisos de la agenda anterior dentro de la misma
transaccion.

## Reglas de negocio y seguridad

- fechas y horas de agenda se intercambian como fecha/hora local sin sufijo, de
  acuerdo con el contrato actual, y se interpretan en la zona operativa
  confirmada `America/Sao_Paulo`;
- timestamps tecnicos y de entrega se guardan como instantes UTC;
- el servidor determina si una orden esta pendiente o atrasada;
- el cliente nunca decide por si solo que una notificacion esta resuelta;
- solo roles autorizados pueden reagendar o completar;
- el usuario solo consulta sus notificaciones, excepto la vista supervisora;
- reagendar exige motivo y genera auditoria con horario anterior y nuevo;
- completar exige notas de resolucion no vacias, conserva las demas
  validaciones actuales y registra actor y hora;
- operaciones repetidas deben ser idempotentes o responder con el estado actual;
- contenido del push no debe incluir datos sensibles de huespedes;
- secretos de Firebase quedan exclusivamente en backend/entorno seguro.

## Estrategia de compatibilidad

- los campos actuales de `MaintenanceOrderDto` se conservan;
- `scheduleVersion` se agrega de forma aditiva;
- los endpoints actuales continúan funcionando;
- el nuevo endpoint de reagendamiento no obliga a migrar clientes antiguos de
  inmediato;
- notificaciones se activan con feature flags por canal;
- primero se despliega persistencia/API, despues frontend y por ultimo push;
- si push esta desactivado, agenda y campana siguen funcionando.

## Fases de implementacion

### Fase 0 - Contrato y preparacion

- corregir la documentacion historica que declara calendario ya implementado;
- formalizar la seleccion de responsable asignado, operadores y supervisores;
- configurar la zona horaria operativa `America/Sao_Paulo`;
- definir feature flags y variables de entorno;
- agregar pruebas contractuales antes de activar envios.

Cierre: contratos y migraciones revisados sin cambiar comportamiento productivo.

### Fase 1 - Agenda temporal Flutter

- consulta de ordenes por rango;
- vistas diaria, semanal y mensual;
- backlog sin agenda;
- filtros y colores;
- apertura de detalle;
- reagendamiento por formulario y arrastre confirmado;
- pruebas responsive y de widgets.

Cierre: operador puede planificar y revisar carga sin usar un selector de fecha
aislado.

### Fase 2 - Notificaciones persistentes

- migraciones, entidades, repositorios e indices;
- detector idempotente;
- outbox;
- API de bandeja y contador;
- resolucion transaccional al reagendar, completar, iniciar o cancelar;
- campana global y dialogo de accion;
- SSE de invalidacion.

Cierre: los avisos sobreviven recarga, cambio de dispositivo y perdida de
conexion, aun sin proveedor push.

### Fase 3 - Push web/PWA

- proyecto y credenciales Firebase por ambiente;
- registro y revocacion de suscripcion;
- service worker web;
- permisos con explicacion previa;
- apertura profunda de la orden desde el push;
- metricas, reintentos y desactivacion de tokens invalidos.

Cierre: un navegador autorizado recibe el aviso con la aplicacion en segundo
plano y abre la orden correcta.

### Fase 4 - Movil y Windows

- Android/iOS: permisos, canales y validacion en segundo plano;
- Windows abierto: toast local integrado con el aviso persistente;
- evaluar WNS/MSIX para Windows cerrado solo con evidencia de necesidad
  operativa; no forma parte de la primera salida;
- matriz de pruebas por plataforma.

Cierre: cada plataforma declarada como soportada tiene evidencia en dispositivo
real. Las limitaciones restantes quedan visibles y no se presentan como push
completo.

### Fase 5 - Observabilidad y operacion

- metricas de detectadas, enviadas, fallidas, abiertas y resueltas;
- cola fallida y reintento supervisor;
- alerta por crecimiento de outbox;
- limpieza de suscripciones inactivas;
- manual operativo y estrategia de desactivacion.

Cierre: el equipo puede detectar fallos y desactivar el canal sin detener
mantenimiento.

## Pruebas requeridas

### Backend

- limites exactos de inicio y fin;
- cambio de estado antes de ejecutar el detector;
- reagendamiento y `scheduleVersion`;
- deduplicacion con dos ejecuciones concurrentes;
- aislamiento por usuario y rol;
- outbox en la misma transaccion;
- reintentos, token invalido y proveedor caido;
- zona horaria y cambio de fecha;
- paginacion y conteo no leido;
- compatibilidad de endpoints existentes.

### Flutter

- vistas diaria, semanal y mensual;
- ordenes sin agenda;
- apertura desde campana y deep link de push;
- reagendar y completar;
- `409` por version desactualizada;
- permiso aceptado, denegado y no disponible;
- estados carga, vacio, error y offline;
- badge actualizado por SSE y al volver al primer plano;
- responsive en web y Windows.

### Punta a punta

- crear orden futura, alcanzar inicio, recibir aviso y reagendar;
- recibir aviso y cerrar exitosamente;
- perder push y recuperar el aviso desde campana;
- editar desde otro cliente antes de actuar sobre el aviso;
- proveedor push caido sin afectar orden ni campana;
- verificar que no exista Google Calendar, OAuth de Google ni sincronizacion
  externa dentro de este alcance.

## Criterios de aceptacion

- una orden `SCHEDULED` genera una sola notificacion de inicio por version de
  agenda y destinatario;
- una orden no cerrada genera aviso de atraso al superar su fin;
- el operador siempre puede recuperar avisos desde la campana;
- el push abre la orden, pero no la modifica directamente;
- reagendar exige motivo, valida conflictos y resuelve avisos anteriores;
- cerrar exitosamente registra actor, hora y notas conforme al flujo actual;
- completar, cancelar o reagendar evita nuevos avisos obsoletos;
- fallos de push no afectan las transacciones de mantenimiento;
- agenda diaria, semanal y mensual consultan rangos acotados;
- permisos, auditoria, concurrencia e idempotencia tienen pruebas;
- Google Calendar permanece fuera del producto y de sus credenciales.

## Definiciones cerradas el 2026-10-28

1. Destinatarios: responsable autenticado asignado y usuarios operativos o
   supervisores autorizados, sin duplicar por usuario.
2. Zona horaria operativa: `America/Sao_Paulo`.
3. Descarte: oculta el aviso actual sin resolver la orden; no repite
   `DUE_TO_START`, pero permite el nuevo aviso `OVERDUE` y sus recordatorios cada
   60 minutos.
4. Cierre exitoso: notas de resolucion obligatorias.
5. Windows: la primera salida garantiza campana y avisos con la aplicacion
   abierta; WNS/MSIX con la aplicacion cerrada queda condicionado a evidencia de
   necesidad despues de validar web/PWA.

No quedan decisiones funcionales abiertas para iniciar la Fase 0 y la Fase 1.

## Avance de implementacion

### 2026-10-28 - Incremento 1: versionado y contrato de reagendamiento

Estado: `Completado y verificado`

Backend:

- migracion `V36__add_maintenance_schedule_version.sql` con valor inicial `1`
  para ordenes ya agendadas y `0` para backlog;
- `scheduleVersion` aditivo en entidad, DTO y proyecciones de ordenes;
- incremento automatico solo cuando cambia inicio o fin;
- `PATCH /api/v1/maintenance/orders/{orderId}/reschedule` con horario, motivo y
  `expectedScheduleVersion` obligatorios;
- respuesta `409 Conflict` si otro cliente modifico la agenda;
- auditoria `RESCHEDULED` con horario anterior, nuevo horario y motivo;
- evento de sincronizacion existente emitido despues del reagendamiento.

Flutter:

- lectura compatible de `scheduleVersion`, con valor `0` para servidores
  anteriores;
- modelo de solicitud y metodo HTTP para reagendar con control de version;
- prueba de serializacion del contrato y respuesta autoritativa.

Verificacion ejecutada:

- `./mvnw.cmd test`: 131 pruebas exitosas (incluye 14 de mantenimiento);
- `flutter analyze`: sin errores; conserva 42 avisos informativos preexistentes
  por APIs de Flutter deprecadas;
- `flutter test`: 77 pruebas exitosas (incluye 9 del adaptador de
  mantenimiento).

Pendiente para el siguiente incremento:

- consulta de agenda por intervalo solapado y backlog separado;
- vistas diaria, semanal y mensual con `operational_timeline.dart`;
- formulario y arrastre confirmado que consuman el endpoint implementado;
- configuracion efectiva de zona operativa y feature flags;
- persistencia, detector, bandeja, outbox, SSE y canales push.

### 2026-10-28 - Incremento 2: consulta operativa de agenda

Estado: `Completado y verificado`

Backend:

- agregado `GET /api/v1/maintenance/orders/calendar` con intervalo semiabierto
  `rangeStart` / `rangeEndExclusive`;
- la consulta incluye cualquier orden cuyo bloque se solape con el intervalo,
  aunque haya comenzado antes;
- backlog sin horario separado y limitado, con indicador `backlogHasMore`;
- filtros por ubicacion, prestador, tipo de prestador, estado y prioridad;
- rango maximo de 62 dias y backlog configurable entre 1 y 100 elementos;
- carga acotada de metadatos de adjuntos, sin consulta por cada orden.

Flutter:

- agregado modelo `MaintenanceCalendar`;
- agregado `getCalendar` al puerto y adaptador HTTP;
- prueba contractual para rango, filtros, ordenes agendadas y backlog.

Verificacion ejecutada:

- `./mvnw.cmd test`: 132 pruebas exitosas;
- `flutter analyze`: sin errores y con los mismos 42 avisos informativos
  preexistentes;
- `flutter test`: 78 pruebas exitosas.

Pendiente para el siguiente incremento:

- integrar el endpoint en estado/presentacion;
- construir la vista semanal predeterminada con
  `operational_timeline.dart`;
- agregar navegacion diaria y mensual, filtros y estados visuales;
- conectar formulario y arrastre con el reagendamiento versionado.

### 2026-10-28 - Incremento 3: agenda semanal visible

Estado: `Completado y verificado`

Flutter:

- la pantalla de mantenimiento consume `GET /maintenance/orders/calendar`
  para la semana seleccionada;
- agregado panel semanal con navegacion anterior, hoy y siguiente, reutilizando
  `operational_timeline.dart`;
- las ordenes agendadas muestran horario y estado visual, y abren el detalle
  operativo existente al seleccionarlas;
- los bloques que cruzan de fecha se segmentan por dia y se recortan al horario
  operativo `06:00-23:00`;
- backlog sin agenda separado, con indicador cuando existen mas resultados;
- estados vacios para agenda y backlog;
- el arrastre permanece deshabilitado hasta conectarlo al endpoint versionado y
  a la confirmacion obligatoria del motivo.

Verificacion ejecutada:

- `flutter analyze`: sin errores; conserva 42 avisos informativos preexistentes
  por APIs de Flutter deprecadas;
- `flutter test`: 79 pruebas exitosas, incluida la nueva cobertura de la agenda
  semanal y el backlog.

Pendiente para el siguiente incremento:

- conectar el formulario de reagendamiento y el arrastre confirmado con
  `scheduleVersion`, incluyendo tratamiento visual de `409 Conflict`;
- agregar filtros y vistas diaria y mensual;
- configurar zona operativa y feature flags antes del detector de avisos.

### 2026-10-28 - Incremento 4: reagendamiento operativo y concurrencia

Estado: `Completado y verificado`

Flutter:

- agregado `Agendar` / `Reagendar` al detalle de orden para estados editables;
- formulario con seleccion de inicio y fin, motivo obligatorio y validacion de
  intervalo;
- el envio utiliza `expectedScheduleVersion` de la orden visible;
- los bloques editables de la agenda semanal admiten arrastre, conservan su
  duracion y abren la misma confirmacion antes de persistir;
- ante `409 Conflict`, la aplicacion recarga el calendario autoritativo y avisa
  que otro usuario modifico la agenda;
- errores distintos de concurrencia conservan el mensaje del adaptador HTTP.

Verificacion ejecutada:

- `flutter analyze`: sin errores; conserva 42 avisos informativos preexistentes
  por APIs de Flutter deprecadas;
- `flutter test`: 81 pruebas exitosas;
- nuevas pruebas de widget para payload con motivo/version y recuperacion ante
  conflicto `409`.

Pendiente para el siguiente incremento:

- agregar filtros y vistas diaria y mensual;
- configurar zona operativa y feature flags;
- iniciar persistencia y detector de avisos internos.

### 2026-10-28 - Incremento 5: modos de agenda y filtros

Estado: `Completado y verificado`

Flutter:

- agregados modos diario, semanal y mensual con rangos semiabiertos de uno,
  siete y hasta 31 dias respectivamente;
- la navegacion anterior, hoy y siguiente adapta su salto al modo activo;
- las vistas diaria y semanal reutilizan la linea de tiempo operativa;
- la vista mensual presenta una lista cronologica compacta con fecha, horario,
  ubicacion y estado;
- agregados filtros por ubicacion, prestador, estado y prioridad, enviados al
  endpoint de calendario para mantener agenda y backlog consistentes;
- agregado restablecimiento conjunto de filtros y estados vacios por periodo.

Verificacion ejecutada:

- `flutter analyze`: sin errores; conserva 42 avisos informativos preexistentes
  por APIs de Flutter deprecadas;
- `flutter test`: 83 pruebas exitosas;
- nuevas pruebas de widget para rangos diario/semanal/mensual y propagacion de
  filtros al contrato de calendario.

Pendiente para el siguiente incremento:

- configurar zona horaria operativa y feature flags;
- iniciar persistencia y detector de avisos internos;
- agregar campana, bandeja y resolucion de avisos.

### 2026-10-28 - Incremento 6: configuracion y persistencia base de avisos

Estado: `Completado y verificado`

Backend:

- agregada configuracion tipada `costanorte.maintenance.alerts` con zona
  operativa `America/Sao_Paulo` y reloj dedicado;
- intervalo del detector configurable, con minimo de un segundo y valor por
  defecto de un minuto;
- feature flags independientes para bandeja interna, detector, SSE y push;
- todos los canales permanecen desactivados por defecto para evitar activar un
  flujo incompleto en produccion;
- migracion `V37__create_maintenance_notifications.sql` con tablas
  `maintenance_notifications` y `notification_outbox`;
- restriccion de deduplicacion por orden, tipo, version de agenda y
  destinatario, mas clave idempotente unica para outbox;
- agregados indices por destinatario/estado, vencimiento y reintento de outbox;
- agregadas entidades, enums y repositorios JPA como base del detector y la
  futura API de bandeja.

Variables de entorno:

- `COSTANORTE_MAINTENANCE_ALERTS_ZONE_ID`;
- `COSTANORTE_MAINTENANCE_ALERTS_DETECTOR_INTERVAL`;
- `COSTANORTE_MAINTENANCE_ALERTS_IN_APP_ENABLED`;
- `COSTANORTE_MAINTENANCE_ALERTS_DETECTOR_ENABLED`;
- `COSTANORTE_MAINTENANCE_ALERTS_SSE_ENABLED`;
- `COSTANORTE_MAINTENANCE_ALERTS_PUSH_ENABLED`.

Verificacion ejecutada:

- `./mvnw.cmd test`: 134 pruebas exitosas;
- Flyway aplico y valido las 37 migraciones sobre H2 en modo MySQL;
- Hibernate valido el mapeo de las nuevas entidades contra el esquema;
- prueba de configuracion confirma zona, intervalo y flags seguros.

Pendiente para el siguiente incremento:

- implementar detector idempotente y seleccion de destinatarios;
- persistir notificacion y outbox en una misma transaccion;
- agregar API de bandeja, contador y acciones de lectura/descarte.

### 2026-10-28 - Incremento 7: detector idempotente de avisos

Estado: `Completado y verificado`

Backend:

- agregado detector transaccional de avisos `DUE` y `OVERDUE`, con consultas
  acotadas por lote y exclusión de notificaciones ya persistidas para la misma
  orden, tipo, version de agenda y destinatario;
- la deteccion respeta la ventana operativa configurable `06:00-23:00` en la
  zona horaria definida para mantenimiento;
- los destinatarios incluyen usuarios activos con rol operador o supervisor y
  el responsable activo de la orden, sin duplicados por nombre de usuario;
- agregado bloqueo pesimista diario en base de datos para serializar la
  ejecucion entre instancias, complementado por las restricciones unicas de la
  migracion existente;
- notificacion interna y outbox push opcional se guardan en la misma
  transaccion, con clave idempotente deterministica por notificacion y canal;
- agregado scheduler condicionado por el feature flag del detector y por su
  intervalo configurable;
- agregadas propiedades para inicio/fin de jornada y tamano de lote;
- todos los flags permanecen desactivados por defecto, por lo que este
  incremento no activa envios en produccion.

Variables de entorno adicionales:

- `COSTANORTE_MAINTENANCE_ALERTS_OPERATIONAL_START`;
- `COSTANORTE_MAINTENANCE_ALERTS_OPERATIONAL_END`;
- `COSTANORTE_MAINTENANCE_ALERTS_DETECTOR_BATCH_SIZE`.

Verificacion ejecutada:

- `./mvnw.cmd test`: 138 pruebas exitosas;
- pruebas focalizadas para ambos tipos de aviso, limite de la ventana
  operativa, feature flag inactivo, deduplicacion de destinatarios y outbox;
- contexto Spring iniciado con el scheduler desactivado por defecto.

Alcance pendiente:

- resolver avisos vigentes al reagendar, iniciar, completar o cancelar una
  orden;
- agregar recurrencia cada 60 minutos para recordatorios `OVERDUE`; el detector
  actual emite solamente el primer aviso de vencimiento por version de agenda;
- agregar API de bandeja, contador y acciones de lectura/descarte;
- implementar despachador de outbox, SSE y canales push.

### 2026-10-28 - Incremento 8: ciclo de vida de avisos

Estado: `Completado y verificado`

Backend:

- los avisos activos de una orden se resuelven al reagendarla, iniciarla,
  completarla o cancelarla;
- la resolucion ocurre dentro de la misma transaccion que modifica la orden;
- solamente los estados `UNREAD` y `READ` cambian a `RESOLVED`, preservando los
  avisos ya descartados o resueltos;
- antes de resolver el aviso se abandonan los envios push `PENDING` asociados,
  evitando entregar mensajes obsoletos despues del cambio de agenda o estado;
- las actualizaciones se realizan mediante operaciones masivas acotadas por
  orden, sin cargar el historico completo de notificaciones;
- la fecha de resolucion y abandono se obtiene del reloj operativo y se
  persiste normalizada en UTC.

Verificacion ejecutada:

- `./mvnw.cmd test`: 140 pruebas exitosas;
- prueba unitaria de orden y timestamp compartido entre abandono y resolucion;
- prueba de integracion que crea una notificacion y un push pendientes,
  reagenda por la API y comprueba `RESOLVED` y `ABANDONED` en base de datos;
- las rutas existentes de inicio, finalizacion y cancelacion ejecutaron las
  consultas de ciclo de vida dentro de sus pruebas de integracion.

Pendiente para el siguiente incremento:

- agregar API autenticada de bandeja, contador no leido, lectura y descarte;
- agregar recurrencia cada 60 minutos para recordatorios `OVERDUE`;
- implementar despachador de outbox, SSE y canales push.

### 2026-10-28 - Incremento 9: API autenticada de bandeja

Estado: `Completado y verificado`

Backend:

- implementado `GET /api/v1/notifications` con filtro opcional por estado,
  paginacion por `Slice`, orden estable descendente y tamano maximo de 100;
- implementado `GET /api/v1/notifications/unread-count` para el contador de
  avisos `UNREAD` del usuario autenticado;
- implementados `PATCH /api/v1/notifications/{notificationId}/read` y
  `PATCH /api/v1/notifications/{notificationId}/dismiss` con semantica
  idempotente y respuesta `204 No Content`;
- las consultas usan proyeccion y no cargan la orden ni el historico completo;
- listado, contador y mutaciones quedan estrictamente limitados al nombre del
  usuario autenticado, incluso para supervisores;
- un ID perteneciente a otro destinatario responde `404 Not Found` para no
  revelar su existencia;
- `module` admite actualmente `maintenance`, sin introducir soporte ficticio
  para otros modulos;
- lectura y descarte registran timestamps UTC desde el reloj operativo.

Verificacion ejecutada:

- `./mvnw.cmd test`: 141 pruebas exitosas;
- prueba de integracion para listado propio, filtro `UNREAD`, contador, lectura
  repetida, descarte y aislamiento frente a notificaciones ajenas;
- contexto de seguridad iniciado con las cuatro rutas autenticadas.

Pendiente para el siguiente incremento:

- integrar campana, contador y bandeja global en Flutter;
- agregar recurrencia cada 60 minutos para recordatorios `OVERDUE`;
- implementar despachador de outbox, SSE y canales push.

### 2026-10-28 - Incremento 10: campana y bandeja Flutter

Estado: `Completado y verificado`

Flutter:

- agregados modelos y contrato HTTP tipados para la bandeja de mantenimiento;
- agregada campana global con contador de no leidos y semantica accesible;
- agregada bandeja con estados de carga, vacio, error y reintento;
- lectura y descarte consumen las acciones idempotentes del backend;
- al abrir un aviso se navega a mantenimiento, se selecciona el dia de
  `dueAt`, se recarga la agenda y se abre la orden relacionada;
- si la orden ya no aparece en agenda, backlog o resumen operativo, se muestra
  un mensaje controlado;
- el contador se refresca al iniciar, al reanudar la aplicacion y al cerrar la
  bandeja, sin introducir polling periodico.

Verificacion ejecutada:

- `flutter analyze`: sin errores y con los mismos 42 avisos informativos
  preexistentes;
- `flutter test`: 85 pruebas exitosas;
- pruebas nuevas para rutas/serializacion HTTP y flujo visual de campana,
  lectura y apertura.

Pendiente para el siguiente incremento:

- invalidar contador y bandeja en tiempo real mediante SSE;
- agregar recurrencia cada 60 minutos para recordatorios `OVERDUE`;
- implementar despachador de outbox y canales push.

### 2026-10-28 - Incremento 11: invalidacion de avisos por SSE

Estado: `Completado y verificado`

Backend:

- reutilizado el canal autenticado `GET /api/v1/sync/events`, evitando una
  segunda conexion SSE y nuevas dependencias;
- agregado evento compatible de dominio `maintenance` y accion
  `notifications-changed`, sin ID, destinatario ni contenido sensible;
- el evento se solicita al crear, leer, descartar o resolver avisos;
- la infraestructura existente lo transmite solamente despues del commit, por
  lo que el cliente nunca invalida su estado por una transaccion revertida;
- la emision queda condicionada por `costanorte.maintenance.alerts.sse-enabled`
  y permanece desactivada por defecto.

Flutter:

- la campana escucha el stream SSE compartido y refresca el contador al
  recibir `maintenance/notifications-changed`;
- una bandeja abierta escucha el mismo stream y recarga su contenido;
- ambas suscripciones se cancelan al liberar sus widgets;
- eventos existentes y clientes anteriores mantienen compatibilidad porque la
  estructura de `schedule-changed` no cambia.

Verificacion ejecutada:

- `./mvnw.cmd test`: 143 pruebas exitosas;
- `flutter analyze`: sin errores y con 42 avisos informativos preexistentes;
- `flutter test`: 85 pruebas exitosas;
- pruebas de feature flag SSE, emision del contrato y reaccion visual al
  evento de invalidacion.

Activacion:

- `COSTANORTE_MAINTENANCE_ALERTS_SSE_ENABLED=true`.

Pendiente para el siguiente incremento:

- agregar recurrencia cada 60 minutos para recordatorios `OVERDUE`;
- implementar despachador de outbox y canales push;
- validar reconexion y latencia SSE en un entorno desplegado con multiples
  instancias.

### 2026-10-28 - Incremento 12: recordatorios `OVERDUE` recurrentes

Estado: `Completado y verificado`

Backend:

- agregado intervalo configurable para recordatorios vencidos, con valor por
  defecto de 60 minutos;
- cada recordatorio usa una ocurrencia deterministica basada en el tiempo
  transcurrido desde `scheduledEndAt` (`OVERDUE-0`, `OVERDUE-1`, etc.);
- la seleccion exige que no exista otro aviso `OVERDUE` de la misma version de
  agenda creado dentro del intervalo actual;
- despues de una interrupcion solamente se genera la ocurrencia vigente, sin
  emitir en rafaga todos los intervalos historicos omitidos;
- Flyway V38 agrega `occurrence_key` y extiende la unicidad por orden, tipo,
  version de agenda, destinatario y ocurrencia; los registros existentes se
  conservan como `INITIAL`;
- la clave idempotente del outbox incluye la ocurrencia, permitiendo un push
  por recordatorio sin duplicarlo entre ejecuciones o instancias;
- se mantienen la ventana operativa, el limite por lote, el bloqueo diario
  multiinstancia y la resolucion de todos los avisos activos al cambiar el
  ciclo de vida de la orden;
- el contrato de la bandeja no cambia: cada recurrencia aparece como un aviso
  independiente y los clientes actuales siguen siendo compatibles.

Configuracion:

- `COSTANORTE_MAINTENANCE_ALERTS_OVERDUE_REMINDER_INTERVAL=PT60M`;
- el detector continua requiriendo
  `COSTANORTE_MAINTENANCE_ALERTS_DETECTOR_ENABLED=true`.

Verificacion ejecutada:

- `./mvnw.cmd test`: 144 pruebas exitosas;
- 38 migraciones Flyway aplicadas y esquema validado;
- pruebas de ocurrencias consecutivas, deduplicacion y claves de outbox
  distintas por intervalo.

Pendiente para el siguiente incremento:

- implementar el despachador de outbox y los canales push;
- validar detector, bloqueo multiinstancia, reconexion y latencia SSE en un
  entorno desplegado.

### 2026-10-28 - Incremento 13: despachador transaccional de outbox

Estado: `Completado y verificado`

Backend:

- implementado `MaintenanceNotificationOutboxDispatcher` por lotes y con
  intervalo configurable;
- agregado puerto `PushProvider`, independiente del proveedor concreto, y un
  mensaje minimo con destino logico, clave idempotente, orden, titulo, cuerpo
  y vencimiento;
- cada fila se reclama mediante una actualizacion atomica
  `PENDING -> PROCESSING`, evitando que dos instancias entreguen el mismo
  trabajo en condiciones normales;
- la llamada remota ocurre fuera de la transaccion de reclamacion para no
  retener conexiones ni bloqueos de base de datos durante el acceso al
  proveedor;
- una entrega exitosa pasa a `DELIVERED`; un fallo vuelve a `PENDING` con
  backoff exponencial acotado y, al alcanzar el maximo, pasa a `ABANDONED`;
- las reclamaciones `PROCESSING` vencidas se recuperan automaticamente tras
  el timeout configurado, cubriendo caidas entre reclamacion y confirmacion;
- cambios de ciclo de vida abandonan tanto filas `PENDING` como `PROCESSING`;
- solamente se persiste el tipo de excepcion del proveedor, sin mensajes que
  pudieran contener tokens u otros datos sensibles;
- Flyway V39 agrega `claimed_at` y su indice de recuperacion;
- el scheduler requiere simultaneamente `push-enabled=true` y una
  implementacion de `PushProvider`; sin adaptador concreto no simula entregas
  ni elimina trabajos pendientes.

Configuracion predeterminada:

- despacho cada `PT10S`, lote de 50 y reclamacion valida por `PT2M`;
- 8 intentos, demora inicial `PT30S` y demora maxima `PT30M`;
- todos los valores pueden sobrescribirse mediante variables
  `COSTANORTE_MAINTENANCE_ALERTS_OUTBOX_*`.

Verificacion ejecutada:

- `./mvnw.cmd test`: 147 pruebas exitosas;
- 39 migraciones Flyway aplicadas y esquema validado;
- pruebas de entrega, recuperacion previa, backoff exponencial, sanitizacion
  de errores y abandono al alcanzar el limite de intentos.

Pendiente para el siguiente incremento:

- persistir y administrar suscripciones push por instalacion;
- implementar el adaptador Firebase Cloud Messaging y sus timeouts;
- validar concurrencia e idempotencia contra MySQL y el proveedor reales.

### 2026-10-28 - Incremento 14: suscripciones push por instalacion

Estado: `Completado y verificado`

Backend:

- implementada la tabla `push_subscriptions` mediante Flyway V40, con
  plataforma, proveedor, instalacion, usuario, actividad y timestamps UTC;
- implementados los endpoints autenticados previstos:
  `POST /api/v1/push/subscriptions`,
  `PATCH /api/v1/push/subscriptions/{installationId}/heartbeat` y
  `DELETE /api/v1/push/subscriptions/{installationId}`;
- el alta recibe `installationId`, `platform`, `provider` y `token`, y devuelve
  solamente metadatos de la instalacion; el token nunca se serializa;
- plataformas iniciales: `WEB`, `ANDROID` e `IOS`; proveedor inicial:
  `FIREBASE`;
- el alta es idempotente por usuario e instalacion y permite rotar el token;
- heartbeat y baja verifican ownership; una suscripcion ajena o inactiva no se
  puede reactivar sin presentar nuevamente un token valido;
- la baja es idempotente para una instalacion conocida y elimina token cifrado
  y hash;
- una instalacion o token registrados por otro usuario desactivan el registro
  anterior, evitando entregar avisos de una sesion previa en un dispositivo
  compartido;
- el token se cifra en reposo con AES-GCM e IV aleatorio; SHA-256 se utiliza
  exclusivamente para detectar tokens activos duplicados;
- las respuestas, errores y logs no incluyen el token ni su valor cifrado;
- el registro se serializa por usuario mediante bloqueo pesimista para evitar
  duplicados concurrentes del mismo `installationId`.

Configuracion obligatoria para registrar instalaciones:

- `COSTANORTE_PUSH_SUBSCRIPTIONS_ENCRYPTION_KEY`, con al menos 32 caracteres;
- si la clave no esta configurada, el alta responde `503 Service Unavailable`
  y el resto de la aplicacion continua operativo;
- la clave debe conservarse estable para poder descifrar suscripciones ya
  guardadas; su rotacion requiere una estrategia explicita posterior.

Verificacion ejecutada:

- `./mvnw.cmd test`: 151 pruebas exitosas;
- 40 migraciones Flyway aplicadas y esquema validado;
- pruebas HTTP de autenticacion, validacion, alta, rotacion, heartbeat, baja
  repetida, ownership y reasignacion de dispositivo;
- prueba criptografica de cifrado/descifrado, hash e IV aleatorio.

Pendiente para el siguiente incremento:

- implementar el adaptador Firebase Cloud Messaging usando las suscripciones
  activas y desactivar tokens invalidados por el proveedor;
- integrar registro, heartbeat y baja en Flutter web/PWA;
- validar concurrencia e idempotencia contra MySQL y Firebase reales.

### 2026-10-28 - Incremento 15: adaptador Firebase Cloud Messaging

Estado: `Completado y verificado en aislamiento`

Backend:

- implementado `FirebasePushProvider` sobre la API HTTP v1 oficial, sin agregar
  dependencias de produccion;
- implementada autenticacion OAuth 2.0 mediante cuenta de servicio, JWT RS256,
  alcance `firebase.messaging` y cache del access token hasta un minuto antes
  de su expiracion;
- las credenciales se leen desde el archivo indicado por
  `GOOGLE_APPLICATION_CREDENTIALS` y no se cargan desde el repositorio;
- cada trabajo del outbox se entrega a las suscripciones Firebase activas del
  destinatario;
- respuestas `UNREGISTERED` y `INVALID_ARGUMENT` con payload controlado
  desactivan y eliminan el token local; fallos transitorios regresan al
  despachador para aplicar su politica de reintentos;
- el push usa titulo y cuerpo genericos, sin nombre de huesped, ubicacion ni
  descripcion operativa; los datos incluyen solamente modulo, ID de orden y
  clave idempotente;
- Android, APNs y Web Push reciben una clave de colapso derivada de la clave
  idempotente para reducir duplicados durante reintentos;
- conexion y solicitud poseen timeouts configurables;
- la aplicacion falla al iniciar si Firebase se activa con credenciales
  ausentes o invalidas, evitando un estado aparentemente habilitado sin
  capacidad de entrega.

Activacion:

- `COSTANORTE_FIREBASE_ENABLED=true`;
- `GOOGLE_APPLICATION_CREDENTIALS=C:\\ruta\\service-account.json`;
- `COSTANORTE_FIREBASE_PROJECT_ID` es opcional y, si se omite, se usa
  `project_id` de la cuenta de servicio;
- tambien deben estar activos
  `COSTANORTE_MAINTENANCE_ALERTS_PUSH_ENABLED=true` y el detector;
- timeouts predeterminados: conexion `PT5S` y solicitud `PT10S`.

Verificacion ejecutada:

- `./mvnw.cmd test`: 156 pruebas exitosas;
- pruebas del endpoint/proyecto y header OAuth, clasificacion de token
  `UNREGISTERED`, fan-out, invalidacion y propagacion de fallos transitorios;
- contexto completo iniciado con Firebase desactivado por defecto.

Limitacion de verificacion:

- no se realizo una entrega contra un proyecto Firebase real porque el
  repositorio no contiene ni debe contener credenciales de cuenta de servicio;
- la garantia extremo a extremo queda pendiente de las credenciales y la
  configuracion cliente del entorno de pruebas.

Pendiente para el siguiente incremento:

- integrar Firebase Messaging y el ciclo de suscripcion en Flutter web/PWA;
- configurar el proyecto Firebase y realizar una entrega extremo a extremo;
- validar concurrencia e idempotencia contra MySQL y Firebase reales.

### 2026-10-28 - Incremento 16: cliente Firebase Flutter web/PWA

Estado: `Implementado y verificado sin credenciales reales`

Flutter:

- agregadas dependencias directas `firebase_core` y `firebase_messaging`, en
  versiones resueltas compatibles con el SDK del proyecto;
- Firebase permanece desactivado por defecto y la aplicacion conserva su
  comportamiento anterior cuando no recibe configuracion;
- la configuracion cliente se recibe mediante `--dart-define`, sin valores
  Firebase escritos en el repositorio;
- implementado registro mediante `register` y `onRegistered`, usando el
  Firebase Installation ID como `installationId` y destino HTTP v1;
- al iniciar sesion se solicita permiso y registra la instalacion; al reanudar
  se envia heartbeat y al cerrar sesion se elimina la suscripcion antes de
  descartar las credenciales locales;
- mensajes en primer plano invalidan el contador de la campana;
- abrir un push desde segundo plano o con la aplicacion terminada navega a
  mantenimiento y abre la orden usando `orderId` y `dueAt`;
- agregado service worker web compatible con la version Firebase JS soportada
  por FlutterFire; recibe su configuracion mediante parametros del registro;
- agregado script `scripts/run_web_with_firebase.ps1`, que valida variables y
  ejecuta Chrome con los `--dart-define` necesarios;
- Windows conserva campana y SSE, porque Firebase Messaging no soporta esa
  plataforma en este proyecto; Android/iOS requieren crear primero sus
  carpetas y configuracion nativa, que actualmente no existen en el repo.

Backend:

- el payload FCM agrega `dueAt` para que el deep link consulte el periodo
  correcto sin exponer descripcion, ubicacion ni datos de huespedes.

Verificacion ejecutada:

- `flutter analyze`: sin errores y con los mismos 42 avisos informativos
  preexistentes;
- `flutter test`: 88 pruebas exitosas;
- pruebas nuevas del contrato REST de alta, heartbeat, baja, autorizacion,
  idempotencia de baja y propagacion de fallos;
- prueba de aplicacion para inicio y fin del ciclo push junto con la sesion.

Configuracion pendiente del operador:

- crear o seleccionar el proyecto Firebase y registrar la aplicacion web;
- obtener API key, project ID, messaging sender ID, web app ID y VAPID publica;
- generar y guardar fuera del repositorio la cuenta de servicio del backend;
- configurar las variables descritas en el plan Flutter y backend;
- ejecutar una entrega real extremo a extremo.

Pendiente para finalizar la validacion del plan:

- prueba real en Chrome con permiso concedido y aplicacion en primer plano,
  segundo plano y cerrada;
- validacion desplegada con MySQL, multiples instancias y Firebase real.

### 2026-10-28 - Incremento 17: compatibilidad de V38 con MySQL

Estado: `Corregido; pendiente de reejecucion autorizada`

- la primera ejecucion local contra MySQL aplico V36 y V37, pero V38 fallo al
  eliminar `uk_maintenance_notifications_dedup` porque InnoDB lo habia elegido
  como indice de soporte para la clave foranea de `order_id`;
- V38 ahora crea primero `idx_maintenance_notifications_order`, de modo que la
  clave foranea conserva un indice valido antes de sustituir la restriccion
  unica por la variante que incluye `occurrence_key`;
- el fallo se produjo en la primera sentencia de V38, por lo que esa migracion
  no alcanzo a aplicar cambios parciales;
- `git diff --check` no detecto errores de whitespace (solo advertencias de
  normalizacion LF/CRLF preexistentes).

Pendiente inmediato:

- reemplazar el valor de ejemplo `TU_PROJECT_ID` por el ID real de Firebase;
- autorizar y repetir el arranque local para aplicar V38-V40 y verificar el
  contexto Spring completo.

### 2026-10-28 - Incremento 18: prueba funcional real sobre MySQL local

Estado: `Campana y detector verificados; push visual pendiente`

- autorizado y verificado el esquema local en version Flyway `40`;
- backend local iniciado en `localhost:8080` con bandeja, detector, SSE, push y
  adaptador Firebase habilitados;
- creada mediante la API la orden de prueba `276`, estado `SCHEDULED`, para
  `Areas comuns piso 1`, con inicio `17:42:27` y fin `17:45:27`;
- el detector creo `DUE_TO_START` a las `17:42:30`, tres segundos despues del
  inicio programado;
- al permanecer la orden en `SCHEDULED`, creo `OVERDUE` a las `17:45:30`, tres
  segundos despues del fin programado;
- ambos avisos quedaron `UNREAD` y fueron recuperados correctamente por la API
  de bandeja para `operador.demo`.

Pendiente de esta prueba:

- confirmar visualmente el push de Chrome con el frontend local ejecutandose,
  sesion iniciada, permiso concedido e instalacion Firebase registrada;
- completar pruebas de segundo plano y navegador cerrado.

### 2026-10-29 - Incremento 19: dialogo operativo automatico

Estado: `Implementado y verificado en frontend`

- completada la parte pendiente de `campana global y dialogo de accion` del
  alcance original;
- los eventos nuevos `DUE_TO_START` y `OVERDUE` abren automaticamente la orden
  cuando la aplicacion esta activa;
- el dialogo de inicio permite iniciar, editar o reagendar;
- el dialogo de fin permite concluir una orden agendada cuyo horario termino o
  reagendarla si necesita mas tiempo;
- se preserva la campana como historial y no se abren avisos anteriores al
  inicio de la sesion;
- verificacion Flutter: 91 pruebas exitosas y analisis sin errores nuevos.
