# CHANGELOG DE DESARROLLO - COSTANORTE

## 2026-03-12 | Hito 1 | Inicializacion y orden del proyecto
- Componente afectado: Backend (gestion documental y control de proyecto)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Establecer base de control, plan de hitos y decisiones tecnicas iniciales.
- Impacto funcional: Sin cambios funcionales en API ni logica de negocio.

## 2026-03-12 | Hito 1 | Inicializacion de control de versiones
- Componente afectado: Backend (infraestructura de desarrollo)
- Archivos tocados:
  - `.git/` (repositorio inicializado)
- Motivo del cambio: Habilitar commits por hito segun metodologia solicitada.
- Impacto funcional: Sin impacto funcional en ejecucion de backend.

## 2026-03-12 | Hito 1 | Validacion de backend (smoke tests)
- Componente afectado: Backend (calidad y validacion tecnica)
- Archivos tocados:
  - `target/surefire-reports/com.axioma.quadras.QuadrasApplicationTests.txt` (generado, no versionado)
- Motivo del cambio: Ejecutar `mvnw test` para validar estabilidad base sin cambios funcionales.
- Impacto funcional: Se detecta bloqueo; `contextLoads` falla por falta de datasource/perfil de test (`Failed to determine a suitable driver class`).

## 2026-03-12 | Hito 1 | Commit backend de inicializacion
- Componente afectado: Backend (codigo base + documentacion)
- Archivos tocados:
  - Estructura base Spring Boot (`pom.xml`, `src/`, `mvnw*`, `.mvn/`)
  - Documentacion de control en `docs/`
- Motivo del cambio: Registrar baseline backend y documentos de gestion de hitos en control de versiones.
- Impacto funcional: Sin cambios funcionales nuevos; se mantiene bloqueo de test por datasource sin configurar.

## 2026-03-12 | Hito 1 | Actualizacion de tablero con commits y estado final
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Reflejar hashes de commits de backend/frontend y estado consolidado del Hito 1.
- Impacto funcional: Sin impacto funcional en API.

## 2026-03-12 | Hito 1 | Desbloqueo de test de contexto en backend
- Componente afectado: Backend (infraestructura de test)
- Archivos tocados:
  - `pom.xml`
  - `src/test/java/com/axioma/quadras/QuadrasApplicationTests.java`
  - `src/test/resources/application-test.properties`
- Motivo del cambio: Configurar entorno de pruebas con perfil `test` y datasource H2 para levantar el contexto Spring sin MySQL local.
- Impacto funcional: Sin cambios en logica de negocio ni en runtime productivo; mejora de estabilidad de pruebas.

## 2026-03-12 | Hito 1 | Revalidacion backend posterior al ajuste de test
- Componente afectado: Backend (calidad)
- Archivos tocados:
  - `target/surefire-reports/` (generado, no versionado)
- Motivo del cambio: Confirmar que `mvnw test` pasa luego del ajuste de infraestructura de pruebas.
- Impacto funcional: Tests de backend en estado OK.

## 2026-03-12 | Hito 1 | Cierre de tablero y estado del hito
- Componente afectado: Backend (control de proyecto)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/HITOS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar Hito 1 como completado en backend y frontend.
- Impacto funcional: Sin cambios funcionales en API.

## 2026-03-12 | Hito 1 | Ajuste final de trazabilidad de commits frontend en tablero
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar ultimo commit documental de frontend en la fila del Hito 1.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 2 | Configuracion base de perfiles y datasource backend
- Componente afectado: Backend (configuracion tecnica)
- Archivos tocados:
  - `src/main/resources/application.properties`
  - `src/main/resources/application-local.properties`
  - `src/main/resources/db/migration/V1__init_technical_baseline.sql`
- Motivo del cambio: Definir perfil `local` con MySQL/Flyway y baseline inicial de migraciones para entorno local del hotel.
- Impacto funcional: Se habilita configuracion base de persistencia para los siguientes hitos.

## 2026-03-12 | Hito 2 | Estructura de capas y endpoint tecnico de salud
- Componente afectado: Backend (arquitectura interna)
- Archivos tocados:
  - `src/main/java/com/axioma/quadras/config/*`
  - `src/main/java/com/axioma/quadras/controller/*`
  - `src/main/java/com/axioma/quadras/service/*`
  - `src/main/java/com/axioma/quadras/domain/*`
  - `src/main/java/com/axioma/quadras/repository/package-info.java`
