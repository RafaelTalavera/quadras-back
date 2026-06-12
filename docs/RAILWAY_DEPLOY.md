# RAILWAY DEPLOY

## Variables minimas

- `SPRING_PROFILES_ACTIVE=railway`
- `COSTANORTE_JWT_SECRET=<secreto-largo-y-unico>`

## Base de datos Hostinger

Puedes usar cualquiera de estos esquemas:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `COSTANORTE_DB_URL`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
- `QUADRAS_DB_URL`, `QUADRAS_DB_USER`, `QUADRAS_DB_PASSWORD`
- `COSTANORTE_DB_HOST`, `COSTANORTE_DB_PORT`, `COSTANORTE_DB_NAME`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
- `QUADRAS_DB_HOST`, `QUADRAS_DB_PORT`, `QUADRAS_DB_NAME`, `QUADRAS_DB_USER`, `QUADRAS_DB_PASSWORD`

Orden efectivo de resolucion en el proyecto:

1. `COSTANORTE_DB_URL` o `QUADRAS_DB_URL` o `DB_URL`
2. Si no existe URL completa, se arma con `COSTANORTE_DB_HOST` o `QUADRAS_DB_HOST`, junto con puerto y nombre
3. Usuario: `COSTANORTE_DB_USER` o `QUADRAS_DB_USER` o `DB_USERNAME`
4. Clave: `COSTANORTE_DB_PASSWORD` o `QUADRAS_DB_PASSWORD` o `DB_PASSWORD`

## Configuracion recomendada para este proyecto

Base creada en Hostinger:

- Base: `u725228781_costaNorte`
- Usuario MySQL: `u725228781_demo`

En Railway carga estas variables:

```text
SPRING_PROFILES_ACTIVE=railway
COSTANORTE_JWT_SECRET=<secreto-largo-y-unico>
DB_URL=jdbc:mysql://<MYSQL_HOST_REAL>:3306/u725228781_costaNorte?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&rewriteBatchedStatements=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048
DB_USERNAME=u725228781_demo
DB_PASSWORD=<password-mysql>
```

Notas:

- `MYSQL_HOST_REAL` debe salir del panel de Hostinger. La URL de phpMyAdmin no sirve como host JDBC.
- Usa el usuario MySQL completo `u725228781_demo`, no el alias corto `demo`.
- La base ya debe existir en Hostinger. El backend crea tablas y aplica migraciones con Flyway, pero no crea el schema MySQL.

## Comportamiento esperado

- Railway inyecta `PORT` y el backend ahora lo toma automaticamente.
- El perfil `railway` desactiva el demo user y la simulacion de mantencion.
- Flyway crea la estructura del sistema en una base vacia durante el primer arranque.
- El backend ejecuta `repair()` antes de `migrate()` en el arranque para limpiar entradas fallidas previas en `flyway_schema_history` cuando la base ya viene con un intento de migracion roto.
- El perfil `railway` fija `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect` para evitar la autodeteccion de dialecto de Hibernate 7 sobre MariaDB 11.8.
- El healthcheck publico queda en `GET /api/v1/system/health`.

## Deploy recomendado

- Fuente: repositorio GitHub
- Runtime: Docker
- Healthcheck path: `/api/v1/system/health`
- Archivo `railway.json` incluido en la raiz para fijar el healthcheck y la politica de reinicio

## Nota operativa

Si no defines `SPRING_PROFILES_ACTIVE=railway`, el backend cae en perfil `local`.

## Incidente resuelto en Railway

Durante el despliegue contra MariaDB 11.8 en Hostinger se presentaron dos fallas consecutivas:

1. Flyway quedaba bloqueado por una entrada fallida previa de `V23` en `flyway_schema_history`.
2. Hibernate 7.2.4 fallaba despues de Flyway al intentar autodetectar el dialecto de MariaDB desde `information_schema`.

Resolucion aplicada en el proyecto:

- `src/main/java/com/axioma/quadras/config/FlywayConfig.java`
  - ejecuta `flyway.repair()` y luego `flyway.migrate()` en el arranque
- `src/main/resources/application-railway.properties`
  - define `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect`

Si aparece un error parecido en otro entorno MariaDB:

- confirmar que el deploy activo usa el perfil `railway`
- verificar que el commit desplegado incluye `FlywayConfig`
- verificar que el perfil `railway` mantiene el dialecto MariaDB explicito
