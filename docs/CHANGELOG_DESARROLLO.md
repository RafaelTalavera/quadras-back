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
