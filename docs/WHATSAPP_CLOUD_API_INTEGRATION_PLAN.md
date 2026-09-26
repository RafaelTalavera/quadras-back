# PLAN DE INTEGRACION WHATSAPP CLOUD API

> **Histórico, sustituido el 2026-09-14.** La decisión vigente y el registro
> de ejecución están en `WHATSAPP_MANUAL_MIGRATION_2026-09-14.md`.

Fecha de inicio: 2026-07-24  
Ultima revision: 2026-08-18  
Estado general: Base tecnica implementada; activacion Meta pendiente  
Tipo de trabajo: Cross-repo

## Implementacion realizada el 2026-08-18

### Backend

- Migracion Flyway `V32__create_whatsapp_integration.sql`:
  - separa `whatsapp_number` y `whatsapp_enabled` en proveedores de masajes y
    mantenimiento;
  - crea configuracion singleton, mensajes/outbox y solicitudes externas;
  - agrega claves unicas de idempotencia e indices de cola, uso y agregado.
- Propiedades tipadas `costanorte.whatsapp`; tokens y secretos solo por ambiente.
- Emision desacoplada desde `MassageBookingService` y `MaintenanceOrderService`.
- Elegibilidad:
  - proveedor activo, numero valido y envio habilitado;
  - mantenimiento exige proveedor `EXTERNAL`.
- Worker persistente con `DRY_RUN`, hasta cinco intentos y backoff.
- Cliente HTTP para plantillas de WhatsApp Cloud API con version configurable.
- Estados separados para transporte y respuesta del proveedor.
- Control previo al envio por presupuesto mensual, limites mensual/diario y limite
  diario por proveedor.
- Webhook publico con challenge y validacion `X-Hub-Signature-256` sobre bytes
  crudos; actualiza entrega/lectura/fallo e ingresa respuestas idempotentes.
- Procesamiento de botones `ACCEPT`, `DECLINE` y base `PROPOSE_VISIT` relacionada
  mediante el `wamid` de contexto.
- APIs supervisor para settings, uso, mensajes y reintento; seguridad aplicada en
  Spring (`SUPERVISOR`), no solo en UI.
- APIs operativas para consultar la ultima solicitud externa de un masaje u orden.

### Frontend

- Modelos de proveedores alineados con `whatsappNumber` y `whatsappEnabled`.
- Catalogo de masajes permite contacto general, numero WhatsApp y activacion.
- Catalogo de mantenimiento permite numero y activacion automatica solo para
  responsables externos.
- `Configuracoes` incorpora panel exclusivo de supervisor con:
  - activacion y kill switch;
  - `DRY_RUN`;
  - mensajes salientes/entrantes y estados;
  - costo estimado, presupuesto y porcentaje consumido;
  - limites mensual, diario y por proveedor/dia.
- Cliente HTTP Flutter consume los contratos reales del backend.

### Activacion deliberadamente pendiente

La implementacion permanece segura por defecto: `enabled=false`, `dryRun=true` y
proveedores sin WhatsApp habilitado. Para una activacion real todavia se requiere:

1. resolver/confirmar el estado comercial de Meta documentado anteriormente;
2. cargar credenciales protegidas y URL HTTPS publica;
3. crear y aprobar las dos plantillas Utility con sus botones;
4. completar el formulario de fecha mediante WhatsApp Flow para
   `PROPOSE_VISIT` (el modelo y estado ya existen, pero no se interpreta texto libre);
5. cargar la tarifa vigente de Brasil y aprobar el presupuesto inicial;
6. ejecutar prueba con numero Meta/allowlist antes del numero real.

No se considera habilitada la comunicacion real hasta completar estos controles.

## Alcance confirmado en la revision 2026-08-18

Esta iniciativa cubre inicialmente dos procesos con proveedores externos:

- `Massagens`: al crear o modificar un agendamiento confirmado, notificar al
  prestador que tenga WhatsApp habilitado. El proveedor puede aceptar o rechazar,
  pero no proponer otra fecha desde este flujo porque fecha y hora son definidas
  por el huesped y el operador.
