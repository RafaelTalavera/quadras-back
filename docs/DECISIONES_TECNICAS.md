# DECISIONES TECNICAS - QUEDRAS

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
- Estado: Activa
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
