# RAILWAY BACKEND RAM - ANALISIS Y AJUSTES

## Objetivo
Reducir el costo fijo de RAM del backend desplegable en Railway sin apagar modulos funcionales de negocio ni introducir degradacion evidente en el rendimiento operativo.

## Cambios aplicados
- Se agrego el flag `costanorte.features.maintenance-simulation.enabled` para volver opcional la simulacion de mantencion.
- Se creo el perfil `railway` en `application-railway.properties` para desactivar:
  - `costanorte.security.demo-user.enabled`
  - `costanorte.features.maintenance-simulation.enabled`
- Se parametrizaron los scripts `start_backend_console.ps1` y `start_backend_detached.ps1` para aceptar `COSTANORTE_JAVA_OPTS`.
- Se fijaron defaults de JVM para medicion y operacion controlada:
  - `-Xms256m`
  - `-Xmx512m`
  - `-XX:NativeMemoryTracking=summary`
- Se elimino `spring-boot-starter-actuator` del backend.
- Se elimino `spring-boot-starter-json` explicito porque `spring-boot-starter-webmvc` ya incorpora el stack Jackson requerido.
- Se removio la configuracion `management.*` asociada a Actuator y se mantuvo `GET /api/v1/system/health` como endpoint tecnico propio.

## Medicion comparativa

### 1. Baseline backend con JVM controlada
- Arranque: `~16.0 s`
- Working set: `~500.48 MB`
- Memoria privada: `~554.14 MB`
- NMT committed: `~514 MB`
- Metaspace committed: `~100.99 MB`

### 2. Luego del recorte de classpath
- Arranque: `~12.47 s`
- Working set: `~475.69 MB`
- Memoria privada: `~540.33 MB`
- NMT committed: `~506 MB`
- Metaspace committed: `~96.70 MB`

Ganancia frente al baseline:
- `-24.79 MB` working set
- `-13.81 MB` memoria privada
- `-8 MB` NMT committed
- `-4.29 MB` metaspace committed
- `-3.53 s` en tiempo de arranque

### 3. Luego del perfil `local,railway`
- Arranque: `~15.02 s`
- Working set: `~475.11 MB`
- Memoria privada: `~553.44 MB`
- NMT committed: `~502 MB`
- Metaspace committed: `~94.08 MB`
- Clases cargadas: `~19994`

Ganancia frente al baseline:
- `-25.37 MB` working set
- `-0.70 MB` memoria privada
- `-12 MB` NMT committed
- `-6.91 MB` metaspace committed

## Lectura tecnica
- El mayor ahorro estable vino del recorte de classpath (`Actuator` + `starter-json` redundante).
- El perfil `railway` reduce clases cargadas y metaspace, pero la memoria privada del proceso puede fluctuar entre corridas y no debe leerse de forma aislada.
- Para este backend, los indicadores mas estables para comparar ajustes son:
  - `NMT committed`
  - `Metaspace committed`
  - cantidad de clases cargadas
  - tiempo de arranque

## Validacion ejecutada
- `./mvnw -DskipTests compile`
- `./mvnw clean -DskipTests package`
- Validacion de sintaxis PowerShell para `start_backend_console.ps1`
- Validacion de sintaxis PowerShell para `start_backend_detached.ps1`
- Arranques locales con medicion post-GC usando `jcmd`

## Pendientes
- Medir el backend bajo carga corta de requests para confirmar si `512 MB` sigue siendo suficiente en Railway con trafico real.
- Definir un perfil productivo independiente de `local` para no depender de propiedades locales al desplegar en Railway.
