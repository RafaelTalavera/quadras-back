# API REST LOCAL

## Objetivo

`quadras` es un backend independiente. Expone contratos HTTP/JSON y persiste
los datos en MySQL o MariaDB. Ningun cliente visual forma parte de su proceso de
compilacion, ejecucion o distribucion.

```text
quedras-front (Flutter)
        | HTTP/JSON + JWT
        v
quadras (Spring Boot /api/v1)
        | JPA + Flyway
        v
MySQL o MariaDB
```

## Limites de responsabilidad

La API administra autenticacion y autorizacion JWT, validaciones, reglas de
negocio, persistencia, migraciones, errores HTTP y sincronizacion SSE.

La API no compila o instala Flutter, no crea accesos directos de Windows, no
distribuye una JVM, no instala MySQL portable y no publica el frontend.

## Ejecucion

El perfil predeterminado sigue siendo `local` para preservar el flujo y las
credenciales actuales.

```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
```

URL base en el mismo equipo:

```text
http://127.0.0.1:8080/api/v1
```

Desde otra computadora de la red se debe usar la IP o DNS del equipo donde se
ejecuta la API, por ejemplo `http://192.168.1.50:8080/api/v1`. `127.0.0.1` solo
funciona en el equipo del backend. La red y el firewall deben permitir TCP
entrante al puerto configurado.

## Endpoints base

- `GET /api/v1/system/health`: estado, sin autenticacion.
- `POST /api/v1/auth/login`: autenticacion.
- `/api/v1/reservations/**`: reservas.
- `/api/v1/courts/**`: canchas.
- `/api/v1/massages/**`: masajes.
- `/api/v1/tours/**`: tours.
- `/api/v1/maintenance/**`: mantenimiento.
- `/api/v1/users/**`: usuarios.
- `GET /api/v1/sync/events`: sincronizacion SSE.

Salvo health y login, las operaciones requieren `Authorization: Bearer <JWT>`.

### Acceso de proveedores

Los usuarios con rol `PROVIDER` incluyen el mismo objeto `providerAccess` en la
respuesta de `POST /api/v1/auth/login`, `GET /api/v1/users/me` y los contratos de
administración de usuarios:

```json
{
  "type": "COURTS",
  "resourceId": 17,
  "displayName": "Joao Silva"
}
```

`type` admite `COURTS`, `MASSAGE`, `MAINTENANCE` y `LAUNDRY`. Para crear o
actualizar una cuenta se envían `providerType` y `providerResourceId`; ambos son
obligatorios con rol `PROVIDER` y se rechazan para roles internos. El recurso
debe existir y estar activo; mantenimiento exige además un proveedor
`EXTERNAL`.

Durante la transición, `laundryProviderId` sigue aceptándose y devolviéndose
para lavandería. Si se envía junto al contrato genérico, debe coincidir con un
acceso `LAUNDRY` al mismo identificador.

El JWT transporta `providerType` y `providerResourceId`, además de `role` y
`securityVersion`. Para lavandería también conserva temporalmente
`laundryProviderId`. Todos estos datos se contrastan contra el usuario
persistido en cada petición autenticada.

#### Autoservicio de canchas

Las cuentas `PROVIDER` con `providerAccess.type = COURTS` disponen de estas
operaciones de solo lectura:

- `GET /api/v1/provider/courts/bookings`: agenda propia.
- `GET /api/v1/provider/courts/bookings/{bookingId}`: detalle de una reserva
  propia.

El listado acepta `bookingDate` o el par `dateFrom` + `dateTo`, y opcionalmente
`status` (`SCHEDULED` o `CANCELLED`). `bookingDate` no puede combinarse con el
rango; el rango exige ambos extremos y admite como maximo 31 dias inclusivos.
Sin fechas se devuelve el dia actual. El orden es estable por fecha, hora de
inicio e identificador.

