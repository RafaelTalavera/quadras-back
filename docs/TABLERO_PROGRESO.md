# TABLERO DE PROGRESO - QUEDRAS

## Estado general
- Proyecto: QUEDRAS
- Backend: En progreso
- Frontend: En progreso
- Ultimo hito trabajado: Hito 1 - Inicializacion y orden del proyecto
- Ultima actualizacion: 2026-03-12
- Riesgos abiertos: Inconsistencia de naming pendiente (`quedras` frontend vs `quadras` backend).
- Proximo paso recomendado: Iniciar Hito 2 (configuracion base backend Spring Boot + MySQL + estructura de capas).

## Hitos
| Hito | Nombre | Backend | Frontend | Estado general | Tests | Documentacion | Commit backend | Commit frontend | Observaciones |
|------|--------|---------|----------|----------------|-------|---------------|----------------|-----------------|---------------|
| 1 | Inicializacion y orden del proyecto | Completado | Completado | Completado | Backend OK (`mvnw test`), Frontend OK (`flutter test`) | Completada | Hecho (`6da5aa9`, `390a9e0`, `781af62`) | Hecho (`7d60e05`, `ea8e76b`, `8ecd571`) | Plan inicial, trazabilidad y validaciones base cerradas. |
| 2 | Configuracion base backend Spring Boot + MySQL + estructura de capas | Pendiente | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Base tecnica backend y perfiles locales. |
| 3 | Configuracion base frontend Flutter Desktop + estructura del cliente | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Pendiente | Base tecnica Flutter Desktop orientada a red local. |
| 4 | Modelo de dominio de reservas | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Definicion de entidades, DTOs y contratos base UI. |
| 5 | API backend de reservas | Pendiente | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Endpoints CRUD y validaciones basicas. |
| 6 | Pantallas base de agenda y creacion de reserva | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Pendiente | Vista de agenda y formulario inicial. |
| 7 | Validacion de solapamientos y reglas de negocio | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Reglas de horario, duracion y conflictos. |
| 8 | Edicion y cancelacion de reservas | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Operaciones de mantenimiento de reservas. |
| 9 | Conexion frontend-backend local | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Integracion HTTP local, manejo de errores y estados. |
| 10 | Validacion integral, documentacion final y preparacion para instalacion | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Checklist final para despliegue interno hotel. |

## Pendientes inmediatos
- Iniciar Hito 2: configuracion base backend Spring Boot + MySQL + capas.
- Definir convencion de ramas (ejemplo: `main` + ramas por hito).
- Definir criterio de naming entre proyectos (`quedras` / `quadras`).

## Bloqueos
- Ninguno activo.
- N/A.

## Decisiones activas
- Mantener `docs/TABLERO_PROGRESO.md` como unica fuente de verdad del proyecto.
- Ejecutar cada hito en flujo secuencial: backend completo primero, frontend despues.
