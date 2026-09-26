# DECISIONES TECNICAS - COSTANORTE

## DT-028 - Avisos WhatsApp manuales por Web o Windows
- Fecha: 2026-09-14
- Estado: Activa; sustituye DT-027
- Contexto: La Cloud API introduce costo y automatismos que la operación no desea asumir.
- Decisión: COSTANORTE solo prepara destinatario y texto. El operador elige Web o aplicación Windows y confirma el envío en WhatsApp. No se infieren estados de envío o entrega por abrir el enlace.
- Impacto: se retiran worker, webhook, credenciales y presupuestos API; se conservan números de proveedores. V32 permanece histórica hasta verificar cada base externa.

## DT-027 - WhatsApp Cloud API desacoplada de la operacion local
- Fecha: 2026-07-24
- Estado: Sustituida por DT-028 el 2026-09-14
- Contexto: El sistema necesita enviar notificaciones y recibir acciones de usuarios y prestadores por WhatsApp, pero la operacion local del hotel debe seguir funcionando sin Internet y el comportamiento actual solo abre WhatsApp desde Flutter sin confirmacion de envio ni recepcion.
- Decision: Integrar la API oficial WhatsApp Cloud API exclusivamente desde Spring Boot, mantener secretos fuera de Flutter y Git, y desacoplar los envios de las transacciones de negocio mediante una bandeja persistente. Una indisponibilidad de Meta no debe impedir guardar ordenes ni otras operaciones locales. Los webhooks publicos deben verificar autenticidad, aplicar idempotencia y registrar estados de mensaje.
- Impacto:
  - permite envio y recepcion interactiva con trazabilidad backend
  - conserva la arquitectura local-first definida por DT-001
  - evita exponer credenciales de Meta en el cliente Flutter
  - introduce persistencia, reintentos y operacion degradada como requisitos antes de activar notificaciones automaticas
  - obliga a tratar la implementacion como cambio cross-repo cuando se expongan estados o acciones en Flutter

## DT-026 - Manutencao con flujo operativo guiado por prestador y contrato minimo
- Fecha: 2026-07-10
- Estado: Activa
- Contexto: La operacion real del hotel lanza ordens de manutencao primero por prestador y no por una combinacion libre de campos. El formulario anterior obligaba a recorrer `Prestador`, `Tipo de ordem`, `Prioridade operacional` y `Referencia do hospede`, aumentando friccion y ruido sobre un flujo que en la practica casi siempre es correctivo.
- Decision: Simplificar el flujo de `Manutencao` para que los accesos principales nazcan desde prestadores fijos (`ELEVATORS`, `AIR_CONDITIONING`, `INTERNET`, `GENERAL_MAINTENANCE/INTERNAL`) y reducir el contrato operativo al minimo necesario. En consecuencia:
  - el frontend puede abrir la orden con prestador predefinido y ocultar ese campo
  - `orderKind` se fija como `CORRECTIVE` en el flujo guiado
  - se elimina `MaintenanceBusinessPriority` del dominio y de la persistencia
  - se elimina `guestReference` del dominio de mantenimiento y se conserva solo `guestName` para pedidos de huesped
- Impacto:
  - mejora la velocidad de carga de ordens para operacion diaria
  - reduce ramas de validacion y tamaño de DTOs/proyecciones
  - endurece el alineamiento entre UI y modelo de negocio real del hotel
  - deja el catalogo preparado para sumar nuevos prestadores fijos en el futuro sin reabrir el diseño del flujo base

## DT-018 - Auditoria transversal append-only por eventos de negocio
- Fecha: 2026-05-25
- Estado: Activa
- Contexto: El sistema necesita mostrar historial de cambios por modulo y entidad, indicando usuario, fecha/hora y detalle de campos modificados. Las columnas `updatedBy` y `updatedAt` no alcanzan para reconstruir el historial.
- Decision: Implementar una auditoria generica basada en eventos `append-only` en tabla `audit_events`, registrada desde la capa de servicio con snapshots `before/after`, diff por campo y actor resuelto desde `SecurityContext`.
- Impacto:
  - permite consultar historial detallado por entidad sin acoplar cada modulo a una estructura de auditoria distinta
  - mantiene control explicito sobre que acciones de negocio generan eventos y que datos se excluyen del snapshot
  - evita auditar blobs o payloads pesados; en adjuntos solo se persisten metadatos relevantes
  - facilita reutilizar una misma UI de timeline en frontend para todos los modulos