- `Manutencao`: al crear o asignar una orden a un proveedor externo con WhatsApp
  habilitado, notificar el trabajo. El proveedor puede aceptar, rechazar o proponer
  fecha y franja horaria de visita.

Tambien se incorpora una consola exclusiva para `SUPERVISOR`, dentro de
`Configuracoes > WhatsApp`, para configurar, observar y limitar la integracion.

La primera entrega prepara el dominio, persistencia, contratos, seguridad,
controles de costo y UI. La conexion real con Meta se activa en una fase posterior.

## Resultado funcional esperado

```text
Operador guarda masaje u orden
          |
          v
Transaccion local confirmada + evento de notificacion en outbox
          |
          v
Politica decide: enviar / omitir / bloquear por presupuesto
          |
          v
Worker envia plantilla Utility por Cloud API
          |
          v
Meta -> webhook -> entrega, lectura o accion del proveedor
          |
          +--> Masaje: aceptar / rechazar
          |
          +--> Mantenimiento: aceptar / rechazar / proponer visita
                                      |
                                      v
                         operador confirma o rechaza propuesta
```

## Decisiones funcionales nuevas

1. No se enviara a todo registro con un telefono cualquiera. El proveedor debe ser
   externo, estar activo, tener `whatsappNumber` valido y `whatsappEnabled=true`.
2. `contact` continuara como dato libre de contacto. `whatsappNumber` sera un dato
   separado, normalizado en E.164 y almacenado sin espacios. La UI mostrara el
   prefijo internacional y validara Brasil inicialmente.
3. Se registrara consentimiento/base operativa mediante `whatsappOptInStatus`,
   `whatsappOptInAt` y `whatsappOptInSource`. No se enviara cuando el estado sea
   desconocido, revocado o bloqueado.
4. Crear o editar el agendamiento nunca esperara una respuesta HTTP de Meta.
5. Una edicion no genera automaticamente otro mensaje salvo que cambie informacion
   material: proveedor, fecha/hora, ubicacion, servicio o cancelacion.
6. Los cambios materiales se agruparan con una ventana corta de consolidacion para
   evitar varios avisos por correcciones consecutivas.
7. Toda accion de boton se procesa de forma idempotente y queda auditada.
8. Una aceptacion externa no puede sobrescribir una cancelacion o una edicion mas
   reciente. Cada solicitud lleva version del recurso y vence.
9. Las respuestas libres se almacenan para consulta, pero no cambian por si solas
   una orden. Solo acciones tipadas y validas ejecutan transiciones.
10. No se enviaran datos innecesarios del huesped. Para masajes se usara una
    referencia operativa minima; para mantenimiento, ubicacion, problema, prioridad
    y observaciones tecnicas necesarias.

## Mensajes y acciones

### Plantilla de masaje: `massage_booking_request_v1` (`UTILITY`)

Contenido minimo propuesto, en portugues:

```text
Nova solicitacao de massagem {{booking_reference}}
Data: {{date}}
Horario: {{time}}
Servico: {{treatment}}
Profissional: {{therapist}}
Local/referencia: {{guest_reference}}

Confirme sua disponibilidade abaixo.
```

Acciones:

- `Aceitar`
- `Nao posso`

No incluye propuesta de fecha. Si rechaza, el operador reasigna manualmente y el
sistema emite como maximo una nueva solicitud al nuevo proveedor.

### Plantilla de mantenimiento: `maintenance_work_request_v1` (`UTILITY`)

```text
Nova ordem de manutencao {{order_reference}}
Servico: {{service}}
Local: {{location}}
Prioridade: {{priority}}
Problema: {{short_description}}
Visita solicitada: {{requested_window_or_pending}}

Escolha uma opcao para responder.
```

Acciones:

- `Aceitar`
- `Propor visita`
- `Nao posso`