- Motivo del cambio: Crear capas base del backend y endpoint `GET /api/v1/system/health` para verificacion tecnica.
- Impacto funcional: API expone endpoint de estado y manejo global de errores base.

## 2026-03-12 | Hito 2 | Refuerzo de pruebas backend
- Componente afectado: Backend (testing)
- Archivos tocados:
  - `pom.xml`
  - `src/test/java/com/axioma/quadras/controller/SystemStatusControllerTest.java`
- Motivo del cambio: Incorporar pruebas de endpoint tecnico y dependencia de pruebas general (`spring-boot-starter-test`).
- Impacto funcional: Se amplian pruebas automatizadas sin afectar logica productiva.

## 2026-03-12 | Hito 2 | Validacion tecnica del backend
- Componente afectado: Backend (calidad)
- Archivos tocados:
  - `target/surefire-reports/` (generado, no versionado)
  - `target/quadras-0.0.1-SNAPSHOT.jar` (generado, no versionado)
- Motivo del cambio: Validar `mvnw test` y `mvnw -DskipTests package` luego de la configuracion base.
- Impacto funcional: Build y pruebas en verde; pendiente validacion de conexion contra MySQL real del entorno hotel.

## 2026-03-12 | Hito 2 | Commit backend de base tecnica
- Componente afectado: Backend (codigo + documentacion)
- Archivos tocados:
  - Configuracion y capas base en `src/main/**`
  - Pruebas en `src/test/**`
  - Documentacion de seguimiento en `docs/**`
- Motivo del cambio: Registrar el avance estable del Hito 2 en control de versiones.
- Impacto funcional: Se habilita infraestructura base de backend para siguientes hitos.

## 2026-03-12 | Hito 2 | Sincronizacion de tablero con commits actuales
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Reflejar hash de commit backend (`6e6a46d`) y commit documental frontend (`bf91833`) del Hito 2.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 2 | Validacion de arranque local contra MySQL real
- Componente afectado: Backend (validacion de integracion local)
- Archivos tocados:
  - `src/main/resources/application-local.properties` (usado en ejecucion)
- Motivo del cambio: Ejecutar `spring-boot:run` con perfil `local` para validar conexion real a MySQL del entorno.
- Impacto funcional: Se detecta bloqueo por credenciales (`Access denied`) con usuario `quadras` y tambien con `root`.

## 2026-03-12 | Hito 2 | Reclasificacion de estado a bloqueado
- Componente afectado: Backend (gobierno de hito)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Mantener trazabilidad real del hito tras falla de conectividad local por credenciales.
- Impacto funcional: Sin cambios funcionales en API; hito no se cierra hasta resolver acceso MySQL.

## 2026-03-12 | Hito 2 | Guia de provisionamiento MySQL local
- Componente afectado: Backend (documentacion operativa)
- Archivos tocados:
  - `docs/MYSQL_LOCAL_SETUP.md`
- Motivo del cambio: Documentar pasos concretos para crear DB/usuario esperados por `application-local.properties`.
- Impacto funcional: Sin impacto funcional; reduce tiempo de desbloqueo del entorno local.

## 2026-03-12 | Hito 2 | Actualizacion de tablero con commits documentales finales
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Reflejar commits documentales mas recientes de backend y frontend en la fila de Hito 2.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 2 | Ajuste de credenciales y base local segun entorno real
- Componente afectado: Backend (configuracion local)
- Archivos tocados:
  - `src/main/resources/application-local.properties`
- Motivo del cambio: Alinear defaults locales a `db_quadras` con credenciales `root/sasa`.
- Impacto funcional: Permite arranque local en el entorno actual sin modificar el perfil de test.

## 2026-03-12 | Hito 2 | Validacion final de conectividad MySQL local
- Componente afectado: Backend (validacion de entorno)
- Archivos tocados:
  - `target/local-run.log` (generado, no versionado)
  - `target/surefire-reports/` (generado, no versionado)
- Motivo del cambio: Verificar `spring-boot:run` con perfil `local` y confirmar conexion/Flyway en `db_quadras`.
- Impacto funcional: Hito 2 desbloqueado y validado en entorno local.

