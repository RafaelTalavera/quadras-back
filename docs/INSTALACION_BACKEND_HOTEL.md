# INSTALACION BACKEND - COSTANORTE

## Objetivo
Dejar el backend Spring Boot operativo en la red local del hotel con MySQL.

## Prerequisitos
- Windows 10/11.
- Java 17 instalado (`java -version`).
- MySQL 8.x activo en la red/local.
- Base de datos `db_quadras` disponible (ver `docs/MYSQL_LOCAL_SETUP.md`).

## Variables de entorno recomendadas (PowerShell)
```powershell
$env:COSTANORTE_DB_HOST = "localhost"
$env:COSTANORTE_DB_PORT = "3306"
$env:COSTANORTE_DB_NAME = "db_quadras"
$env:COSTANORTE_DB_USER = "root"
$env:COSTANORTE_DB_PASSWORD = "sasa"
$env:COSTANORTE_SERVER_PORT = "8080"
$env:COSTANORTE_JWT_SECRET = "costanorte-local-jwt-secret-change-me-2026"
$env:COSTANORTE_DEMO_USER_USERNAME = "operador.demo"
$env:COSTANORTE_DEMO_USER_PASSWORD = "Costanorte2026!"
$env:COSTANORTE_DEMO_USER_ROLE = "OPERATOR"
```

## Compilacion de release
```powershell
.\mvnw -DskipTests package
```

Artefacto esperado:
- `target/costanorte-0.0.1-SNAPSHOT.jar`

## Ejecucion local
```powershell
java -jar target/costanorte-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --server.port=8080
```

## Verificacion de salud
```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/system/health" -Method Get
```

Respuesta esperada:
- `status: UP`

## Login de prueba
```powershell
$loginBody = @{
    username = "operador.demo"
    password = "Costanorte2026!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody
```

Respuesta esperada:
- `tokenType: Bearer`
- `role: OPERATOR`

## Smoke de API de reservas (opcional recomendado)
```powershell
.\scripts\backend_smoke_local.ps1 -Port 8091
```

## Operacion
- Logs de arranque: consola o archivo definido por el operador.
- Detencion: `Ctrl + C` (si corre en consola) o detener proceso Java.

## Notas de instalacion
- El backend no depende de internet para operar.
- Si el puerto `8080` esta ocupado, definir `COSTANORTE_SERVER_PORT` o usar `--server.port`.
- El usuario demo `operador.demo / Costanorte2026!` se crea/actualiza al arrancar si `COSTANORTE_DEMO_USER_ENABLED=true`.
- En entornos no locales se recomienda redefinir `COSTANORTE_JWT_SECRET` y la clave demo antes de exponer el backend a operadores reales.
- Compatibilidad temporal: si el entorno aun usa variables legacy `QUADRAS_*`, el backend las sigue aceptando en esta fase.