La opcion `Propor visita` debe abrir preferentemente un WhatsApp Flow con selector
de fecha, franja horaria y observacion opcional. Como alternativa de primera fase,
puede abrir una URL HTTPS publica de un solo uso. No se recomienda interpretar una
fecha escrita libremente como mecanismo principal.

### Confirmaciones posteriores

- El sistema no responde automaticamente con mensajes de cortesia.
- Solo envia una confirmacion cuando cambia una obligacion operativa: propuesta
  aceptada, propuesta rechazada con instruccion nueva o cancelacion.
- Las confirmaciones se agrupan y usan plantilla cuando la politica de Meta lo
  requiera.

## Estado de negocio propuesto

WhatsApp no debe reutilizar los estados operativos actuales como si fueran estados
de transporte. Se agregan estados separados.

### Solicitud externa

- `NOT_REQUIRED`
- `PENDING_NOTIFICATION`
- `AWAITING_PROVIDER`
- `ACCEPTED`
- `DECLINED`
- `VISIT_PROPOSED` (solo mantenimiento)
- `EXPIRED`
- `CANCELLED`

### Mensaje

- `QUEUED`
- `SUPPRESSED`
- `SENDING`
- `ACCEPTED_BY_META`
- `DELIVERED`
- `READ`
- `FAILED_RETRYABLE`
- `FAILED_FINAL`

`ACCEPTED_BY_META` significa que Meta acepto la solicitud, no que el proveedor
acepto el trabajo.

## Modelo de persistencia propuesto

### Cambios en proveedores

Agregar en `massage_providers` y `maintenance_providers`:

- `whatsapp_number`
- `whatsapp_enabled`
- `whatsapp_opt_in_status`
- `whatsapp_opt_in_at`
- `whatsapp_opt_in_source`

En mantenimiento el envio automatico exige ademas `provider_type = EXTERNAL`.
En masajes, todos los prestadores se consideran externos en el modelo actual; si
esto cambia, se debera explicitar `provider_type` antes de activar envios.

### Tablas transversales

- `whatsapp_settings`: interruptor general, zona horaria, moneda, limites y alertas.
- `whatsapp_templates`: nombre logico, nombre Meta, idioma, categoria y version.
- `whatsapp_notification_outbox`: evento de negocio, agregado, version, destino,
  plantilla, fecha de disponibilidad, intentos y clave idempotente unica.
- `whatsapp_messages`: direccion, `wamid`, estado, categoria, pais, timestamps,
  error sanitizado, costo estimado y costo conciliado.
- `whatsapp_message_events`: eventos webhook inmutables y deduplicados.
- `whatsapp_provider_requests`: solicitud, recurso asociado, estado, vencimiento y
  version esperada.
- `maintenance_visit_proposals`: orden, fecha/franja propuesta, observacion, estado,
  proveedor y timestamps.
- `whatsapp_monthly_usage`: agregado mensual para lectura rapida; la fuente de
  verdad siguen siendo los mensajes y eventos.
- `whatsapp_cost_rates`: tarifas importadas/configuradas con vigencia, mercado,
  categoria, moneda y fuente.

No se guardaran tokens en estas tablas. Los secretos viven exclusivamente en el
gestor de secretos o variables protegidas del ambiente.

## Idempotencia y reglas de emision

- Clave sugerida: `eventType + aggregateType + aggregateId + aggregateVersion + recipient`.
- El outbox se crea dentro de la misma transaccion que el masaje u orden.
- El worker reclama filas con bloqueo seguro y lease temporal.
- Reintentos solo para errores transitorios (`429`, timeout y `5xx`) con backoff y
  jitter; maximo recomendado: 5 intentos durante 24 horas.
- Errores de numero, plantilla, permiso u opt-in son finales hasta correccion.
- Un webhook repetido se ignora por identificador unico de evento/mensaje.
- Antes del envio se vuelve a validar que el recurso no este cancelado, que la
  version siga vigente y que el proveedor siga habilitado.
