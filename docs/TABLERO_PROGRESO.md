# TABLERO DE PROGRESO - QUEDRAS

## Estado general
- Proyecto: QUEDRAS
- Backend: En progreso
- Frontend: En progreso
- Ultimo hito trabajado: Hito 8 - Edicion y cancelacion de reservas
- Ultima actualizacion: 2026-03-12
- Riesgos abiertos: Inconsistencia de naming entre proyectos (`quedras` vs `quadras`).
- Proximo paso recomendado: Iniciar Hito 9 (conexion frontend-backend local).

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
| 8 | Edicion y cancelacion de reservas | Completado | Completado | Completado | Backend OK (`mvnw test`, 22 tests), Frontend OK (`flutter test`, `flutter analyze`) | Completada | Hecho (`37f2a1b`) | Hecho (`a1008d3`) | API y UI permiten editar/cancelar reservas con reglas de estado y validaciones de integridad. |
| 9 | Conexion frontend-backend local | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Integracion HTTP local, manejo de errores y estados. |
| 10 | Validacion integral, documentacion final y preparacion para instalacion | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Checklist final para despliegue interno hotel. |

## Pendientes inmediatos
- Iniciar Hito 9: integrar Flutter Desktop con backend local por HTTP.
- Definir estrategia de manejo cuando el backend local no este disponible.
- Definir convencion de ramas (ejemplo: `main` + ramas por hito).
- Definir criterio final de naming entre proyectos (`quedras` / `quadras`).

## Bloqueos
- Ninguno activo.
- N/A.

## Decisiones activas
- Mantener `docs/TABLERO_PROGRESO.md` como unica fuente de verdad del proyecto.
- Ejecutar cada hito en flujo secuencial: backend completo primero, frontend despues.