## 2026-03-12 | Hito 2 | Cierre de hito en tablero e hitos
- Componente afectado: Backend (control de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/MYSQL_LOCAL_SETUP.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar cierre del Hito 2 y dejar guia local actualizada con valores validados.
- Impacto funcional: Sin cambios de negocio; trazabilidad y estado del proyecto actualizados.

## 2026-03-12 | Hito 2 | Sincronizacion final de hashes de commit en tablero
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Reflejar commits finales de cierre de Hito 2 en backend y frontend.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 3 | Inicio secuencial del hito (fase backend N/A)
- Componente afectado: Backend (gobierno del hito)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Abrir formalmente el Hito 3 y ejecutar la fase backend requerida por metodologia, sin cambios funcionales de API.
- Impacto funcional: Sin cambios en runtime backend; habilita inicio ordenado de la implementacion frontend.

## 2026-03-12 | Hito 3 | Cierre de frontend base y consolidacion del tablero
- Componente afectado: Backend (control global de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar finalizacion de Hito 3 con commit frontend estable y actualizar la fuente unica de verdad del proyecto.
- Impacto funcional: Sin cambios en runtime backend; el hito queda cerrado y habilita inicio del Hito 4.

## 2026-03-12 | Hito 3 | Sincronizacion final de hashes de cierre
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hashes finales de commits backend/frontend del Hito 3 en la fila consolidada del tablero.
- Impacto funcional: Sin impacto funcional; mejora trazabilidad de release por hito.

## 2026-03-12 | Hito 4 | Implementacion backend del dominio de reservas
- Componente afectado: Backend (dominio + persistencia)
- Archivos tocados:
  - `src/main/java/com/axioma/quadras/domain/model/Reservation.java`
  - `src/main/java/com/axioma/quadras/domain/model/ReservationStatus.java`
  - `src/main/java/com/axioma/quadras/domain/dto/*`
  - `src/main/java/com/axioma/quadras/repository/ReservationRepository.java`
  - `src/main/resources/db/migration/V2__create_reservations_domain.sql`
- Motivo del cambio: Definir entidad JPA de reservas, estados permitidos, DTOs base y migracion Flyway del dominio.
- Impacto funcional: Queda listo el modelo de datos para exponer API de reservas en Hito 5.

## 2026-03-12 | Hito 4 | Pruebas y validacion backend del dominio
- Componente afectado: Backend (calidad)
- Archivos tocados:
  - `src/test/java/com/axioma/quadras/domain/model/ReservationTest.java`
  - `src/test/java/com/axioma/quadras/domain/dto/ReservationDtoTest.java`
  - `src/test/java/com/axioma/quadras/infrastructure/FlywayReservationMigrationTest.java`
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Cubrir reglas de dominio, mapeo DTO y aplicacion de migraciones Flyway en entorno de pruebas.
- Impacto funcional: Backend validado en `mvnw test` para continuar con implementacion frontend del Hito 4.

## 2026-03-12 | Hito 4 | Cierre frontend del modelo de reservas
- Componente afectado: Frontend (dominio y serializacion)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/domain/*`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/test/features/reservations/domain/reservation_models_test.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
- Motivo del cambio: Alinear contrato frontend con entidad/DTO backend de reservas y validar serializacion.
- Impacto funcional: Frontend queda listo para consumir API real en hitos posteriores sin redefinir modelo.

## 2026-03-12 | Hito 4 | Cierre global de hito y actualizacion de tablero
- Componente afectado: Backend (gobierno de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar Hito 4 como completado en backend/frontend y dejar proximo paso en Hito 5.
- Impacto funcional: Sin impacto en runtime; trazabilidad completa del hito cerrada.

## 2026-03-12 | Hito 4 | Sincronizacion final de hashes de commit
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar hash documental final de cierre en la fila consolidada del Hito 4.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 5 | Implementacion backend de API de reservas
- Componente afectado: Backend (controller/service/repository)
- Archivos tocados:
  - `src/main/java/com/axioma/quadras/controller/ReservationController.java`
  - `src/main/java/com/axioma/quadras/service/ReservationService.java`
  - `src/main/java/com/axioma/quadras/repository/ReservationRepository.java`
  - `src/main/java/com/axioma/quadras/config/ApiExceptionHandler.java`
- Motivo del cambio: Exponer endpoints de alta, listado y consulta por id con codigos HTTP consistentes.
- Impacto funcional: Backend publica API `/api/v1/reservations` lista para integracion inicial del cliente.

## 2026-03-12 | Hito 5 | Pruebas de integracion y validacion backend
- Componente afectado: Backend (calidad + gobernanza)
- Archivos tocados:
  - `src/test/java/com/axioma/quadras/controller/ReservationControllerTest.java`
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Validar flujo principal de API, casos invalidos y 404 con MockMvc + Flyway en test.
- Impacto funcional: API de reservas validada en `mvnw test` para continuar cierre del hito.

## 2026-03-12 | Hito 5 | Sincronizacion frontend (N/A funcional)
- Componente afectado: Frontend (seguimiento)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
- Motivo del cambio: Registrar fase frontend sin cambios funcionales y revalidar estabilidad del cliente.
- Impacto funcional: Sin impacto en UI; hito backend queda trazado en ambos repos.

## 2026-03-12 | Hito 5 | Cierre global de hito y actualizacion de tablero
- Componente afectado: Backend (gobierno de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar Hito 5 como completado y habilitar inicio del Hito 6.
- Impacto funcional: Sin cambios de runtime adicionales; tablero y trazabilidad cerrados.

## 2026-03-12 | Hito 5 | Sincronizacion final de hashes de cierre
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hash documental final del backend en la fila consolidada del Hito 5.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 6 | Inicio secuencial del hito (fase backend N/A)
- Componente afectado: Backend (gobierno de hito)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Iniciar formalmente Hito 6 respetando flujo secuencial con fase backend sin cambios funcionales.
- Impacto funcional: Sin cambios en API backend; habilita implementacion ordenada de frontend.

## 2026-03-12 | Hito 6 | Implementacion frontend de agenda y alta base
- Componente afectado: Frontend (presentacion + estado local)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/schedule/presentation/schedule_page.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/presentation/new_reservation_page.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/application/reservation_app_service.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/home/presentation/shell_page.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/app/quedras_app.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/app/router/app_router.dart`
- Motivo del cambio: Implementar vistas operativas del Hito 6 con validaciones de formulario y estados locales de carga/error.
- Impacto funcional: Frontend permite alta y visualizacion diaria de reservas en modo local en memoria.

## 2026-03-12 | Hito 6 | Pruebas de frontend y cierre global de hito
- Componente afectado: Proyecto (calidad + gobernanza)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/test/features/reservations/application/reservation_app_service_test.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Validar `flutter test`/`flutter analyze`, cerrar estado del Hito 6 y consolidar tablero.
- Impacto funcional: Hito 6 cerrado con trazabilidad completa y base lista para reglas de conflicto del Hito 7.

## 2026-03-12 | Hito 6 | Sincronizacion final de hashes de cierre
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hash documental final del backend en la fila consolidada del Hito 6.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 7 | Reglas backend de solapamiento y horario
- Componente afectado: Backend (service/repository/domain)
- Archivos tocados:
  - `src/main/java/com/axioma/quadras/service/ReservationService.java`
  - `src/main/java/com/axioma/quadras/repository/ReservationRepository.java`
  - `src/main/java/com/axioma/quadras/domain/model/ReservationRules.java`
- Motivo del cambio: Incorporar validaciones de negocio para conflictos de horario, ventana operativa y duraciones permitidas.
- Impacto funcional: `POST /api/v1/reservations` ahora rechaza solapamientos y horarios no permitidos con codigos HTTP consistentes.

## 2026-03-12 | Hito 7 | Pruebas de integracion y gobierno backend
- Componente afectado: Backend (calidad + documentacion)
- Archivos tocados:
  - `src/test/java/com/axioma/quadras/controller/ReservationControllerTest.java`
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Cubrir casos de conflicto, horario invalido y duracion invalida, y registrar avance del hito.
- Impacto funcional: Backend validado en `mvnw test` para continuar implementacion frontend de Hito 7.

## 2026-03-12 | Hito 7 | Cierre frontend de reglas de negocio
- Componente afectado: Frontend (servicio + UI + pruebas)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/application/reservation_app_service.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/presentation/new_reservation_page.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/test/features/reservations/application/reservation_app_service_test.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
- Motivo del cambio: Aplicar en el cliente las mismas reglas de solapamiento, horario y duracion del backend y validar con `flutter test` y `flutter analyze`.
- Impacto funcional: Frontend bloquea reservas invalidas con mensajes consistentes y queda listo para evolucion de Hito 8.

## 2026-03-12 | Hito 7 | Cierre global y actualizacion de tablero
- Componente afectado: Backend (gobierno de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar Hito 7 como completado en backend/frontend y establecer Hito 8 como siguiente paso del proyecto.
- Impacto funcional: Sin cambios de runtime adicionales; trazabilidad del hito cerrada en fuente de verdad global.

## 2026-03-12 | Hito 7 | Sincronizacion final de hashes de cierre
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hash de commit documental de cierre backend en la fila consolidada del Hito 7.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 8 | Implementacion backend de edicion y cancelacion
- Componente afectado: Backend (controller/service/repository/domain)
- Archivos tocados:
  - `src/main/java/com/axioma/quadras/controller/ReservationController.java`
  - `src/main/java/com/axioma/quadras/service/ReservationService.java`
  - `src/main/java/com/axioma/quadras/repository/ReservationRepository.java`
  - `src/main/java/com/axioma/quadras/domain/model/Reservation.java`
  - `src/main/java/com/axioma/quadras/domain/dto/UpdateReservationDto.java`
- Motivo del cambio: Habilitar operaciones de mantenimiento de reservas en API (`PUT` para editar y `PATCH` para cancelar) con control de estado y reglas de integridad.
- Impacto funcional: Backend permite editar y cancelar reservas existentes, aplicando horario/duracion/solapamiento y validaciones por estado.

## 2026-03-12 | Hito 8 | Pruebas y documentacion backend
- Componente afectado: Backend (calidad + gobierno de hito)
- Archivos tocados:
  - `src/test/java/com/axioma/quadras/controller/ReservationControllerTest.java`
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Validar casos de exito/error de update/cancel en pruebas de integracion y registrar decisiones/estado del hito.
- Impacto funcional: `mvnw test` en verde con cobertura de operaciones de mantenimiento del Hito 8.

## 2026-03-12 | Hito 8 | Cierre frontend de edicion y cancelacion
- Componente afectado: Frontend (servicio + agenda + pruebas)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/application/reservation_app_service.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/domain/update_reservation_model.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/schedule/presentation/schedule_page.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/test/features/reservations/application/reservation_app_service_test.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
- Motivo del cambio: Implementar acciones de edicion/cancelacion desde agenda, extender servicio en memoria y cerrar documentacion del hito en frontend.
- Impacto funcional: Cliente permite mantenimiento de reservas con reglas de negocio alineadas al backend y validacion en `flutter test`/`flutter analyze`.

## 2026-03-12 | Hito 8 | Cierre global y actualizacion de tablero
- Componente afectado: Backend (gobierno de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar Hito 8 como completado en backend/frontend y habilitar inicio de Hito 9.
- Impacto funcional: Sin cambios de runtime adicionales; fuente unica de verdad actualizada.

## 2026-03-12 | Hito 8 | Sincronizacion final de hashes de cierre
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hash del commit documental de cierre backend en la fila consolidada del Hito 8.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 9 | Inicio secuencial del hito (fase backend)
- Componente afectado: Backend (gobierno de hito)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Abrir Hito 9 en estado `En progreso` y fijar decision tecnica de integracion HTTP local entre frontend y backend.
- Impacto funcional: Sin cambios funcionales en API backend durante esta fase; se mantiene estabilidad del contrato existente.

## 2026-03-12 | Hito 9 | Validacion backend previa a integracion cliente
- Componente afectado: Backend (calidad)
- Archivos tocados:
  - `target/surefire-reports/` (generado, no versionado)
- Motivo del cambio: Ejecutar `mvnw test` para confirmar estabilidad de endpoints y reglas antes de conectar frontend por HTTP real.
- Impacto funcional: Backend validado en verde (`22 tests`) para continuar fase frontend del Hito 9.

## 2026-03-12 | Hito 9 | Cierre frontend de integracion local
- Componente afectado: Frontend (red + reservas + pruebas)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/core/network/api_client.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/core/network/local_http_client.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/features/reservations/infrastructure/http_reservation_app_service.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/app/quedras_app.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/test/features/reservations/infrastructure/http_reservation_app_service_test.dart`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
- Motivo del cambio: Conectar cliente de reservas a backend local por HTTP y cerrar trazabilidad de pruebas/documentacion en repo frontend.
- Impacto funcional: Frontend consume endpoints reales de reservas y propaga errores de API/red a la UI.

## 2026-03-12 | Hito 9 | Cierre global y actualizacion de tablero
- Componente afectado: Backend (gobierno de proyecto)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Consolidar Hito 9 como completado en backend/frontend y habilitar inicio de Hito 10.
- Impacto funcional: Sin cambios de runtime adicionales; fuente de verdad global actualizada.

## 2026-03-12 | Hito 9 | Sincronizacion final de hashes de cierre
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hash del commit documental de cierre backend en la fila consolidada del Hito 9.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 10 | Implementacion backend para instalacion y smoke reproducible
- Componente afectado: Backend (operacion + instalacion)
- Archivos tocados:
  - `scripts/backend_smoke_local.ps1`
  - `docs/INSTALACION_BACKEND_HOTEL.md`
- Motivo del cambio: Incorporar guia operativa de instalacion backend y script repetible para validar salud/flujo de reservas en entorno local.
- Impacto funcional: Sin cambios de logica de negocio; mejora capacidad de despliegue y verificacion tecnica.

## 2026-03-12 | Hito 10 | Validacion tecnica backend y evidencia integral
- Componente afectado: Backend (calidad + trazabilidad)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/VALIDACION_INTEGRAL_HITO10.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar estado de Hito 10 en progreso, decision tecnica de cierre de release y resultados de validaciones backend/frontend.
- Impacto funcional: Se consolida evidencia de release y se explicita bloqueo de build desktop por Visual Studio incompleto.

## 2026-03-12 | Hito 10 | Cierre frontend de documentacion de instalacion
- Componente afectado: Frontend (documentacion + scripts)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/README.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/INSTALACION_FRONTEND_HOTEL.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/VALIDACION_FRONTEND_HITO10.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/scripts/frontend_preflight.ps1`
- Motivo del cambio: Documentar instalacion/validacion de cliente desktop y registrar evidencia del bloqueo de build Windows.
- Impacto funcional: Sin cambios de logica de UI; queda preparado proceso operativo de instalacion.

## 2026-03-12 | Hito 10 | Actualizacion global de estado a bloqueado
- Componente afectado: Backend (tablero + gobernanza)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Reflejar cierre parcial del Hito 10 con bloqueo externo de toolchain Visual Studio.
- Impacto funcional: Proyecto queda trazado en estado bloqueado hasta resolver build desktop de frontend.

## 2026-03-12 | Hito 10 | Sincronizacion final de hashes de trazabilidad
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar hash documental de actualizacion de estado bloqueado en la fila del Hito 10.
- Impacto funcional: Sin impacto funcional.

## 2026-03-12 | Hito 10 | Revalidacion backend previa al cierre final
- Componente afectado: Backend (calidad + evidencia tecnica)
- Archivos tocados:
  - `docs/VALIDACION_INTEGRAL_HITO10.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Reejecutar validaciones backend (`mvnw test`, `mvnw -DskipTests package`, smoke local) para confirmar estabilidad antes del cierre global del hito.
- Impacto funcional: Sin cambios de logica; backend ratificado en estado estable para cierre de Hito 10.

## 2026-03-12 | Hito 10 | Cierre frontend por resolucion de toolchain y build release
- Componente afectado: Frontend (validacion final)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/INSTALACION_FRONTEND_HOTEL.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_PROGRESS.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/VALIDACION_FRONTEND_HITO10.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/FRONT_CHANGELOG.md`
- Motivo del cambio: Completar prerequisitos de Visual Studio para Flutter Desktop y validar `flutter build windows --release` exitoso.
- Impacto funcional: Frontend sin bloqueos de build en Windows; Hito 10 queda listo para cierre global.

## 2026-03-12 | Hito 10 | Cierre global del hito y actualizacion de tablero final
- Componente afectado: Backend (gobernanza + cierre documental)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/VALIDACION_INTEGRAL_HITO10.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Marcar Hito 10 como completado en backend/frontend, cerrar bloqueos y actualizar decision tecnica de toolchain Windows.
- Impacto funcional: Proyecto QUEDRAS cierra el plan de 10 hitos con estado estable y trazabilidad completa.

## 2026-03-14 | Hito 11 | Renombre seguro backend a COSTANORTE (fase 1)
- Componente afectado: Backend (configuracion + identidad tecnica)
- Archivos tocados:
  - `pom.xml`
  - `src/main/resources/application.properties`
  - `src/main/resources/application-local.properties`
  - `src/main/java/com/axioma/quadras/service/SystemStatusService.java`
  - `src/test/java/com/axioma/quadras/controller/SystemStatusControllerTest.java`
  - `src/test/resources/application-test.properties`
  - `src/main/java/com/axioma/quadras/{config,controller,repository,service}/package-info.java`
  - `scripts/backend_smoke_local.ps1`
- Motivo del cambio: Cambiar identidad de QUEDRAS a COSTANORTE sin romper operacion actual, agregando compatibilidad temporal de variables (`COSTANORTE_*` con fallback a `QUADRAS_*`).
- Impacto funcional: Backend expone nombre de servicio `COSTANORTE-BACKEND`, compila artefacto `costanorte-0.0.1-SNAPSHOT.jar` y mantiene compatibilidad con configuraciones legacy.

## 2026-03-14 | Hito 11 | Validacion backend del renombre seguro
- Componente afectado: Backend (calidad + trazabilidad)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/INSTALACION_BACKEND_HOTEL.md`
  - `docs/MYSQL_LOCAL_SETUP.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar criterios, decision tecnica y guias operativas actualizadas para ejecutar COSTANORTE en entorno local del hotel.
- Impacto funcional: Validaciones backend en verde (`mvnw test`, `mvnw -DskipTests package`, `backend_smoke_local.ps1`) con documentacion alineada al nuevo nombre.

## 2026-03-14 | Hito 11 | Renombre seguro frontend a COSTANORTE (fase 1)
- Componente afectado: Frontend (branding + build + configuracion)
- Archivos tocados:
  - `C:/Users/Public/Documents/Proyectos/quedras-front/pubspec.yaml`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/lib/**`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/windows/**`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/test/**`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/README.md`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/docs/**`
  - `C:/Users/Public/Documents/Proyectos/quedras-front/scripts/frontend_preflight.ps1`
- Motivo del cambio: Completar renombre comercial de app/ejecutable a COSTANORTE manteniendo compatibilidad temporal con `QUEDRAS_API_BASE_URL`.
- Impacto funcional: Frontend compila y genera `costanorte.exe` sin romper flujo de reservas ni integracion con backend local.

## 2026-03-14 | Hito 11 | Cierre global y actualizacion de tablero
- Componente afectado: Backend (gobernanza + seguimiento)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
  - `docs/VALIDACION_RENOMBRE_HITO11.md`
- Motivo del cambio: Consolidar Hito 11 como completado para backend y frontend, actualizar riesgos activos y dejar evidencia centralizada del renombre seguro.
- Impacto funcional: Proyecto pasa a identidad COSTANORTE con control de progreso y trazabilidad documental consistentes.

## 2026-03-14 | Hito 12 | Implementacion backend de seguridad JWT y usuario demo
- Componente afectado: Backend (security + auth + persistencia)
- Archivos tocados:
  - `pom.xml`
  - `src/main/java/com/axioma/quadras/config/*`
  - `src/main/java/com/axioma/quadras/controller/AuthController.java`
  - `src/main/java/com/axioma/quadras/controller/UserController.java`
  - `src/main/java/com/axioma/quadras/domain/dto/{AuthTokenDto,CurrentUserDto,LoginRequestDto}.java`
  - `src/main/java/com/axioma/quadras/domain/model/{AppUser,AppUserRole}.java`
  - `src/main/java/com/axioma/quadras/repository/AppUserRepository.java`
  - `src/main/java/com/axioma/quadras/service/{ApplicationUserDetailsService,AuthService,AuthenticatedUserPrincipal,DemoUserInitializer,JwtService}.java`
  - `src/main/resources/application.properties`
  - `src/main/resources/db/migration/V3__create_users_security_domain.sql`
- Motivo del cambio: Incorporar login JWT, usuario demo bootstrap, rol inicial `OPERATOR` y proteccion de endpoints de reservas para habilitar autenticacion del cliente.
- Impacto funcional: La API deja de aceptar llamadas anonimas sobre reservas y expone contrato `POST /api/v1/auth/login` + `GET /api/v1/users/me`.

## 2026-03-14 | Hito 12 | Pruebas automatizadas y smoke con autenticacion
- Componente afectado: Backend (calidad + validacion)
- Archivos tocados:
  - `src/test/java/com/axioma/quadras/controller/AuthControllerTest.java`
  - `src/test/java/com/axioma/quadras/controller/ReservationControllerTest.java`
  - `src/test/java/com/axioma/quadras/infrastructure/FlywayReservationMigrationTest.java`
  - `src/test/resources/application-test.properties`
  - `scripts/backend_smoke_local.ps1`
- Motivo del cambio: Validar login, proteccion de endpoints, rechazo de JWT invalido, migracion de usuarios y smoke local con encabezado `Authorization`.
- Impacto funcional: `mvnw test` queda en verde con cobertura de seguridad y el smoke local confirma login JWT real contra MySQL.

## 2026-03-14 | Hito 12 | Cierre documental de backend y contrato para frontend
- Componente afectado: Backend (gobernanza + documentacion operativa)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/INSTALACION_BACKEND_HOTEL.md`
  - `docs/MYSQL_LOCAL_SETUP.md`
  - `docs/VALIDACION_SEGURIDAD_HITO12.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar el cierre backend del Hito 12, documentar credenciales demo/variables JWT y dejar evidencia operativa para integracion del frontend.
- Impacto funcional: Equipo de frontend y operacion cuentan con un contrato autenticado estable y reproducible.

## 2026-03-14 | Hito 12 | Sincronizacion final de hash backend
- Componente afectado: Backend (tablero de progreso)
- Archivos tocados:
  - `docs/TABLERO_PROGRESO.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar el hash real del commit backend del Hito 12 (`557c88e`) en la fuente unica de verdad del proyecto.
- Impacto funcional: Sin impacto funcional; mejora trazabilidad exacta del cierre backend del hito.

## 2026-03-16 | Post Hito 12 | Reenfoque comercial del frontend y actualizacion global
- Componente afectado: Backend (gobernanza + trazabilidad global)
- Archivos tocados:
  - `docs/HITOS.md`
  - `docs/TABLERO_PROGRESO.md`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Registrar el nuevo alcance visible del frontend, que pasa a 3 modulos comerciales (`Massagens`, `Quadras`, `Tours e Viagens`) mas `Configuracoes`, con salida visible en `pt-BR`, y enlazar la trazabilidad al commit frontend `384d38b`.
- Impacto funcional: Sin cambios nuevos en la API actual; el siguiente paso de backend queda enfocado en contratos dedicados para `Massagens` y `Tours e Viagens`, y en la coherencia de mensajes `pt-BR` donde la UI consuma errores reales.

## 2026-03-19 | Post Hito 12 | Implementacion backend del dominio de massagens
- Componente afectado: Backend (massagens + persistencia + seguridad + pruebas)
- Archivos tocados:
  - `src/main/java/com/axioma/quadras/domain/model/{MassageProvider,MassageBooking}.java`
  - `src/main/java/com/axioma/quadras/domain/dto/{CreateMassageProviderDto,UpdateMassageProviderDto,MassageProviderDto,CreateMassageBookingDto,MassageBookingDto}.java`
  - `src/main/java/com/axioma/quadras/repository/{MassageProviderRepository,MassageBookingRepository}.java`
  - `src/main/java/com/axioma/quadras/service/{MassageProviderService,MassageBookingService}.java`
  - `src/main/java/com/axioma/quadras/controller/{MassageProviderController,MassageBookingController}.java`
  - `src/main/java/com/axioma/quadras/config/SecurityConfig.java`
  - `src/main/resources/db/migration/V4__create_massages_domain.sql`
  - `src/test/java/com/axioma/quadras/controller/MassageControllerTest.java`
  - `src/test/java/com/axioma/quadras/infrastructure/FlywayReservationMigrationTest.java`
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
- Motivo del cambio: Incorporar contratos persistidos para `Massagens` y permitir que el frontend administre prestadores y agendamentos desde API real en lugar de listas manuales locales.
- Impacto funcional: El backend expone `GET/POST/PUT /api/v1/massages/providers` y `GET/POST /api/v1/massages/bookings`, con validacion de prestador activo y bloqueo de doble reserva del mismo prestador en la misma fecha/hora.

## 2026-03-20 | Post Hito 12 | Plan e implementacion de pagos completos para massagens
- Componente afectado: Backend + documentacion
- Archivos tocados:
  - `docs/DECISIONES_TECNICAS.md`
  - `docs/CHANGELOG_DESARROLLO.md`
  - `src/main/java/com/axioma/quadras/domain/model/{MassageBooking,MassagePaymentMethod}.java`
  - `src/main/java/com/axioma/quadras/domain/dto/{CreateMassageBookingDto,MassageBookingDto,UpdateMassagePaymentDto}.java`
  - `src/main/java/com/axioma/quadras/repository/MassageBookingRepository.java`
  - `src/main/java/com/axioma/quadras/service/MassageBookingService.java`
  - `src/main/java/com/axioma/quadras/controller/MassageBookingController.java`
  - `src/main/resources/db/migration/V5__extend_massage_payments.sql`
  - `src/test/java/com/axioma/quadras/controller/MassageControllerTest.java`
  - `src/test/java/com/axioma/quadras/infrastructure/FlywayReservationMigrationTest.java`
- Motivo del cambio: Incorporar captura completa de pago al agendar, sumar flujo separado de informar pago y permitir busquedas operativas sobre masajes agendados.
- Impacto funcional: La API de massagens pasa a soportar medio de pago, fecha y observaciones tanto en el alta como en un endpoint dedicado de pago.