- En actualizaciones rapidas, conservar solo la ultima notificacion no enviada del
  mismo recurso y tipo.

## Control de volumen y costo

### Recomendacion

Usar tres niveles, todos configurables por supervisor:

1. `Alerta mensual`: 70% del presupuesto.
2. `Limite blando`: 85%; se avisa en el panel y se deshabilitan reenvios manuales
   no esenciales.
3. `Limite duro`: 100%; no se crean nuevos envios cobrables, salvo que un supervisor
   aumente el presupuesto. Los registros operativos se siguen guardando.

El control principal debe ser un presupuesto mensual en BRL porque el objetivo es
controlar costo. Como proteccion secundaria se agregan limites de mensajes mensual,
diario y por proveedor/dia. Valor inicial sugerido para pruebas: 100 mensajes/mes,
20/dia y 3 mensajes por proveedor/recurso en 24 horas. El presupuesto monetario se
define despues de cargar la tarifa real vigente para Brasil.

El sistema debe reservar costo estimado antes de enviar para evitar carreras entre
workers. Cuando Meta/Billing entregue informacion conciliable, se reemplaza la
estimacion por costo real. El panel diferenciara claramente:

- `Costo estimado`
- `Costo conciliado`
- `Sin tarifa disponible`

Nunca se mostrara como costo real una multiplicacion basada en una tarifa antigua.

### Reglas para minimizar mensajes

- una solicitud inicial por version material del servicio;
- sin recordatorio automatico en la primera entrega;
- reenvio manual exclusivo de supervisor y con motivo auditado;
- no enviar recibos de lectura ni respuestas de cortesia;
- deduplicar y consolidar actualizaciones;
- no responder automaticamente a texto libre;
- detener automatizaciones ante `STOP`, bloqueo u opt-out;
- expiracion sugerida: masaje 2 horas o antes del inicio; mantenimiento 48 horas,
  configurable;
- cancelar pendientes cuando el recurso se cancela o reasigna.

## Panel `Configuracoes > WhatsApp`

Acceso: solo `SUPERVISOR`. Los operadores ven el estado del aviso dentro de cada
agendamiento, pero no configuracion, costos globales ni reenvio forzado.

### Resumen mensual

- mensajes encolados, enviados, entregados, leidos y fallidos;
- solicitudes aceptadas, rechazadas, vencidas y pendientes;
- propuestas de visita pendientes;
- desglose por `Massagens` y `Manutencao`;
- desglose por categoria/tipo de mensaje;
- costo estimado y conciliado en BRL;
- presupuesto consumido y proyeccion simple al cierre del mes;
- estado de Cloud API, webhook y ultimo evento recibido.

### Controles

- activar/desactivar integracion (kill switch);
- modo `DRY_RUN`, que persiste y calcula pero no llama a Meta;
- presupuesto mensual y umbrales de alerta;
- limites mensual, diario y por proveedor;
- plantillas activas e idioma;
- politica de reintentos y expiracion dentro de rangos seguros;
- accion de prueba dirigida a un numero permitido en ambiente no productivo;
- cola de fallos con reintento autorizado y motivo.

Cambiar presupuesto, limites, plantillas o kill switch genera auditoria con valor
anterior, nuevo, usuario y fecha.

## Contratos API previstos

### Supervisor

- `GET /api/v1/settings/whatsapp`
- `PUT /api/v1/settings/whatsapp`
- `GET /api/v1/settings/whatsapp/usage?month=YYYY-MM`
- `GET /api/v1/settings/whatsapp/messages`
- `POST /api/v1/settings/whatsapp/messages/{messageId}/retry`
- `POST /api/v1/settings/whatsapp/test`

### Operacion autenticada

- `GET /api/v1/massages/bookings/{id}/external-request`
- `GET /api/v1/maintenance/orders/{id}/external-request`
- `POST /api/v1/maintenance/orders/{id}/visit-proposals/{proposalId}/accept`
- `POST /api/v1/maintenance/orders/{id}/visit-proposals/{proposalId}/reject`