El backend obtiene el profesor exclusivamente del JWT validado y filtra el
ownership dentro de la consulta; no acepta un identificador de profesor enviado
por el cliente. Un detalle ajeno responde `404` para no revelar su existencia.
Una asociacion inactiva o un tipo de proveedor diferente responde `403`.

El contrato externo devuelve `id`, fechas y horas, duracion, nombre y referencia
del cliente, estado, observacion de cancelacion y datos de recurrencia. No expone
WhatsApp, importes, pagos, materiales ni metadatos internos de auditoria. B4.2 no
habilita creacion, edicion, pago o cancelacion por parte del profesor.

#### Autoservicio de masajes

Las cuentas `PROVIDER` con `providerAccess.type = MASSAGE` disponen de estas
operaciones de solo lectura:

- `GET /api/v1/provider/massages/bookings`: agenda propia.
- `GET /api/v1/provider/massages/bookings/{bookingId}`: detalle de una reserva
  propia.

Los filtros y limites temporales son los mismos del autoservicio de canchas:
`bookingDate` o `dateFrom` + `dateTo`, rango inclusivo maximo de 31 dias,
`status` opcional y dia actual por defecto. El orden es fecha, hora e
identificador.

El ownership se filtra por el `MassageTherapist` autenticado dentro de la
consulta. Un detalle ajeno responde `404`; terapeuta o empresa inactivos y tipos
de proveedor diferentes responden `403`. El contrato devuelve fecha, hora,
cliente, referencia, tratamiento, estado y observacion de cancelacion. No expone
importe, pago, datos de la empresa ni auditoria interna, y B4.3 no habilita
mutaciones del masajista.

#### Autoservicio de mantenimiento

Las cuentas `PROVIDER` con `providerAccess.type = MAINTENANCE`, asociadas a un
proveedor externo activo, disponen de estas operaciones sobre sus propias
ordenes:

- `GET /api/v1/provider/maintenance/orders`: listado filtrado.
- `GET /api/v1/provider/maintenance/orders/{orderId}`: detalle.
- `PATCH /api/v1/provider/maintenance/orders/{orderId}/start`: inicio.
- `GET /api/v1/provider/maintenance/orders/{orderId}/attachments`: metadatos de
  evidencias.
- `POST /api/v1/provider/maintenance/orders/{orderId}/attachments`: carga de
  evidencia con el contrato `AddMaintenanceAttachmentDto`.
- `PATCH /api/v1/provider/maintenance/orders/{orderId}/complete`: finalizacion
  con `resolutionNotes` obligatorio.

El listado acepta el par inclusivo `dateFrom` + `dateTo`, con un maximo de 31
dias, y `status` opcional. Sin fechas devuelve el dia actual. Para ordenes
programadas filtra por inicio programado; para el backlog sin programacion usa
la fecha de reporte. El orden es estable: primero programadas, luego backlog, y
finalmente fecha e identificador.

El identificador del proveedor se obtiene del JWT validado y todas las lecturas
y mutaciones consultan por `orderId + providerId`; una orden ajena responde
`404`. Un proveedor inactivo, interno o de otro tipo responde `403`. El contrato
externo omite pagos, datos del solicitante, asignaciones internas y auditoria.
Los metadatos de evidencia tampoco exponen el usuario interno `createdBy`.

No se habilitan creacion, edicion general, reasignacion, reprogramacion,
cancelacion, pago, auditoria ni eliminacion de anexos para el proveedor externo.

## Base de datos

La API usa una instancia existente de MySQL o MariaDB. Flyway actualiza el
esquema al arrancar. `configure_local_mysql.ps1` es una ayuda opcional para
preparar MySQL; no instala el motor.

## Frontend

El frontend se compila y publica desde `quedras-front` y debe configurarse con
`COSTANORTE_API_BASE_URL=http://<host-api>:8080/api/v1`. Nunca debe recibir las
credenciales de la base de datos.
