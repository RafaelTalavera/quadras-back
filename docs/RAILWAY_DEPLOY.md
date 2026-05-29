# RAILWAY DEPLOY

## Variables minimas

- `SPRING_PROFILES_ACTIVE=railway`
- `COSTANORTE_JWT_SECRET=<secreto-largo-y-unico>`

## Base de datos Hostinger

Puedes usar cualquiera de estos esquemas:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `COSTANORTE_DB_URL`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
- `COSTANORTE_DB_HOST`, `COSTANORTE_DB_PORT`, `COSTANORTE_DB_NAME`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`

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
- El healthcheck publico queda en `GET /api/v1/system/health`.

## Deploy recomendado

- Fuente: repositorio GitHub
- Runtime: Docker
- Healthcheck path: `/api/v1/system/health`
- Archivo `railway.json` incluido en la raiz para fijar el healthcheck y la politica de reinicio

## Nota operativa

Si no defines `SPRING_PROFILES_ACTIVE=railway`, el backend cae en perfil `local`.
