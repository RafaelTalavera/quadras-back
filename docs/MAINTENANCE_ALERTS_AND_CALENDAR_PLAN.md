# PLAN INTEGRADO - AGENDA Y ALERTAS DE MANTENIMIENTO

Fecha: 2026-10-28  
Estado: `Propuesto para implementacion`  
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