### Meta/publicos

- `GET /api/v1/integrations/whatsapp/webhook` (challenge)
- `POST /api/v1/integrations/whatsapp/webhook` (firma obligatoria)
- `GET|POST /public/maintenance-visit/{singleUseToken}` solo si se usa el fallback
  web en lugar de WhatsApp Flows.

Los endpoints publicos usan rate limit, tokens opacos con expiracion, comparacion
constante donde corresponda y respuestas sin revelar si un recurso interno existe.

## Seguridad y privacidad ampliadas

- Validar `X-Hub-Signature-256` sobre los bytes crudos antes de parsear el webhook.
- Aceptar el challenge solo si coincide el verify token.
- Responder rapido a Meta y procesar asincronicamente.
- Cifrar o proteger numeros y contenido sensible en reposo segun capacidad del
  ambiente; siempre enmascararlos en UI y logs.
- Aplicar retencion: payload crudo corto y configurable; conservar metadata/auditoria
  necesaria sin duplicar datos personales indefinidamente.
- No incluir fotos ni adjuntos de mantenimiento en el primer mensaje. Si luego se
  habilitan, usar enlaces autenticados, de corta duracion y auditados.
- Sanitizar texto de operadores antes de interpolarlo en plantillas.
- Separar permisos `OPERATOR` y `SUPERVISOR` en backend, no solo en Flutter.

## Estrategia de implementacion

### Fase A - Preparacion local sin Meta

1. Migrar proveedores a campos WhatsApp explicitos y actualizar frontend.
2. Crear settings, outbox, mensajes, solicitudes y propuestas de visita.
3. Implementar politica de elegibilidad, deduplicacion, presupuesto y `DRY_RUN`.
4. Emitir eventos `AFTER_COMMIT`/outbox desde creacion y cambios materiales de
   masajes y mantenimiento.
5. Crear APIs y panel supervisor con datos simulados/reales locales.
6. Mostrar estado de notificacion en ambos modulos.

Criterio: se puede probar todo el ciclo hasta `QUEUED/SUPPRESSED` sin credenciales.

### Fase B - Recepcion Meta

1. Configuracion segura y health check.
2. Challenge GET, firma POST, persistencia idempotente y procesamiento asincrono.
3. Estados de entrega y lectura.
4. Acciones aceptar/rechazar con fixtures reales anonimizados.

### Fase C - Envio controlado

1. Cliente Cloud API y plantillas Utility aprobadas.
2. Worker, rate limit y reintentos.
3. Numero de prueba, allowlist y presupuesto minimo.
4. Prueba punta a punta de masaje.
5. Prueba punta a punta de mantenimiento.

### Fase D - Propuesta de visita

1. Implementar WhatsApp Flow o fallback web de un solo uso.
2. Validar fecha futura, zona `America/Sao_Paulo` y franja.
3. Crear propuesta sin cambiar aun la agenda.
4. Supervisor/operador acepta: actualizar agenda si la orden/version sigue valida.
5. Notificar confirmacion solo cuando sea necesario.

### Fase E - Produccion y operacion

1. Numero real, usuario de sistema, token protegido y facturacion.
2. Tarifas vigentes, presupuesto BRL y alertas verificadas.
3. Metricas, backup, retencion, runbook y kill switch probado.
4. Activacion gradual: mantenimiento allowlist, luego masajes, luego todos los
   proveedores habilitados.

## Pruebas obligatorias

- unitarias: elegibilidad, cambios materiales, plantillas, costo, limites y estados;
- persistencia: unicidad/idempotencia y reserva atomica de presupuesto;
- controller: permisos supervisor, challenge y firma invalida/valida;
- webhook: entrega, lectura, boton repetido, respuesta tardia y recurso cancelado;
- integracion: Meta caido no revierte masaje/orden; worker recupera tras reinicio;
- frontend: panel supervisor, estados, costo estimado vs conciliado y responsive;
- E2E: aceptar masaje, rechazar masaje, aceptar mantenimiento, proponer visita,
  aceptar/rechazar propuesta, limite duro y kill switch.

