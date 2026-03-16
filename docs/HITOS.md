# Hitos del proyecto COSTANORTE

## Hito 1 - Inicializacion y orden del proyecto
Objetivo:
Dejar estructura de proyectos auditada, tablero inicial operativo y documentacion base creada.
Alcance:
Revision de backend/frontend existentes, creacion de archivos de control y definicion de plan de hitos.
Backend:
Crear carpeta `docs` y archivos de gobierno tecnico del proyecto.
Frontend:
Crear carpeta `docs` y archivos de seguimiento especificos de cliente.
Criterios de validacion:
Documentacion base disponible, estructura revisada y tests de smoke ejecutables sin cambios funcionales.
Riesgos:
Convencion de ramas pendiente y naming entre proyectos a unificar (`quedras`/`quadras`).
Estado:
Completado.

## Hito 11 - Renombre seguro de QUEDRAS a COSTANORTE (fase 1)
Objetivo:
Actualizar la identidad del sistema a COSTANORTE sin romper la operacion actual.
Alcance:
Renombre visible y de configuracion (backend + frontend) con compatibilidad temporal para variables y artefactos previos.
Backend:
Actualizar `spring.application.name`, estado de servicio tecnico, variables de entorno (`COSTANORTE_*` con fallback a `QUADRAS_*`) y metadatos de artefacto Maven.
Frontend:
Actualizar nombre de app, variable de endpoint (`COSTANORTE_API_BASE_URL` con fallback) y binario desktop Windows.
Criterios de validacion:
Backend y frontend compilan, pruebas en verde y build release Windows generado con el nuevo nombre.
Riesgos:
Referencias historicas en documentacion y automatizaciones externas que aun dependan de nombres legacy.
Estado:
Completado.

## Hito 12 - Seguridad de usuarios con JWT y rol inicial
Objetivo:
Incorporar autenticacion de usuarios y autorizacion por rol para proteger la API backend.
Alcance:
Spring Security con JWT firmado, validacion de rol embebido en el token, un rol inicial y usuario demo operativo/documentado para pruebas.
Backend:
Tabla de usuarios, seed de usuario demo, endpoint de login, endpoints protegidos por rol, manejo uniforme de errores 401/403 y adaptacion de smoke tests.
Frontend:
Login operativo, sesion JWT en memoria, guard de rutas, logout y consumo autenticado del modulo de reservas.
Criterios de validacion:
`mvnw test`, `mvnw -DskipTests package`, `backend_smoke_local.ps1`, `flutter test` y `flutter analyze` deben validar login JWT y acceso autenticado a endpoints protegidos.
Riesgos:
Gestion local de secreto JWT, futura estrategia de expiracion/renovacion y ampliacion ordenada a multiples roles.
Estado:
Completado.

## Fase posterior a Hito 12 - Reenfoque comercial del frontend
Objetivo:
Reorientar la experiencia visible del cliente a servicios comerciales del hotel sin perder el layout base ya validado.
Alcance:
Reducir la navegacion visible a `Massagens`, `Quadras`, `Tours e Viagens` y `Configuracoes`, normalizar textos visibles a `pt-BR` y retirar contenido tecnico expuesto al operador.
Backend:
Sin cambios funcionales en esta fase; queda pendiente definir contratos dedicados para `Massagens` y `Tours e Viagens`, y evaluar normalizacion `pt-BR` extremo a extremo en mensajes de API.
Frontend:
Shell actualizado, `Quadras` unificado como modulo operativo, nuevas pantallas base para `Massagens` y `Tours e Viagens`, y textos visibles en portugues de Brasil.
Criterios de validacion:
`flutter analyze` y `flutter test` en verde con navegacion acotada al nuevo alcance visible.
Riesgos:
Falta de contrato backend para los nuevos modulos comerciales y necesidad de mantener consistencia de idioma entre UI y respuestas reales de API.
Estado:
En progreso (frontend base completada, backend pendiente).

## Hito 2 - Configuracion base backend Spring Boot + MySQL + estructura de capas
Objetivo:
Configurar backend para operar en red local con MySQL y base de capas clara.
Alcance:
Properties por entorno, estructura `controller/service/repository/domain`, salud basica y conectividad DB local.
Backend:
Configuracion de datasource, Flyway inicial, paquetes base y manejo de errores base.
Frontend:
N/A.
Criterios de validacion:
Backend levanta local, conecta a MySQL y pasa tests base.
Riesgos:
Dependencia de configuracion local de MySQL por ambiente.
Estado:
Completado.

