# MYSQL LOCAL SETUP - COSTANORTE BACKEND

## Objetivo
Dejar el backend listo para arrancar con perfil `local` usando MySQL del entorno.

## 1) Crear base y usuario (ejecutar con cuenta administradora de MySQL)
```sql
CREATE DATABASE IF NOT EXISTS db_quadras CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2) Definir variables de entorno (PowerShell)
```powershell
$env:COSTANORTE_DB_HOST = "localhost"
$env:COSTANORTE_DB_PORT = "3306"
$env:COSTANORTE_DB_NAME = "db_quadras"
$env:COSTANORTE_DB_USER = "root"
$env:COSTANORTE_DB_PASSWORD = "sasa"
```

Alternativas soportadas por el backend:

- URL completa: `COSTANORTE_DB_URL` o `QUADRAS_DB_URL` o `DB_URL`
- Usuario: `COSTANORTE_DB_USER` o `QUADRAS_DB_USER` o `DB_USERNAME`
- Clave: `COSTANORTE_DB_PASSWORD` o `QUADRAS_DB_PASSWORD` o `DB_PASSWORD`
- Host/puerto/base legacy: `QUADRAS_DB_HOST`, `QUADRAS_DB_PORT`, `QUADRAS_DB_NAME`

Si no defines ninguna variable, el backend usa por defecto:

- host `localhost`
- puerto `3306`
- base `db_quadras`
- usuario `root`
- clave `sasa`

## 3) Validar arranque local del backend
```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local --server.port=0"
```

## Resultado esperado
- Spring Boot inicia sin errores.
- Flyway aplica `V1__init_technical_baseline.sql`, `V2__create_reservations_domain.sql` y `V3__create_users_security_domain.sql`.
- Endpoint tecnico responde en `GET /api/v1/system/health`.
- Usuario demo disponible para login: `operador.demo / Costanorte2026!`.
- Compatibilidad temporal: el backend tambien acepta variables legacy `QUADRAS_*`.
