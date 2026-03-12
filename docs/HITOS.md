# Hitos del proyecto QUEDRAS

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
Sin remotos Git definidos, convencion de ramas pendiente y test backend fallando por datasource no configurado.
Estado:
En progreso (backend desbloqueado, pendiente cierre documental final del hito).

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
Pendiente.

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
Pendiente.

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
Pendiente.

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
Pendiente.

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
Pendiente.

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
Pendiente.

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
Pendiente.

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
Pendiente.

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
Pendiente.