## DT-001 - Arquitectura local sin dependencia de internet
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: El sistema debe operar dentro de red local del hotel, incluso sin internet.
- Decision: Backend Spring Boot y MySQL se ejecutan localmente en infraestructura del hotel; cliente Flutter Desktop consume API local.
- Impacto: Se prioriza robustez en red interna y simplicidad operacional.

## DT-002 - Backend en Spring Boot con capas explicitas
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Se requiere mantenibilidad y trazabilidad del dominio de reservas.
- Decision: Organizar backend por capas (`controller`, `service`, `repository`, `domain`, `config`) y migraciones con Flyway.
- Impacto: Facilita pruebas, evolucion de reglas de negocio y control de cambios.

## DT-003 - Frontend en Flutter Desktop para operacion interna
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: El cliente operara como aplicacion de escritorio dentro del hotel.
- Decision: Usar Flutter Desktop (Windows como objetivo inicial) con estructura modular y cliente HTTP desacoplado.
- Impacto: Entrega una UI consistente y mantenible para operacion diaria.

## DT-004 - Sin autenticacion en etapa inicial
- Fecha: 2026-03-12
- Estado: Cerrada (superada por DT-017)
- Contexto: Alcance inicial excluye login/permisos.
- Decision: No implementar seguridad de usuarios en los primeros hitos.
- Impacto: Se reduce complejidad inicial y se acelera validacion de negocio principal.

## DT-005 - Flujo de trabajo secuencial por hito
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Se solicita control estricto de progreso y trazabilidad.
- Decision: Ejecutar cada hito con orden fijo: backend completo -> frontend completo -> tablero/documentacion.
- Impacto: Mejora control de alcance y reduce regresiones por cambios desordenados.

## DT-006 - Perfiles de ejecucion separados para runtime local y pruebas
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: El runtime del hotel usa MySQL local, pero las pruebas deben ejecutarse sin depender de infraestructura externa.
- Decision: Usar perfil `local` para MySQL + Flyway en runtime y perfil `test` con H2 en memoria para pruebas automatizadas.
- Impacto: Permite CI/desarrollo estable sin bloquear el avance por ausencia de MySQL en el entorno de pruebas.

## DT-007 - Shell frontend con rutas base y cliente HTTP desacoplado
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: El Hito 3 requiere una base de UI desktop mantenible y lista para integrar backend local sin acoplamiento temprano.
- Decision: Implementar `MaterialApp` con rutas nominales (`/`, `/agenda`, `/reservas/nueva`), shell responsive para desktop/mobile y capa `ApiClient` con implementacion `LocalHttpClient`.
- Impacto: Permite evolucionar cada modulo de UI por separado y facilita pruebas sustituyendo el cliente HTTP por dobles de prueba.

## DT-008 - Contrato base de reservas y estados de ciclo de vida
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: El Hito 4 requiere un modelo estable para persistencia backend y serializacion frontend antes de exponer API.
- Decision: Definir entidad `Reservation` con estados `SCHEDULED`, `COMPLETED` y `CANCELLED`, y DTOs base (`CreateReservationDto`, `ReservationDto`) como contrato de datos inicial.
- Impacto: Estandariza el dominio para los proximos hitos (API, agenda, validaciones de solapamiento) y reduce riesgo de cambios contractuales tardios.

## DT-009 - API de reservas v1 con alcance minimo y codigos HTTP consistentes
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Hito 5 requiere exponer operaciones base de consulta/alta sin introducir aun reglas avanzadas de negocio.
- Decision: Publicar `POST /api/v1/reservations`, `GET /api/v1/reservations` y `GET /api/v1/reservations/{id}` con respuestas `201/200/404/400`, dejando solapamientos para Hito 7.
- Impacto: Permite integracion temprana del cliente con una API estable y reduce acoplamiento prematuro a reglas aun no cerradas.