## Observaciones sobre precios de Meta

Meta migro a cobro por mensaje de plantilla entregado y las tarifas dependen de
categoria y mercado. Por eso no se fija un valor BRL en codigo ni en este plan. La
tarifa debe cargarse con fecha de vigencia y revisarse contra el rate card oficial
antes de cada salida a produccion. Las solicitudes descriptas deben redactarse para
categoria `UTILITY`; Meta conserva la decision final de clasificacion.

## Fuera de alcance inicial

- chatbot general o IA que ejecute ordenes desde texto libre;
- marketing, promociones o mensajes masivos;
- inbox completo para conversaciones humanas;
- envio de fotos/documentos por WhatsApp;
- confirmacion automatica de fechas ambiguas;
- soporte inicial para Tours u otros modulos.

## Repositorios oficiales

- Backend Spring Boot: `C:/Users/Public/Documents/Proyectos/quadras`
- Frontend Flutter: `C:/Users/Public/Documents/Proyectos/quedras-front`

## Objetivo

Integrar el sistema COSTANORTE con la API oficial WhatsApp Cloud API de Meta para:

- enviar notificaciones de negocio desde Spring Boot;
- recibir mensajes y acciones de usuarios y proveedores mediante webhooks;
- registrar intentos y estados de entrega;
- mostrar y operar la informacion desde el frontend Flutter;
- dejar una base extensible para interpretar solicitudes de negocio en lenguaje natural.

El ejemplo `Agendar Quadra Tenis 14/07/26` sirve como referencia futura y no forma
parte del alcance de la primera implementacion.

## Estado encontrado al iniciar

- El frontend ya intenta abrir WhatsApp o WhatsApp Web con un texto precargado.
- El campo generico `contact` del responsable se utiliza actualmente como numero.
- El backend no envia mensajes a Meta.
- El backend no tiene webhook de WhatsApp.
- No existe persistencia de conversaciones, mensajes ni estados de entrega.
- No existe auditoria backend del intento o resultado de la notificacion.
- Hay cambios locales previos en ambos repositorios que deben preservarse.

## Decisiones iniciales

1. La integracion con Meta vive exclusivamente en Spring Boot.
2. El token de Meta y el secreto de la aplicacion nunca se exponen en Flutter.
3. Guardar una orden no depende de que Meta o Internet esten disponibles.
4. El envio se desacopla mediante una bandeja persistente con reintentos.
5. Los webhooks de Meta usan endpoints publicos HTTPS separados de los endpoints
   autenticados con JWT para operadores.
6. Se validara la autenticidad de los webhooks antes de procesarlos.
7. Los numeros se almacenaran normalizados en formato internacional.
8. `contact` y `whatsappNumber` se separaran antes de activar envios automaticos.
9. La version de Graph API sera configurable y no quedara fija en el codigo.
10. Primero se usara el numero de prueba de Meta; el numero real se incorporara
    despues de revisar su situacion actual y los requisitos de coexistencia o migracion.

## Flujo objetivo para una orden

1. El operador guarda la orden.
2. La orden se confirma en la base local.
3. Se crea una notificacion pendiente relacionada con la orden y el proveedor.
4. Un proceso intenta enviar la plantilla aprobada por Meta.
5. Se almacena el identificador de mensaje devuelto por Meta.
6. Los webhooks actualizan los estados `SENT`, `DELIVERED`, `READ` o `FAILED`.
7. Las respuestas o botones del proveedor se almacenan y relacionan con la orden.
8. Flutter muestra el estado y las acciones disponibles al operador.

## Fases

### Paso 0 - Relevamiento y plan

Estado: Completado

