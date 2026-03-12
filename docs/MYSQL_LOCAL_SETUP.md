# MYSQL LOCAL SETUP - QUEDRAS BACKEND

## Objetivo
Dejar el backend listo para arrancar con perfil `local` usando MySQL del entorno.

## 1) Crear base y usuario (ejecutar con cuenta administradora de MySQL)
```sql
CREATE DATABASE IF NOT EXISTS quadras CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'quadras'@'localhost' IDENTIFIED BY 'quadras';
GRANT ALL PRIVILEGES ON quadras.* TO 'quadras'@'localhost';
FLUSH PRIVILEGES;
```

## 2) Definir variables de entorno (PowerShell)
```powershell
$env:QUADRAS_DB_HOST = "localhost"
$env:QUADRAS_DB_PORT = "3306"
$env:QUADRAS_DB_NAME = "quadras"
$env:QUADRAS_DB_USER = "quadras"
$env:QUADRAS_DB_PASSWORD = "quadras"
```

## 3) Validar arranque local del backend
```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local --server.port=0"
```

## Resultado esperado
- Spring Boot inicia sin errores.
- Flyway aplica `V1__init_technical_baseline.sql`.
- Endpoint tecnico responde en `GET /api/v1/system/health`.
