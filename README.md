# COSTANORTE Backend

Backend Spring Boot para operacion interna del hotel.

## Variables de base de datos soportadas

El proyecto si define variables de entorno de base de datos en [src/main/resources/application.properties](/abs/path/c:/Users/Public/Documents/Proyectos/quadras/src/main/resources/application.properties:1).

Orden de prioridad:

1. `COSTANORTE_DB_URL`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
2. `QUADRAS_DB_URL`, `QUADRAS_DB_USER`, `QUADRAS_DB_PASSWORD`
3. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
4. `COSTANORTE_DB_HOST`, `COSTANORTE_DB_PORT`, `COSTANORTE_DB_NAME`, `COSTANORTE_DB_USER`, `COSTANORTE_DB_PASSWORD`
5. Compatibilidad temporal para host/port/name con `QUADRAS_DB_HOST`, `QUADRAS_DB_PORT`, `QUADRAS_DB_NAME`

Valores por defecto locales:

- Host: `localhost`
- Puerto: `3306`
- Base: `db_quadras`
- Usuario: `root`
- Clave: `sasa`

## Arranque local

```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
```

## Documentacion relacionada

- `docs/MYSQL_LOCAL_SETUP.md`
- `docs/RAILWAY_DEPLOY.md`
- `docs/INSTALACION_BACKEND_HOTEL.md`