- [x] Identificar backend y frontend oficiales.
- [x] Leer decisiones y protocolos vigentes.
- [x] Revisar la funcionalidad WhatsApp existente.
- [x] Identificar cambios locales que no deben sobrescribirse.
- [x] Registrar arquitectura, fases y condiciones de seguridad.

### Paso 1 - Preparacion de Meta

Estado: Pendiente

- [ ] Confirmar estado del portafolio comercial de Meta.
- [ ] Confirmar estado del numero real de WhatsApp.
- [ ] Crear o seleccionar una aplicacion Meta de tipo Business.
- [ ] Agregar el producto WhatsApp.
- [ ] Obtener el numero de prueba y credenciales temporales.
- [ ] Registrar los identificadores sin guardar secretos en Git.
- [ ] Realizar un envio manual controlado desde las herramientas de Meta.

Criterio de cierre:

- Meta entrega `Phone Number ID`, `WhatsApp Business Account ID` y token temporal.
- El numero receptor de prueba recibe el mensaje de prueba.
- Ningun secreto se incorpora al repositorio.

### Paso 2 - Configuracion segura en Spring Boot

Estado: Pendiente

- [ ] Crear propiedades tipadas `costanorte.whatsapp`.
- [ ] Agregar variables de entorno documentadas.
- [ ] Mantener la integracion desactivada por defecto.
- [ ] Validar configuracion solo cuando la integracion este activa.
- [ ] Agregar pruebas de propiedades y arranque sin WhatsApp.

### Paso 3 - Webhook minimo

Estado: Pendiente

- [ ] Implementar verificacion `GET` del webhook.
- [ ] Implementar recepcion `POST`.
- [ ] Excluir solo las rutas necesarias de autenticacion JWT.
- [ ] Validar firma de Meta.
- [ ] Responder rapidamente y procesar fuera del hilo HTTP.
- [ ] Cubrir verificacion, firma invalida y payload valido con pruebas.

### Paso 4 - Persistencia y bandeja de salida

Estado: Pendiente

- [ ] Crear migracion Flyway para conversaciones y mensajes.
- [ ] Crear migracion para notificaciones y reintentos.
- [ ] Modelar estados entrantes y salientes.
- [ ] Implementar idempotencia por identificador de Meta.
- [ ] Evitar guardar contenido sensible en logs.

### Paso 5 - Envio desde Spring Boot

Estado: Pendiente

- [ ] Implementar cliente de Graph API.
- [ ] Enviar texto dentro de la ventana de atencion permitida.
- [ ] Enviar plantillas aprobadas fuera de esa ventana.
- [ ] Persistir respuesta y errores de Meta.
- [ ] Implementar reintentos controlados.
- [ ] Probar primero con el numero de Meta.

### Paso 6 - Integracion con ordenes de mantenimiento

Estado: Pendiente

- [ ] Separar `whatsappNumber` de `contact`.
- [ ] Crear plantilla `UTILITY` para solicitar agendamiento.
- [ ] Crear notificacion al confirmar la transaccion de una orden.
- [ ] No bloquear la creacion de la orden si falla WhatsApp.
- [ ] Relacionar mensaje, proveedor y orden.
- [ ] Registrar auditoria funcional.

### Paso 7 - Consola Flutter

Estado: Pendiente

- [ ] Actualizar contrato backend/frontend.
- [ ] Mostrar estado de notificacion en la orden.
- [ ] Mostrar errores y permitir reintento autorizado.
- [ ] Mostrar respuestas del proveedor.
- [ ] Preservar la salida manual actual durante la transicion.
- [ ] Validar el flujo real punta a punta.

### Paso 8 - Numero real y produccion

Estado: Pendiente

- [ ] Revisar coexistencia o migracion del numero real.
- [ ] Verificar el negocio cuando Meta lo requiera.
- [ ] Crear usuario de sistema y token de produccion.
- [ ] Configurar facturacion.
- [ ] Publicar URL HTTPS estable.
- [ ] Activar plantillas aprobadas.
- [ ] Ejecutar pruebas controladas y plan de desactivacion.

