# TABLERO DE PROGRESO - QUEDRAS

## Estado general
- Proyecto: QUEDRAS
- Backend: Completado (fase Hito 10)
- Frontend: Completado (fase Hito 10)
- Ultimo hito trabajado: Hito 10 - Validacion integral, documentacion final y preparacion para instalacion
- Ultima actualizacion: 2026-03-12
- Riesgos abiertos: Inconsistencia de naming entre proyectos (`quedras` vs `quadras`).
- Proximo paso recomendado: Ejecutar instalacion piloto en puesto operativo del hotel con backend local y validar flujo E2E.

## Hitos
| Hito | Nombre | Backend | Frontend | Estado general | Tests | Documentacion | Commit backend | Commit frontend | Observaciones |
|------|--------|---------|----------|----------------|-------|---------------|----------------|-----------------|---------------|
| 1 | Inicializacion y orden del proyecto | Completado | Completado | Completado | Backend OK (`mvnw test`), Frontend OK (`flutter test`) | Completada | Hecho (`6da5aa9`, `390a9e0`, `781af62`) | Hecho (`7d60e05`, `ea8e76b`, `8ecd571`, `28a9d0e`) | Plan inicial, trazabilidad y validaciones base cerradas. |
| 2 | Configuracion base backend Spring Boot + MySQL + estructura de capas | Completado | N/A | Completado | Backend OK en `mvnw test`, `mvnw -DskipTests package` y `spring-boot:run` local | Completada | Hecho (`6e6a46d`, `f1a124f`, `8d08cb8`) | Hecho documental (`bf91833`, `d3ccad1`, `0f6b3c2`) | Conectividad validada con `db_quadras` (`root/sasa`). |
| 3 | Configuracion base frontend Flutter Desktop + estructura del cliente | N/A (fase backend ejecutada) | Completado | Completado | Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`a755ef1`, `f889753`) | Hecho (`4cad6b8`) | Shell desktop operativo, rutas base y cliente HTTP desacoplado listos para integracion local. |
| 4 | Modelo de dominio de reservas | Completado | Completado | Completado | Backend OK (`mvnw test`, incluye `FlywayReservationMigrationTest`), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`6937aef`, `03d9223`) | Hecho (`eb642e4`) | Contrato de `Reservation` alineado entre backend y frontend con serializacion validada. |
| 5 | API backend de reservas | Completado | N/A (sin cambios funcionales) | Completado | Backend OK (`mvnw test`, 12 tests), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`333bb3d`, `9b4083c`) | Hecho documental (`4486f2e`) | Endpoints `POST/GET/GET{id}` operativos con respuestas 201/200/404/400. |
| 6 | Pantallas base de agenda y creacion de reserva | N/A (fase backend ejecutada) | Completado | Completado | Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`b51f932`, `63004fa`) | Hecho (`968fe87`) | Agenda diaria y alta operativa con validaciones y estados locales en memoria. |
| 7 | Validacion de solapamientos y reglas de negocio | Completado | Completado | Completado | Backend OK (`mvnw test`, 15 tests), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`7d6fb2e`, `d60aef0`) | Hecho (`26dab3b`) | Reglas de horario (07:00-23:00), duracion (60/90/120) y solapamientos aplicadas y alineadas entre API/UI. |
| 8 | Edicion y cancelacion de reservas | Completado | Completado | Completado | Backend OK (`mvnw test`, 22 tests), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`37f2a1b`, `2352674`) | Hecho (`a1008d3`) | API y UI permiten editar/cancelar reservas con reglas de estado y validaciones de integridad. |
| 9 | Conexion frontend-backend local | Completado | Completado | Completado | Backend OK (`mvnw test`, 22 tests), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`dde3646`, `c71d87a`) | Hecho (`55f7234`) | Cliente Flutter conectado a API local de reservas con operaciones CRUD y manejo de errores de red/API. |
| 10 | Validacion integral, documentacion final y preparacion para instalacion | Completado | Completado | Completado | Backend OK (`mvnw test`, `mvnw -DskipTests package`, smoke HTTP local), Frontend OK (`flutter test`, `flutter analyze`, `flutter doctor -v`, `flutter build windows --release`) | Completada | Hecho (`1d6284e`, `a27b60c`, `93d7b6b`, `16153f2`) | Hecho (`f3a5963`, `fc866ae`) | Build Windows release generado y validado sin bloqueos. |

## Pendientes inmediatos
- Ejecutar instalacion piloto de backend/frontend en equipo objetivo del hotel.
- Validar conectividad real en red local hotel (IP fija y puerto de backend).
- Definir convencion de ramas (ejemplo: `main` + ramas por hito).
- Definir criterio final de naming entre proyectos (`quedras` / `quadras`).

## Bloqueos
- Sin bloqueos abiertos para el cierre del Hito 10.

## Decisiones activas
- Mantener `docs/TABLERO_PROGRESO.md` como unica fuente de verdad del proyecto.
- Ejecutar cada hito en flujo secuencial: backend completo primero, frontend despues.
- Estandarizar toolchain de build Windows con Visual Studio 2022 + workload C++ desktop para garantizar builds reproducibles de Flutter.
