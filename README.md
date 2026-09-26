# COSTANORTE API

API REST Spring Boot para la operacion de COSTANORTE. El frontend Flutter vive
en un repositorio independiente y consume esta aplicacion exclusivamente por
HTTP/JSON bajo `/api/v1`.

Este repositorio no instala ni empaqueta el frontend, Java, servicios Windows o
MySQL portable.

## Configuracion local

El proyecto si define variables de entorno de base de datos en [src/main/resources/application.properties](/abs/path/c:/Users/Public/Documents/Proyectos/quadras/src/main/resources/application.properties:1).

Orden de prioridad de las variables de base de datos:

1. `COSTANORTE_DB_URL`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
2. `QUADRAS_DB_URL`, `QUADRAS_DB_USER`, `QUADRAS_DB_PASSWORD`
3. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
4. `COSTANORTE_DB_HOST`, `COSTANORTE_DB_PORT`, `COSTANORTE_DB_NAME`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
5. Compatibilidad temporal para host/port/name con `QUADRAS_DB_HOST`, `QUADRAS_DB_PORT`, `QUADRAS_DB_NAME`

Valores predeterminados locales:

- Host: `localhost`
- Puerto: `3306`
- Base: `db_quadras`
- Usuario: `root`
- Clave: `sasa`
- API: `http://127.0.0.1:8080/api/v1`
- Operador demo: `operador.demo` / `123456`
- Supervisor local: `supervisor.demo` / `654321`

## Ejecutar la API

```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
```

Comprobar el estado:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/v1/system/health
```

## Validacion

```powershell
.\mvnw test
.\mvnw -DskipTests package
.\scripts\backend_smoke_local.ps1 -SkipBuild
```

## Herramientas locales

- `configure_local_mysql.ps1`: prepara una base en un MySQL existente.
- `backend_smoke_local.ps1`: prueba la API extremo a extremo.
- `start_backend_first_test.ps1` y `stop_backend_first_test.ps1`: instancia temporal.
- `seed_court_bookings_jan_jul_2026.ps1`: carga puntual mediante HTTP.

## Documentacion

- `docs/API_REST_LOCAL.md`
- `docs/ACONDICIONAMIENTO_API_REST.md`
- `docs/MYSQL_LOCAL_SETUP.md`
- `docs/RAILWAY_DEPLOY.md`