### Paso 9 - Interpretacion de acciones

Estado: Futuro, fuera del alcance inicial

- [ ] Definir comandos soportados.
- [ ] Extraer intencion y datos sin ejecutar automaticamente.
- [ ] Solicitar confirmacion cuando haya ambiguedad.
- [ ] Aplicar autorizacion, idempotencia y auditoria.
- [ ] Ejecutar casos de negocio mediante servicios existentes.

## Variables previstas

```text
COSTANORTE_WHATSAPP_ENABLED
COSTANORTE_WHATSAPP_GRAPH_API_VERSION
COSTANORTE_WHATSAPP_PHONE_NUMBER_ID
COSTANORTE_WHATSAPP_BUSINESS_ACCOUNT_ID
COSTANORTE_WHATSAPP_ACCESS_TOKEN
COSTANORTE_WHATSAPP_VERIFY_TOKEN
COSTANORTE_META_APP_SECRET
```

Los valores reales son secretos de entorno. Este documento solo registra los nombres.

## Reglas de seguridad

- No guardar tokens, secretos ni payloads reales con datos personales en Git.
- No enviar secretos a Flutter.
- Verificar la firma de cada webhook.
- Aplicar idempotencia a eventos repetidos.
- Minimizar datos personales en logs.
- Auditar acciones de negocio sin registrar credenciales.
- Mantener el envio desactivable mediante configuracion.

## Registro de avance

### 2026-07-24 - Paso 0

- Se revisaron ambos repositorios y sus protocolos.
- Se confirmo que el comportamiento actual abre WhatsApp desde Flutter, pero no
  constituye una integracion API.
- Se decidio mantener la operacion local independiente de Meta.
- Se definieron fases pequeñas con criterio de cierre.
- Siguiente accion: completar el Paso 1 desde Meta con un numero de prueba.

### 2026-07-24 - Paso 1 iniciado

- El responsable confirma acceso a Meta Business Suite.
- Existe un numero brasileño exclusivo para la integracion, terminado en `7314`.
- El numero ya no necesita conservar operacion desde un telefono.
- Por seguridad, el numero completo, codigos de verificacion y credenciales no se
  registran en este repositorio.
- Siguiente control: verificar el portafolio comercial y el estado de la aplicacion
  en Meta antes de registrar el numero.

### 2026-07-24 - Restriccion detectada en Meta

- Meta informa una restriccion por integridad sobre el negocio `Hoteles Costa Norte`.
- La notificacion visible afecta anuncios y publicos; el alcance sobre activos de
  WhatsApp debe confirmarse dentro de Business Support Home.
- No se creara otro portafolio ni se registrara el numero mientras se revisa la
  restriccion, para evitar una posible interpretacion de evasion.
- El identificador completo del negocio no se registra por seguridad.
- Siguiente accion: asegurar la cuenta, revisar administradores y actividad, y
  solicitar revision desde el canal oficial de Meta.

### 2026-07-24 - Integracion previa localizada en MAXTIME

- Se localizo una implementacion Spring Boot previa en
  `C:/Users/Public/Documents/Proyectos/maxtime-v-0.01`.
- La implementacion incluye webhook, cliente Cloud API, mensajes interactivos,
  pruebas automatizadas y un playbook de pruebas reales.
- La aplicacion y sus activos de Meta pueden servir para una prueba tecnica
  controlada si continúan activos y el usuario tiene acceso administrativo.
- No se copiara codigo ni configuracion sin revisar compatibilidad y seguridad.
- Se detectaron valores predeterminados sensibles en archivos versionados del
  proyecto MAXTIME. Deben considerarse expuestos y rotarse antes de reutilizar
  cualquier credencial.
- La version configurada de Graph API es antigua y debera actualizarse a una
  version soportada despues de consultar el panel de Meta.
- Siguiente control: identificar en Meta el nombre y estado de la aplicacion,
  la cuenta de WhatsApp asociada y si existe un numero de prueba.