## DT-010 - Hito 6 con estado local en memoria para flujo UI base
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Hito 6 prioriza experiencia de agenda/alta y validaciones de formulario; la integracion HTTP completa tiene hito dedicado (Hito 9).
- Decision: Implementar `InMemoryReservationAppService` para soportar carga/error/exito locales en UI y mantener separacion via interfaz `ReservationAppService`.
- Impacto: La UI avanza sin bloquearse por red local y mantiene bajo riesgo la futura sustitucion por adaptador HTTP real en Hito 9.

## DT-011 - Reglas base de negocio para creacion de reservas
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Hito 7 exige evitar reservas invalidas o conflictivas antes de exponer operaciones de mantenimiento.
- Decision: Aplicar en backend validaciones de horario operativo (`07:00` a `23:00`), duraciones permitidas (`60/90/120` minutos) y bloqueo de solapamientos con respuesta `409 Conflict`.
- Impacto: Mejora integridad del calendario y establece contrato de errores para alineacion de mensajes en frontend.

## DT-012 - Reglas de mantenimiento para editar y cancelar reservas
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Hito 8 requiere editar/cancelar sin romper integridad del calendario ni estados del dominio.
- Decision: Exponer `PUT /api/v1/reservations/{id}` y `PATCH /api/v1/reservations/{id}/cancel`; permitir edicion solo en estado `SCHEDULED`, permitir cancelacion idempotente de `SCHEDULED/CANCELLED` y bloquear cancelacion de `COMPLETED`; en edicion se reaplican horario/duracion/solapamiento excluyendo la propia reserva.
- Impacto: Se habilita mantenimiento operativo de turnos con reglas consistentes y sin introducir cambios de seguridad o alcance extra.

## DT-013 - Integracion local por adaptador HTTP en cliente de reservas
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Hito 9 requiere conectar Flutter Desktop con backend local sin dependencia de internet y con manejo claro de errores de conectividad.
- Decision: Mantener API backend sin cambios de contrato y sustituir el servicio en memoria por un adaptador HTTP (`ReservationAppService`) que consume endpoints locales de reservas, propagando mensajes de error de API y fallos de red en formato entendible para UI.
- Impacto: El flujo operativo pasa a datos persistidos en MySQL via backend local y la UI queda preparada para escenarios de backend no disponible.

## DT-014 - Cierre de release con validacion reproducible y checklist de instalacion
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: Hito 10 exige preparar instalacion interna del hotel y dejar evidencia tecnica repetible de validaciones.
- Decision: Formalizar en documentacion los comandos de validacion (`mvnw test`, `mvnw -DskipTests package`, `flutter test`, `flutter analyze`, `flutter build windows`) y un checklist operativo con prerequisitos minimos de backend/frontend.
- Impacto: Reduce riesgo de despliegue, mejora trazabilidad de cierre y facilita reproduccion del proceso en nuevas estaciones de trabajo.

## DT-015 - Estandar de toolchain Windows para builds Flutter Desktop
- Fecha: 2026-03-12
- Estado: Activa
- Contexto: El bloqueo de Hito 10 se origino por instalacion incompleta de toolchain Visual Studio para compilar Windows release.
- Decision: Estandarizar instalacion de `Visual Studio Community 2022` con workload `Desktop development with C++` y verificar con `flutter doctor -v` antes de ejecutar `flutter build windows --release`.
- Impacto: Evita bloqueos de build por componentes faltantes y vuelve reproducible el cierre de release en estaciones nuevas.

## DT-016 - Renombre seguro de marca a COSTANORTE con compatibilidad temporal
- Fecha: 2026-03-14
- Estado: Activa
- Contexto: El cliente solicita renombrar el sistema de QUEDRAS a COSTANORTE y el entorno actual ya se encuentra instalado y validado.
- Decision: Aplicar renombre por fases; en fase 1 actualizar nombres visibles y de configuracion, manteniendo compatibilidad hacia atras con variables legacy (`QUADRAS_*`) y rutas internas actuales (`com.axioma.quadras` / carpeta repo) hasta fase de migracion profunda.
- Impacto: Permite cambiar identidad del producto sin riesgo de corte operativo ni necesidad de reinstalar toda la infraestructura en el mismo paso.

