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
