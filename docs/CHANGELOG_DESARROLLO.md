# CHANGELOG DE DESARROLLO - QUEDRAS

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
