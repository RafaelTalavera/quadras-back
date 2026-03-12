# INSTALACION BACKEND - QUEDRAS

## Objetivo
Dejar el backend Spring Boot operativo en la red local del hotel con MySQL.

## Prerequisitos
- Windows 10/11.
- Java 17 instalado (`java -version`).
- MySQL 8.x activo en la red/local.
- Base de datos `db_quadras` disponible (ver `docs/MYSQL_LOCAL_SETUP.md`).

## Variables de entorno recomendadas (PowerShell)
```powershell
$env:QUADRAS_DB_HOST = "localhost"
$env:QUADRAS_DB_PORT = "3306"
$env:QUADRAS_DB_NAME = "db_quadras"
$env:QUADRAS_DB_USER = "root"
$env:QUADRAS_DB_PASSWORD = "sasa"
$env:QUADRAS_SERVER_PORT = "8080"
```

## Compilacion de release
```powershell
.\mvnw -DskipTests package
```

Artefacto esperado:
- `target/quadras-0.0.1-SNAPSHOT.jar`

## Ejecucion local
```powershell
java -jar target/quadras-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --server.port=8080
```

## Verificacion de salud
```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/system/health" -Method Get
```

Respuesta esperada:
- `status: UP`

## Smoke de API de reservas (opcional recomendado)
```powershell
.\scripts\backend_smoke_local.ps1 -Port 8091
```

## Operacion
- Logs de arranque: consola o archivo definido por el operador.
- Detencion: `Ctrl + C` (si corre en consola) o detener proceso Java.

## Notas de instalacion
- El backend no depende de internet para operar.
- Si el puerto `8080` esta ocupado, definir `QUADRAS_SERVER_PORT` o usar `--server.port`.
