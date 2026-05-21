# COSTANORTE Windows Local Bundle

Este directorio define el bundle base para distribuir `frontend + backend + base de datos` en una PC Windows.

## Estado actual
- El bundle se arma desde `scripts/package_windows_local.ps1`.
- El frontend se distribuye como release Windows ya compilado.
- El backend se distribuye como `costanorte.jar`.
- La configuracion local de Spring sale desde `config/application-local.properties`.
- El bundle debe incluir `runtime/jre` para no depender de Java instalado en la PC destino.
- El modo recomendado ahora es un `MySQL` portable del propio bundle, aislado en puerto `3307`.
- Los archivos mutables de runtime se generan en `C:\ProgramData\CostanorteLocal` para no depender de permisos de escritura en `Program Files`.
- La base puede inicializarse con `scripts/provision_portable_mysql.ps1`.
- Sigue existiendo `scripts/configure_local_mysql.ps1` como alternativa para un MySQL externo.
- Si se agrega `WinSW-x64.exe`, el backend puede registrarse como servicio con `scripts/install_backend_service.ps1`.
- Si se agrega `runtime/mysql` y `WinSW`, `scripts/install_local_stack.ps1` puede provisionar la pila completa.
- Si no hay wrapper de servicio, el backend igual puede arrancar por consola con `scripts/start_backend_console.ps1`.
- El instalador final validado sale en `dist/windows-local/output/costanorte-local-installer.exe`.

## Estructura esperada del bundle generado
- `app/backend/costanorte.jar`
- `app/frontend/costanorte.exe`
- `config/application-local.properties`
- `C:\ProgramData\CostanorteLocal\config\mysql-service.ini` generado en instalacion
- `scripts/configure_local_mysql.ps1`
- `scripts/provision_portable_mysql.ps1`
- `scripts/install_local_stack.ps1`
- `scripts/stop_local_stack.ps1`
- `scripts/smoke_windows_local_bundle.ps1`
- `scripts/start_backend_detached.ps1`
- `scripts/start_backend_console.ps1`
- `scripts/stop_backend_detached.ps1`
- `scripts/stop_portable_mysql.ps1`
- `scripts/install_backend_service.ps1`
- `scripts/build_windows_installer.ps1`
- `support/README.md`
- `support/GUIA_COLEGA_WINDOWS.md`
- `tools/winsw/WinSW-x64.exe` opcional
- `runtime/mysql/` opcional y recomendado
- `runtime/jre/` opcional
- `database/seed/baseline.sql` opcional

## Flujo recomendado
1. Construir el bundle con `scripts/package_windows_local.ps1`.
2. Revisar `config/application-local.properties`.
3. Si se quiere una base inicial real, incluir `baseline.sql`.
4. Si se quiere instalacion autonoma, incluir `runtime/mysql` a partir de un MySQL noinstall ZIP extraido.
5. En la maquina destino, el instalador ejecuta `scripts/install_local_stack.ps1` al finalizar.
6. Abrir `costanorte.exe`.

## Operacion
- Para detener la pila local del bundle: `scripts/stop_local_stack.ps1`
- Para validar arranque, `health` y login demo en una sola ejecucion: `scripts/smoke_windows_local_bundle.ps1`
- Para detener solo backend detached: `scripts/stop_backend_detached.ps1`
- Para detener solo MySQL portable: `scripts/stop_portable_mysql.ps1`
- Para compilar el instalador desde el bundle, si Inno Setup esta instalado: `scripts/build_windows_installer.ps1`
- La desinstalacion del instalador intenta detener la pila y borrar `C:\ProgramData\CostanorteLocal\logs`, los `mysql-*.ini` generados, `C:\ProgramData\CostanorteLocal\mysql` y `C:\ProgramData\CostanorteLocal\service`

## Entrega recomendada
- Entregar `dist/windows-local/output/costanorte-local-installer.exe`
- Entregar `support/GUIA_COLEGA_WINDOWS.md`
- Informar credenciales demo: `operador.demo / Costanorte2026!`

## Nota
El archivo `costanorte-local.iss` ya puede invocar `scripts/install_local_stack.ps1` al terminar la instalacion. Para que la provision de base sea completamente automatica, el bundle debe incluir `runtime/mysql`.
