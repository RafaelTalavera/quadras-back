# TABLERO DE PROGRESO - QUEDRAS

## Estado general
- Proyecto: QUEDRAS
- Backend: En progreso
- Frontend: En progreso
- Ultimo hito trabajado: Hito 4 - Modelo de dominio de reservas
- Ultima actualizacion: 2026-03-12
- Riesgos abiertos: Inconsistencia de naming entre proyectos (`quedras` vs `quadras`).
- Proximo paso recomendado: Iniciar Hito 5 (API backend de reservas) con endpoints CRUD iniciales.

## Hitos
| Hito | Nombre | Backend | Frontend | Estado general | Tests | Documentacion | Commit backend | Commit frontend | Observaciones |
|------|--------|---------|----------|----------------|-------|---------------|----------------|-----------------|---------------|
| 1 | Inicializacion y orden del proyecto | Completado | Completado | Completado | Backend OK (`mvnw test`), Frontend OK (`flutter test`) | Completada | Hecho (`6da5aa9`, `390a9e0`, `781af62`) | Hecho (`7d60e05`, `ea8e76b`, `8ecd571`, `28a9d0e`) | Plan inicial, trazabilidad y validaciones base cerradas. |
| 2 | Configuracion base backend Spring Boot + MySQL + estructura de capas | Completado | N/A | Completado | Backend OK en `mvnw test`, `mvnw -DskipTests package` y `spring-boot:run` local | Completada | Hecho (`6e6a46d`, `f1a124f`, `8d08cb8`) | Hecho documental (`bf91833`, `d3ccad1`, `0f6b3c2`) | Conectividad validada con `db_quadras` (`root/sasa`). |
| 3 | Configuracion base frontend Flutter Desktop + estructura del cliente | N/A (fase backend ejecutada) | Completado | Completado | Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`a755ef1`, `f889753`) | Hecho (`4cad6b8`) | Shell desktop operativo, rutas base y cliente HTTP desacoplado listos para integracion local. |
| 4 | Modelo de dominio de reservas | Completado | Completado | Completado | Backend OK (`mvnw test`, incluye `FlywayReservationMigrationTest`), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`6937aef`, `03d9223`) | Hecho (`eb642e4`) | Contrato de `Reservation` alineado entre backend y frontend con serializacion validada. |
| 5 | API backend de reservas | Pendiente | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Endpoints CRUD y validaciones basicas. |
| 6 | Pantallas base de agenda y creacion de reserva | N/A | Pendiente | Pendiente | Pendiente | Pendiente | N/A | Pendiente | Vista de agenda y formulario inicial. |
| 7 | Validacion de solapamientos y reglas de negocio | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Reglas de horario, duracion y conflictos. |
| 8 | Edicion y cancelacion de reservas | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Operaciones de mantenimiento de reservas. |
| 9 | Conexion frontend-backend local | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Integracion HTTP local, manejo de errores y estados. |
| 10 | Validacion integral, documentacion final y preparacion para instalacion | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Checklist final para despliegue interno hotel. |

## Pendientes inmediatos
- Iniciar Hito 5 en backend con `POST/GET` de reservas sobre el modelo de Hito 4.
- Definir codigos HTTP y validaciones de entrada para DTOs de reserva.
- Definir convencion de ramas (ejemplo: `main` + ramas por hito).
- Definir criterio final de naming entre proyectos (`quedras` / `quadras`).

## Bloqueos
- Ninguno activo.
- N/A.

## Decisiones activas
- Mantener `docs/TABLERO_PROGRESO.md` como unica fuente de verdad del proyecto.
- Ejecutar cada hito en flujo secuencial: backend completo primero, frontend despues.
