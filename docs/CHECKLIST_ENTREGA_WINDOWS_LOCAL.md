# CHECKLIST ENTREGA WINDOWS LOCAL

## Antes de compilar
- Confirmar que el frontend release Windows esta actualizado.
- Confirmar que el backend `jar` esta actualizado.
- Confirmar que el bundle incluye `runtime/jre`.
- Confirmar que el bundle incluye `runtime/mysql`.
- Confirmar credenciales demo vigentes: `operador.demo / Costanorte2026!`.
- Confirmar que la URL del front apunta a `http://127.0.0.1:8080/api/v1`.

## Build
- Ejecutar `.\scripts\package_windows_local.ps1`.
- Ejecutar `.\scripts\build_windows_installer.ps1 -BundleDir ".\dist\windows-local" -IsccPath "C:\Users\rtala\AppData\Local\Programs\Inno Setup 6\ISCC.exe"`.
- Verificar que exista `dist\windows-local\output\costanorte-local-installer.exe`.

## Smoke
- Instalar el `.exe` en una maquina o carpeta limpia.
- Confirmar que el instalador ejecute `Instalar stack local` al finalizar.
- Verificar que la config/runtime mutable quede en `C:\ProgramData\CostanorteLocal`.
- Ejecutar el smoke del paquete o validacion equivalente sobre la instalacion real.
- Verificar `GET /api/v1/system/health = UP`.
- Verificar login demo correcto.
- Verificar apertura de `costanorte.exe`.

## Entrega
- Entregar `costanorte-local-installer.exe`.
- Entregar la guia [GUIA_COLEGA_WINDOWS.md](C:/Users/Public/Documents/Proyectos/quadras/installer/windows-local/GUIA_COLEGA_WINDOWS.md).
- Informar usuario demo y contrasena demo.
- Informar que, si algo falla, primero debe ejecutar `Instalar stack local`.
- Informar que logs y config de runtime quedan en `C:\ProgramData\CostanorteLocal`.
