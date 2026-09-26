# ACONDICIONAMIENTO COMO API REST

## Alcance

Esta fase reorganiza el repositorio alrededor de una API REST local
independiente. Por decision del proyecto se conservan las credenciales y los
valores predeterminados actuales para desarrollo local.

## Cambios realizados

Se retiro `installer/windows-local`, que construia un producto completo con
Inno Setup y mezclaba frontend, backend, Java y MySQL.

Tambien se retiraron los scripts destinados a compilar/copiar Flutter,
empaquetar Windows, instalar WinSW, provisionar MySQL portable, administrar el
stack empaquetado y validar ese instalador.

Se conservaron las herramientas coherentes con una API local: configuracion de
un MySQL existente, smoke test HTTP, instancia temporal de prueba y carga de
datos por endpoints REST.

La planilla historica de agendamientos se retiro del backend porque los datos
operativos deben almacenarse fuera del codigo fuente.

Los scripts de prueba ahora usan como fallback la misma clave del operador demo
de `application.properties`: `123456`.

## Elementos deliberadamente conservados

En esta fase no se modificaron las credenciales locales, secreto JWT local,
usuarios bootstrap, perfil `local`, CORS, configuracion Railway/Docker ni los
contratos y reglas de negocio.
