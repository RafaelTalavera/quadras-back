# GUIA RAPIDA COSTANORTE

## Instalacion
1. Ejecutar `costanorte-local-installer.exe` como administrador.
2. Completar la instalacion.
3. Al finalizar, el instalador provisiona la base local y el backend.
4. Si la app no se abre sola, abrir desde el acceso directo `COSTANORTE`.

## Requisitos externos
- Este paquete debe incluir su propio Java y su propia base local.
- No hace falta abrir el backend desde IDE.
- No hace falta instalar MySQL aparte.

## Primer ingreso
- Usuario demo: `operador.demo`
- Contrasena demo: `Costanorte2026!`

## Que instala este paquete
- Aplicacion Windows `COSTANORTE`
- Backend local en `127.0.0.1:8080`
- Base MySQL local del paquete en `127.0.0.1:3307`

## Uso diario
- Para abrir la app: acceso directo `COSTANORTE`
- Si la app no conecta, ejecutar desde el menu Inicio: `COSTANORTE > Instalar stack local`
- Para apagar el entorno local: `COSTANORTE > Detener stack local`

## Verificacion rapida
Si hace falta validar que el backend quedo arriba:

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/system/health" -Method Get
```

Respuesta esperada:

```json
{
  "status": "UP"
}
```

## Soporte rapido
- Si Windows pregunta por permisos, aceptar ejecucion como administrador.
- Si ya habia una version previa, desinstalar `COSTANORTE Local` y volver a instalar.
- Si la app abre pero no inicia sesion, ejecutar `Instalar stack local` y reintentar.
- Si sigue fallando, enviar la carpeta `C:\ProgramData\CostanorteLocal\logs`.
