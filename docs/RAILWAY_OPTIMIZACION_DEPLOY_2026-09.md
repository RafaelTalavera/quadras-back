# Optimizacion de despliegue Railway - 2026-09

## Objetivo

Reducir el consumo evitable de red, almacenamiento de build y memoria nativa
del backend sin afectar la operacion esperada de dos a tres equipos
simultaneos, con picos de cuatro.

## Cambios aplicados

### Contexto Docker

Se incorpora `.dockerignore`. Excluye artefactos que el `Dockerfile` no usa:
`dist/`, `target/`, `.git/`, `hist/`, `installer/`, `docs/` y `scripts/`.

En el equipo de desarrollo, `dist/` ocupa aproximadamente 7,0 GiB. Antes de
este cambio se encontraba dentro del contexto que Docker podia transferir al
builder. El conjunto de archivos requerido por los `COPY` del Dockerfile
(POM, Maven Wrapper y `src`) mide aproximadamente 0,93 MiB.

No se elimina `dist/`: sigue disponible como artefacto local y queda fuera
solamente del despliegue del backend.

### JVM de produccion

El runtime Java 17 queda con estos limites en `Dockerfile`:

```
-XX:+UseSerialGC
-XX:InitialRAMPercentage=8
-XX:MaxRAMPercentage=45
-XX:MinHeapFreeRatio=10
-XX:MaxHeapFreeRatio=20
-XX:-ShrinkHeapInSteps
-XX:MaxMetaspaceSize=160m
-XX:ReservedCodeCacheSize=48m
-XX:MaxDirectMemorySize=32m
-Xss256k
```

El limite de memoria directa evita que buffers nativos crezcan sin control por
fuera del heap. No se debe definir `JAVA_OPTS` en Railway salvo que se reemplace
el conjunto completo y se realice una nueva medicion.

El perfil `railway` existente conserva el pool Hikari en tres conexiones, y
Tomcat en ocho hilos. Es apropiado como punto de partida para cuatro usuarios
con operaciones breves; no aumentar estos valores sin observar espera de
conexiones o latencia bajo carga.

### Archivos adjuntos

Los adjuntos de mantenimiento se reciben como Base64 y se guardan como BLOB en
MySQL remoto. El servicio ya limita el archivo decodificado a 8 MiB. Mantener
este limite evita multiplicar en memoria el texto Base64 y el arreglo binario
durante cargas simultaneas. Para archivos mas grandes o frecuentes se debe
usar almacenamiento de objetos y persistir solo URL y metadatos en MySQL.

## Operacion en Railway

1. Usar `SPRING_PROFILES_ACTIVE=railway` y un `COSTANORTE_JWT_SECRET` unico.
2. No adjuntar un Volume al backend mientras no exista una necesidad real de
   datos persistentes en filesystem.
3. Desplegar con el healthcheck `/api/v1/system/health`.
4. Durante la primera semana registrar RAM en reposo, RAM pico, CPU y la linea
   `Disk` en Usage. Si aparece `Disk`, comprobar primero si hay un Volume
   asociado; los BLOB actuales viven en MySQL, no en el filesystem del backend.
5. Probar en staging cuatro clientes simultaneos, incluyendo una carga de
   adjunto dentro del limite, antes de cambiar pools o limites de JVM.

## Validacion

- `./mvnw test`: correcto.
- `./mvnw -DskipTests package`: correcto.
- JAR de produccion generado: `target/costanorte-0.0.1-SNAPSHOT.jar` (65,28 MiB
  en la medicion local).
- Java 17 acepto `-XX:MaxDirectMemorySize=32m`.
- No se ejecuto `docker build` local porque Docker no esta instalado en la
  estacion de trabajo. Railway validara el Dockerfile al construir el deploy.

## Rollback

Para revertir esta optimizacion, revertir el commit que incorpora
`.dockerignore`, el limite `MaxDirectMemorySize` y esta documentacion. No hay
migraciones de base de datos ni cambios en el contrato HTTP.
