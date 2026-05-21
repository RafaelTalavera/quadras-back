# VALIDACION INSTALADOR WINDOWS LOCAL

## Fecha
- 2026-05-01

## Objetivo
Confirmar que el instalador Windows de COSTANORTE funcione como entrega autonoma en una instalacion real, sin depender de IDE, backend manual ni Java/MySQL instalados por fuera del paquete.

## Causa raiz detectada
- El bundle `dist/windows-local` funcionaba porque corria en una carpeta escribible del repo.
- La instalacion real en `Program Files` fallaba porque los scripts intentaban escribir archivos mutables dentro de la carpeta instalada.
- Casos concretos detectados:
  - `provision_portable_mysql.ps1` generaba `mysql-initialize.ini` y `mysql-service.ini` bajo `InstallRoot\config`.
  - `start_backend_detached.ps1` generaba logs bajo `InstallRoot\logs`.
  - `install_backend_service.ps1` generaba archivos de servicio y logs bajo `InstallRoot`.
- En una instalacion real, Windows bloqueaba esas escrituras y el frontend quedaba sin backend operativo.

## Correccion aplicada
- Se movio el runtime mutable a `C:\ProgramData\CostanorteLocal`.
- Config MySQL generada:
  - `C:\ProgramData\CostanorteLocal\config\mysql-initialize.ini`
  - `C:\ProgramData\CostanorteLocal\config\mysql-service.ini`
- Logs backend:
  - `C:\ProgramData\CostanorteLocal\logs\backend-detached\`
  - `C:\ProgramData\CostanorteLocal\logs\backend-service\`
- Runtime MySQL:
  - `C:\ProgramData\CostanorteLocal\mysql\`
- Runtime de servicio WinSW:
  - `C:\ProgramData\CostanorteLocal\service\`
- El instalador ahora deja ejecutado `Instalar stack local` por defecto al finalizar.

## Prueba controlada realizada
1. Se verifico que no hubiera procesos escuchando en `8080` ni `3307`.
2. Se recompilo el instalador desde `dist/windows-local/output/costanorte-local-installer.exe`.
3. Se instalo una copia de prueba en `C:\Program Files (x86)\Costanorte Local Test`.
4. Se levanto el stack usando los scripts instalados, no el IDE.
5. Se valido `GET /api/v1/system/health`.
6. Se valido `POST /api/v1/auth/login` con usuario demo.
7. Se confirmo que el proceso Java activo saliera de `runtime\jre` de la instalacion.
8. Se confirmo que MySQL y logs escribieran en `C:\ProgramData\CostanorteLocal`.

## Resultado final validado
- Instalador compilado correctamente.
- Instalacion real en `Program Files (x86)` funcionando.
- Backend `UP` en `http://127.0.0.1:8080/api/v1/system/health`.
- Login demo correcto con:
  - usuario `operador.demo`
  - contrasena `Costanorte2026!`
- Java embebido usado desde la instalacion.
- MySQL embebido usado desde la instalacion.
- Sin dependencia de IDE ni de Java/MySQL externos.

## Artefacto validado
- `dist/windows-local/output/costanorte-local-installer.exe`

## Criterio operativo final
- La entrega correcta para colega es el instalador generado desde `dist/windows-local/output/`.
- Si se reinstala en una PC limpia, la prueba minima obligatoria es:
  1. instalar como administrador
  2. validar `health`
  3. validar login demo
  4. abrir `costanorte.exe`

## Riesgos residuales
- Si `8080` o `3307` ya estan ocupados por otro proceso local, el stack puede no levantar correctamente.
- Si una politica corporativa bloquea PowerShell, `java.exe` o `mysqld.exe`, la provision automatica puede fallar.
- Si se distribuye una version vieja del instalador, puede reaparecer el problema historico de permisos/escritura.