## DT-017 - Seguridad stateless con JWT firmado y rol persistido
- Fecha: 2026-03-14
- Estado: Activa
- Contexto: El Hito 12 requiere autenticar al usuario del cliente y autorizar operaciones sensibles del backend sin introducir sesion de servidor.
- Decision: Implementar Spring Security stateless con JWT firmado por HMAC, claim `role` obligatorio y validado contra el usuario persistido; mantener un rol inicial `OPERATOR`, usuario demo bootstrap y endpoint `POST /api/v1/auth/login` como contrato base para el frontend.
- Impacto: La API deja de ser anonima, el frontend puede operar con `Bearer` tokens y el sistema queda preparado para extender a multiples roles con cambios acotados en enum/migracion/autorizacion.

## DT-018 - Frontend comercial acotado a 3 modulos visibles y `pt-BR`
- Fecha: 2026-03-16
- Estado: Activa
- Contexto: El producto deja de presentarse como panel tecnico de reservas y pasa a una experiencia comercial/operativa del hotel con alcance visible mas acotado.
- Decision: Mantener el layout base del frontend, pero limitar la navegacion visible a `Massagens`, `Quadras`, `Tours e Viagens` y `Configuracoes`; retirar contenido tecnico visible al operador y normalizar la salida de UI a portugues de Brasil (`pt-BR`).
- Impacto: La experiencia queda alineada a la marca Costa Norte, `Quadras` conserva el flujo real ya integrado y backend pasa a tener como siguiente paso la definicion de contratos dedicados para `Massagens` y `Tours e Viagens`.

## DT-019 - Dominio backend dedicado para massagens con prestadores persistidos
- Fecha: 2026-03-19
- Estado: Activa
- Contexto: La operacion de massagens debe dejar de depender de una planilla Excel y de listas cargadas manualmente en UI para prestadores.
- Decision: Incorporar en backend el dominio `Massagens` con tablas y endpoints propios para `prestadores` y `agendamentos`, protegidos por JWT igual que `reservations`; validar que solo prestadores activos puedan ser elegidos y bloquear doble reserva del mismo prestador en la misma fecha/hora.
- Impacto: El frontend puede cargar el combo de prestadores desde API persistida y el hotel dispone de una base estable para evolucionar agenda, mantenimiento y reportes de massagens.

## DT-020 - Pago de massagens con captura completa en alta y registro posterior
- Fecha: 2026-03-20
- Estado: Activa
- Contexto: La operacion necesita mantener el cobro opcional durante el agendamiento, pero tambien registrar pagos despues de que el masaje ya fue agendado, sin volver a usar planillas externas.
- Decision: Extender `MassageBooking` con `paymentMethod`, `paymentDate` y `paymentNotes`; mantener `paid` en el alta y agregar un flujo independiente `PATCH /api/v1/massages/bookings/{id}/payment` para registrar o corregir el pago posteriormente. La busqueda operativa de masajes se resuelve sobre el mismo endpoint `GET /api/v1/massages/bookings` con filtros por fecha, cliente, referencia, prestador y estado de pago.
- Impacto: El operador puede cobrar al crear el turno o mas tarde desde una pantalla dedicada, con trazabilidad basica de medio de pago (`CARD`, `CASH`, `PIX`), fecha y observaciones sin duplicar registros.

## DT-021 - El agente debe operar solo sobre el entorno oficialmente documentado
- Fecha: 2026-03-20
- Estado: Activa
- Contexto: Se detecto un desvio de implementacion hacia un frontend web embebido en este repositorio, cuando la arquitectura oficial del proyecto ya define otro frontend y otro limite de trabajo.
- Decision: El backend oficial del proyecto es este repositorio `quadras` en Spring Boot. El frontend oficial del proyecto es el repositorio separado `C:/Users/Public/Documents/Proyectos/quedras-front` en Flutter Desktop. El agente debe trabajar exclusivamente sobre los repositorios, stacks y componentes respaldados por la documentacion vigente del proyecto. La presencia de archivos auxiliares, experimentales o historicos dentro del workspace no redefine la arquitectura oficial. Si existe ambiguedad entre lo hallado en disco y lo documentado, el agente debe detenerse, verificar la documentacion y pedir confirmacion antes de implementar fuera del entorno definido.
- Impacto: Reduce riesgo de cambios en componentes no oficiales, evita desalineacion entre backend/frontend y mantiene la trazabilidad tecnica del proyecto bajo una unica fuente de verdad documental.

