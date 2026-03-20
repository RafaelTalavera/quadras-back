# DECISIONES TECNICAS - COSTANORTE

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