## Hito 3 - Configuracion base frontend Flutter Desktop + estructura del cliente
Objetivo:
Definir shell de aplicacion desktop y arquitectura interna de UI.
Alcance:
Estructura por modulos/paginas/servicios y tema visual inicial orientado a operacion hotel.
Backend:
N/A.
Frontend:
Scaffold principal, rutas base, cliente HTTP desacoplado.
Criterios de validacion:
App desktop inicia sin errores y con estructura preparada para integracion.
Riesgos:
Diferencias de entorno Flutter/Windows entre estaciones de trabajo.
Estado:
Completado.

## Hito 4 - Modelo de dominio de reservas
Objetivo:
Formalizar entidades de negocio para reservas de cancha.
Alcance:
Modelo de reserva, estados permitidos y reglas de horario a nivel de dominio.
Backend:
Entidades JPA, migraciones Flyway y DTOs base.
Frontend:
Modelos cliente y contratos de serializacion.
Criterios de validacion:
Modelo consistente entre backend y frontend con pruebas de serializacion.
Riesgos:
Cambios tardios en reglas operativas del hotel.
Estado:
Completado.

## Hito 5 - API backend de reservas
Objetivo:
Exponer API REST para crear/listar/consultar reservas.
Alcance:
Endpoints CRUD iniciales, validaciones de entrada y codigos HTTP consistentes.
Backend:
Controladores, servicios, repositorios y pruebas de integracion.
Frontend:
N/A.
Criterios de validacion:
Coleccion de pruebas backend cubre flujo principal y casos invalidos.
Riesgos:
Contratos API no alineados con necesidades de UI.
Estado:
Completado.

## Hito 6 - Pantallas base de agenda y creacion de reserva
Objetivo:
Construir experiencia inicial de uso para agenda y alta.
Alcance:
Pantalla de agenda diaria y formulario de nueva reserva.
Backend:
N/A.
Frontend:
Vistas, validaciones de formulario y estados de carga/error locales.
Criterios de validacion:
Flujo UI completo usando datos mock o backend local segun disponibilidad.
Riesgos:
Ajustes de UX por operativa real del personal del hotel.
Estado:
Completado.

## Hito 7 - Validacion de solapamientos y reglas de negocio
Objetivo:
Asegurar integridad de reservas sin conflictos horarios.
Alcance:
Reglas de solapamiento, horario operativo y duraciones permitidas.
Backend:
Validaciones de negocio y pruebas de borde.
Frontend:
Mensajes de error alineados con reglas backend.
Criterios de validacion:
No se permiten reservas superpuestas y se cubren casos limites.
Riesgos:
Reglas de negocio ambiguas sin politicas cerradas por operacion.
Estado:
Completado.

## Hito 8 - Edicion y cancelacion de reservas
Objetivo:
Permitir mantenimiento completo del ciclo de vida de reservas.
Alcance:
Modificar horarios y cancelar reservas existentes.
Backend:
Endpoints de actualizacion/cancelacion y control de consistencia.
Frontend:
Flujos de edicion/cancelacion desde agenda.
Criterios de validacion:
Cambios reflejados correctamente en UI y persistencia.
Riesgos:
Conflictos al editar hacia franjas ocupadas.
Estado:
Completado.

## Hito 9 - Conexion frontend-backend local
Objetivo:
Integrar cliente Flutter con API local de backend.
Alcance:
Cliente HTTP real, manejo de disponibilidad de servidor y errores de red local.
Backend:
Ajustes CORS/local network si aplica, estabilidad de endpoints.
Frontend:
Integracion de servicios remotos y refresco de datos en agenda.
Criterios de validacion:
Flujos principales operan en red local sin dependencia de internet.
Riesgos:
Configuracion de firewall o puertos en equipos del hotel.
Estado:
Completado.

## Hito 10 - Validacion integral, documentacion final y preparacion para instalacion
Objetivo:
Cerrar version instalable para uso interno del hotel.
Alcance:
Pruebas integrales, manual tecnico y checklist de instalacion local.
Backend:
Ajustes finales de configuracion y guia de despliegue.
Frontend:
Build desktop y guia de ejecucion operativa.
Criterios de validacion:
Checklist de aceptacion completo y ejecucion estable en entorno objetivo.
Riesgos:
Desvios de infraestructura entre entorno de desarrollo e instalacion final.
Estado:
Completado.