## DT-022 - Massagens con cancelacion auditable y sin borrado fisico
- Fecha: 2026-03-20
- Estado: Activa
- Contexto: El frontend oficial ya necesita editar y cancelar atendimientos de massagens sin eliminar registros, con observacion obligatoria y trazabilidad por usuario autenticado.
- Decision: Extender `MassageBooking` con `status`, `cancellationNotes`, `cancelledAt`, `createdBy`, `updatedBy` y `cancelledBy`; exponer `PUT /api/v1/massages/bookings/{id}` y `PATCH /api/v1/massages/bookings/{id}/cancel`; exigir observacion en cancelacion y tomar el usuario desde el JWT autenticado. El bloqueo de conflictos pasa a aplicarse solo sobre bookings `SCHEDULED`, permitiendo reutilizar un horario despues de cancelarlo.
- Impacto: El backend de massagens deja de depender de borrados o reescrituras implícitas, gana trazabilidad operativa completa y queda alineado al frontend Flutter para mantenimiento de agenda.

## DT-023 - Las migraciones de massagens deben preservar soporte de FK antes de soltar indices unicos
- Fecha: 2026-03-20
- Estado: Activa
- Contexto: La migracion `V6__extend_massage_bookings_with_status_and_audit.sql` fallo en MySQL local porque intentaba eliminar `uk_massage_bookings_provider_slot` antes de crear un indice alternativo compatible con la FK por `provider_id`, dejando la version 6 en estado fallido dentro de Flyway.
- Decision: En migraciones que reemplacen un indice unico usado indirectamente por una FK, crear primero un indice alternativo con el mismo prefijo requerido por MySQL y recien despues eliminar el indice original. Para `massage_bookings`, `idx_massage_bookings_provider_slot` debe existir antes de soltar `uk_massage_bookings_provider_slot`.
- Impacto: Se evita que Flyway deje la base en estado parcial por orden incorrecto de DDL, mejora la reproducibilidad del arranque local y reduce intervenciones manuales sobre `flyway_schema_history`.

## DT-024 - Despliegue en Railway con perfil backend liviano y sin componentes de demo
- Fecha: 2026-05-13
- Estado: Activa
- Contexto: El costo a optimizar para despliegue productivo recae sobre el backend Java ejecutado en Railway; la base de datos se desplegara fuera de Railway y no forma parte directa del presupuesto de RAM del servicio Java.
- Decision: Mantener el backend como monolito funcional completo, pero introducir un perfil `railway` que desactive componentes no productivos (`demo user`, simulacion de mantencion) y fijar una JVM baseline de operacion con `-Xms256m -Xmx512m`. Adicionalmente, retirar infraestructura no usada en runtime (`Actuator`) y dependencias redundantes del classpath cuando no aporten funcionalidad visible al usuario.
- Impacto: Se reduce el costo fijo de RAM y el tiempo de arranque sin apagar modulos de negocio; el despliegue queda mejor alineado a un presupuesto de memoria de Railway y mantiene intactos los endpoints funcionales requeridos por operacion.

## DT-025 - Railway sobre MariaDB debe arrancar con repair de Flyway y dialecto explicito
- Fecha: 2026-06-02
- Estado: Activa
- Contexto: En el deploy productivo sobre Railway + Hostinger MariaDB 11.8, el backend logro conectar a la base pero fallo en dos puntos distintos del arranque. Primero, Flyway Community se negaba a migrar porque existia una entrada fallida previa de `V23` en `flyway_schema_history`. Despues de corregir eso, Hibernate 7.2.4 fallo al autodetectar el dialecto consultando metadata de MariaDB que no coincide con lo esperado por esa version.
- Decision: Ejecutar `flyway.repair()` antes de `flyway.migrate()` mediante una `FlywayMigrationStrategy` propia y fijar en el perfil `railway` el dialecto `org.hibernate.dialect.MariaDBDialect` en lugar de depender de autodeteccion.
- Impacto:
  - el backend puede recuperarse automaticamente de estados fallidos previos en `flyway_schema_history` durante el arranque en Railway
  - se evita la caida de Hibernate por introspeccion de metadata en MariaDB 11.8
  - el deploy queda mas deterministico para Hostinger/Railway, a costa de asumir MariaDB explicita en ese perfil
