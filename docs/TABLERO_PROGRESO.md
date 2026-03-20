# TABLERO DE PROGRESO - COSTANORTE

## Estado general
- Proyecto: COSTANORTE
- Backend: Completado (Hito 12 backend, sin cambios funcionales nuevos en esta fase)
- Frontend: En progreso (fase posterior a Hito 12)
- Ultimo hito trabajado: Fase posterior a Hito 12 - Reenfoque comercial del frontend
- Ultima actualizacion: 2026-03-16
- Riesgos abiertos: Definir contratos backend para `Massagens` y `Tours e Viagens`; mantener consistencia `pt-BR` extremo a extremo entre UI y API; definir estrategia de persistencia/renovacion de sesion si el producto evoluciona mas alla del uso local; pendiente fase 2 para migrar nombres internos legacy (`com.axioma.quadras`, rutas de repositorio).
- Proximo paso recomendado: Definir el siguiente paquete funcional para `Massagens` y `Tours e Viagens`, y cerrar la normalizacion de mensajes `pt-BR` en backend donde la UI consuma errores directamente.

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
| 11 | Renombre seguro de QUEDRAS a COSTANORTE (fase 1) | Completado | Completado | Completado | Backend OK (`mvnw test`, `mvnw -DskipTests package`, `backend_smoke_local.ps1`), Frontend OK (`flutter pub get`, `flutter test`, `flutter analyze`, `flutter build windows --release`) | Completada | Hecho (`98d62f3`) | Hecho (`b035d21`) | Marca actualizada a COSTANORTE con compatibilidad temporal para configuraciones legacy. |
| 12 | Seguridad de usuarios con JWT y rol inicial | Completado | Completado | Completado | Backend OK (`mvnw test`, `mvnw -DskipTests package`, `backend_smoke_local.ps1 -SkipBuild` con login JWT); Frontend OK (`flutter test`, `flutter analyze`, `flutter build windows --release`) | Completada | Hecho (`557c88e`) | Hecho (`3393a5e`) | JWT operativo extremo a extremo con login frontend, sesion en memoria, logout, guard de rutas y consumo autenticado de reservas. |

## Actualizacion post Hito 12
- Fecha: 2026-03-16
- Commit frontend asociado: `384d38b`
- Frontend reenfocado al alcance visible `Massagens`, `Quadras`, `Tours e Viagens` y `Configuracoes`, manteniendo el layout base ya validado.
- `Quadras` conserva integracion real con backend; `Massagens` y `Tours e Viagens` quedan en estado frontend controlado hasta definir contrato dedicado.
- La UI visible se normaliza a portugues de Brasil y se elimina contenido tecnico expuesto al operador.

## Pendientes inmediatos
- Definir contratos backend para `Tours e Viagens`.
- Definir persistencia o renovacion de sesion si el flujo deja de ser estrictamente local.
- Planificar fase 2 de renombre interno (`package`, rutas repo, nombres legacy en historial tecnico).
- Definir convencion de ramas (ejemplo: `main` + ramas por hito).

## Bloqueos
- Sin bloqueos tecnicos abiertos; hay dependencias funcionales pendientes para los nuevos modulos comerciales.

## Decisiones activas
- Mantener `docs/TABLERO_PROGRESO.md` como unica fuente de verdad del proyecto.
- Ejecutar cada hito en flujo secuencial: backend completo primero, frontend despues.
- Estandarizar toolchain de build Windows con Visual Studio 2022 + workload C++ desktop para garantizar builds reproducibles de Flutter.
- Ejecutar renombre por fases: identidad y configuracion primero, migracion interna profunda despues.
- Mantener JWT stateless con rol embebido en el token y validado contra usuario persistido para sostener evolucion futura a multiples roles.
- Mantener el frontend con navegacion visible acotada a modulos comerciales y salida `pt-BR`.
- Regla operativa: cualquier implementacion del agente debe respetar el entorno oficialmente documentado. Backend oficial: este repo `quadras`. Frontend oficial: `C:/Users/Public/Documents/Proyectos/quedras-front`. No intervenir fuera de ese alcance sin confirmacion explicita del usuario y trazabilidad en documentacion.
