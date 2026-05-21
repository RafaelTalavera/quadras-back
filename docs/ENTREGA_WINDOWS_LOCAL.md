# ENTREGA WINDOWS LOCAL

## Objetivo
Armar una entrega para un colega donde una sola instalacion deje disponible:
- frontend Windows `Flutter`
- backend `Spring Boot`
- base `MySQL`

La operacion objetivo es local, con backend y base en la misma PC y frontend apuntando a `http://127.0.0.1:8080/api/v1`.

## Estado tecnico confirmado
- Backend: `Spring Boot 4`, `Java 17`, `MySQL`, `Flyway`.
- Frontend: `Flutter Windows`, binario `costanorte.exe`.
- Validacion actual:
  - `flutter analyze` OK
  - `flutter test` OK
  - `.\mvnw test` OK

## Base de empaquetado agregada
- Script de armado del bundle: [scripts/package_windows_local.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/package_windows_local.ps1)
- Script de configuracion MySQL: [scripts/configure_local_mysql.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/configure_local_mysql.ps1)
- Script de provision de MySQL portable: [scripts/provision_portable_mysql.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/provision_portable_mysql.ps1)
- Script orquestador de pila local: [scripts/install_local_stack.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/install_local_stack.ps1)
- Script de parada de pila local: [scripts/stop_local_stack.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/stop_local_stack.ps1)
- Script de smoke del bundle: [scripts/smoke_windows_local_bundle.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/smoke_windows_local_bundle.ps1)
- Script de backend desacoplado: [scripts/start_backend_detached.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/start_backend_detached.ps1)
- Script de parada de backend desacoplado: [scripts/stop_backend_detached.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/stop_backend_detached.ps1)
- Script de parada de MySQL portable: [scripts/stop_portable_mysql.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/stop_portable_mysql.ps1)
- Script de arranque por consola: [scripts/start_backend_console.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/start_backend_console.ps1)
- Script de instalacion de servicio con WinSW: [scripts/install_backend_service.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/install_backend_service.ps1)
- Helper para compilar Inno Setup: [scripts/build_windows_installer.ps1](C:/Users/Public/Documents/Proyectos/quadras/scripts/build_windows_installer.ps1)
- Config local externa de Spring: [installer/windows-local/config/application-local.properties](C:/Users/Public/Documents/Proyectos/quadras/installer/windows-local/config/application-local.properties)
- Base de instalador Inno Setup: [installer/windows-local/costanorte-local.iss](C:/Users/Public/Documents/Proyectos/quadras/installer/windows-local/costanorte-local.iss)
- Guia para colega: [installer/windows-local/GUIA_COLEGA_WINDOWS.md](C:/Users/Public/Documents/Proyectos/quadras/installer/windows-local/GUIA_COLEGA_WINDOWS.md)
- Checklist de publicacion: [docs/CHECKLIST_ENTREGA_WINDOWS_LOCAL.md](C:/Users/Public/Documents/Proyectos/quadras/docs/CHECKLIST_ENTREGA_WINDOWS_LOCAL.md)
- Validacion final de instalador real: [docs/VALIDACION_INSTALADOR_WINDOWS_LOCAL.md](C:/Users/Public/Documents/Proyectos/quadras/docs/VALIDACION_INSTALADOR_WINDOWS_LOCAL.md)

## Flujo de armado
```powershell
cd C:\Users\Public\Documents\Proyectos\quadras

.\scripts\package_windows_local.ps1 `
  -FrontendRepoPath "C:\Users\Public\Documents\Proyectos\quedras-front" `
  -JavaHome "C:\runtimes\jdk-17" `
  -MySqlServerDir "C:\mysql\mysql-8.0.43-winx64" `
  -WinSwPath "C:\tools\WinSW-x64.exe" `
  -DatabaseDumpPath "C:\respaldos\baseline.sql"
```

Salida esperada:
- `dist/windows-local/app/backend/costanorte.jar`
- `dist/windows-local/app/frontend/costanorte.exe`
- `dist/windows-local/config/application-local.properties`
- `dist/windows-local/runtime/mysql/...` si se paso `-MySqlServerDir`
- `dist/windows-local/costanorte-local.iss`

## Flujo en maquina destino
1. Instalar el paquete.
2. El instalador debe provisionar la base local y el backend al finalizar.
3. Si hiciera falta reintentar manualmente, ejecutar `Instalar stack local` o correr:
```powershell
.\scripts\install_local_stack.ps1
```
4. Si no se incluyo MySQL portable, usar modo alternativo con MySQL externo:
```powershell
.\scripts\configure_local_mysql.ps1 `
  -MySqlBinDir "C:\Program Files\MySQL\MySQL Server 8.0\bin" `
  -AdminUser "root" `
  -AdminPassword "TU_PASSWORD_ROOT" `
  -DumpPath ".\database\seed\baseline.sql"
```
5. Si tampoco se incluyo `WinSW`, arrancar backend por consola o desacoplado:
```powershell
.\scripts\start_backend_console.ps1
```
o
```powershell
.\scripts\start_backend_detached.ps1
```
6. Validar salud:
```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/system/health" -Method Get
```
7. Validar bundle completo con usuario demo:
```powershell
.\scripts\smoke_windows_local_bundle.ps1
```
8. Abrir `costanorte.exe`.
9. Para apagar el entorno local:
```powershell
.\scripts\stop_local_stack.ps1
```

## Runtime mutable
- La instalacion deja binarios y frontend dentro de `Program Files`.
- Los archivos mutables de runtime se generan en `C:\ProgramData\CostanorteLocal`.
- Rutas importantes:
  - `C:\ProgramData\CostanorteLocal\config\mysql-initialize.ini`
  - `C:\ProgramData\CostanorteLocal\config\mysql-service.ini`
  - `C:\ProgramData\CostanorteLocal\logs\backend-detached\`
  - `C:\ProgramData\CostanorteLocal\logs\backend-service\`
  - `C:\ProgramData\CostanorteLocal\mysql\`

## Compilacion del instalador
Si el bundle ya esta armado y la maquina tiene Inno Setup:
```powershell
.\scripts\build_windows_installer.ps1 -BundleDir ".\dist\windows-local"
```

Artefacto final validado:
- `dist/windows-local/output/costanorte-local-installer.exe`

Credenciales demo confirmadas:
- Usuario: `operador.demo`
- Contrasena: `Costanorte2026!`

## Riesgos abiertos
- El modo completamente autonomo depende de incluir un `MySQL` noinstall ZIP ya extraido en `-MySqlServerDir`.
- `MySQL` se provisiona en puerto `3307` para no chocar con instalaciones existentes en `3306`.
- Si no se incluye `WinSW`, el backend ahora puede quedar operativo igual mediante proceso desacoplado.
- La URL del frontend sigue siendo de compilacion; si cambia host o puerto, hay que regenerar el release.
- Si `8080` o `3307` ya estan ocupados por otro proceso local, la provision automatica puede fallar.
- Si una politica corporativa bloquea PowerShell, `java.exe` o `mysqld.exe`, la provision automatica puede fallar.

## Estado actual
1. Bundle validado con Java embebido, MySQL portable y backend local.
2. Instalador `.exe` compilado y probado sobre instalacion real en `Program Files (x86)`.
3. `health` y login demo validados sobre instalacion real sin IDE ni Java externo.
4. Runtime mutable movido a `C:\ProgramData\CostanorteLocal` para evitar fallos de permisos en `Program Files`.
