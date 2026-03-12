# TABLERO DE PROGRESO - QUEDRAS

## Estado general
- Proyecto: QUEDRAS
- Backend: Bloqueado
- Frontend: En progreso
- Ultimo hito trabajado: Hito 2 - Configuracion base backend Spring Boot + MySQL + estructura de capas
- Ultima actualizacion: 2026-03-12
- Riesgos abiertos: Credenciales MySQL de entorno local no disponibles/validas para app (`Access denied`).
- Proximo paso recomendado: Definir credenciales MySQL validas (o crear usuario `quadras`) y repetir arranque `local`.

## Hitos
| Hito | Nombre | Backend | Frontend | Estado general | Tests | Documentacion | Commit backend | Commit frontend | Observaciones |
|------|--------|---------|----------|----------------|-------|---------------|----------------|-----------------|---------------|
| 1 | Inicializacion y orden del proyecto | Completado | Completado | Completado | Backend OK (`mvnw test`), Frontend OK (`flutter test`) | Completada | Hecho (`6da5aa9`, `390a9e0`, `781af62`) | Hecho (`7d60e05`, `ea8e76b`, `8ecd571`, `28a9d0e`) | Plan inicial, trazabilidad y validaciones base cerradas. |
| 2 | Configuracion base backend Spring Boot + MySQL + estructura de capas | Bloqueado | N/A | Bloqueado | Backend OK en test/build, FAIL en `spring-boot:run` local (Access denied MySQL) | En progreso | Hecho parcial (`6e6a46d`, `f1a124f`) | Hecho documental (`bf91833`, `d3ccad1`) | Base tecnica implementada; bloqueo por credenciales de BD local. |
| 3 | Configuracion base frontend Flutter Desktop + estructura del cliente | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Pendiente | Base tecnica Flutter Desktop orientada a red local. |
| 4 | Modelo de dominio de reservas | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Definicion de entidades, DTOs y contratos base UI. |
| 5 | API backend de reservas | Pendiente | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Endpoints CRUD y validaciones basicas. |
| 6 | Pantallas base de agenda y creacion de reserva | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Pendiente | Vista de agenda y formulario inicial. |
| 7 | Validacion de solapamientos y reglas de negocio | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Reglas de horario, duracion y conflictos. |
| 8 | Edicion y cancelacion de reservas | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Operaciones de mantenimiento de reservas. |
| 9 | Conexion frontend-backend local | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Integracion HTTP local, manejo de errores y estados. |
| 10 | Validacion integral, documentacion final y preparacion para instalacion | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Checklist final para despliegue interno hotel. |

## Pendientes inmediatos
- Definir credenciales MySQL validas para backend (`QUADRAS_DB_USER`/`QUADRAS_DB_PASSWORD`).
- Repetir arranque `spring-boot:run` con perfil `local` y confirmar migraciones Flyway.
- Definir convencion de ramas (ejemplo: `main` + ramas por hito).
- Definir criterio de naming entre proyectos (`quedras` / `quadras`) en ventana posterior al cierre de Hito 2.

## Bloqueos
- `spring-boot:run` con perfil `local` falla: `Access denied for user 'quadras'@'localhost'`.
- `spring-boot:run` con `root` tambien falla: `Access denied for user 'root'@'localhost'`.

## Decisiones activas
- Mantener `docs/TABLERO_PROGRESO.md` como unica fuente de verdad del proyecto.
- Ejecutar cada hito en flujo secuencial: backend completo primero, frontend despues.
