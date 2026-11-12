# Plan de acceso para proveedores externos

## 0. Registro de avance

Actualizado: 2026-11-12.

- [x] **B1.1 — Tipo de acceso.** Creado `ProviderAccessType` con `COURTS`,
  `MASSAGE`, `MAINTENANCE` y `LAUNDRY`.
- [x] **B1.2 — Esquema aditivo y compatibilidad de lavandería.** La migración
  `V48__generalize_provider_user_associations.sql` agrega `provider_type` y las
  claves foráneas explícitas de canchas, masajes y mantenimiento, conserva
  `laundry_provider_id`, migra los usuarios `PROVIDER` existentes a `LAUNDRY` y
  exige mediante `CHECK` exactamente un recurso acorde al tipo. Los índices de
  las nuevas asociaciones quedaron creados.
- [x] **B1.3 — Mapeo JPA inicial.** `AppUser` mapea el tipo y las cuatro
  asociaciones sin romper las fábricas ni el contrato legado de lavandería.
- [x] **B1.4 — Validación funcional de asociaciones activas.** La gestión de
  usuarios rechaza profesores, terapeutas y lavanderías inactivos, y exige que
  mantenimiento sea activo y de tipo `EXTERNAL`. El bloqueo operativo ante una
  desactivación posterior se aplicará mediante el guard de B3.
- [x] **B2.1 — Valor de dominio.** Creado `ProviderAccess` como representación
  única e inmutable de tipo, identificador y nombre visible; `AppUser` garantiza
  coherencia entre rol, tipo y exactamente una asociación.
- [x] **B2.2 — Principal y JWT.** El principal expone el acceso genérico; los
  tokens nuevos incluyen `providerType` y `providerResourceId`, y el filtro los
  contrasta contra persistencia. Se conserva `laundryProviderId` y se aceptan
  temporalmente tokens legados de lavandería que no contengan los claims nuevos.
- [x] **B2.3 — Contratos de autenticación y usuarios.** Login, usuario actual y
  administración exponen `providerAccess`. Altas y actualizaciones aceptan
  `providerType` + `providerResourceId`, manteniendo compatibilidad con
  `laundryProviderId` cuando ambos contratos coinciden.
- [x] **B2.4 — Gestión, carga y auditoría.** `UserManagementService` resuelve y
  valida los cuatro recursos, incrementa `securityVersion` mediante el modelo al
  cambiar acceso, y registra tipo e identificador en auditoría. La carga de
  usuarios obtiene las asociaciones con `EntityGraph` para evitar consultas
  adicionales por cada asociación.
- [x] **B2.5 — Documentación REST.** `API_REST_LOCAL.md` documenta enums,
  ejemplos, reglas de entrada, claims y compatibilidad temporal.
- [x] **B3.1 — Guard reutilizable.** `ProviderAccessGuard` exige rol `PROVIDER`,
  tipo compatible, ownership y que el actor de negocio siga activo; para
  mantenimiento también conserva la exigencia de tipo `EXTERNAL`.
- [x] **B3.2 — Consultas acotadas por ownership.** Canchas, masajes,
  mantenimiento y lavandería disponen de búsquedas por identificador del
  recurso y actor asociado. Las reservas de cancha incorporan una clave foránea
  al profesor para no autorizar mediante nombres; la migración V49 recupera las
  asociaciones históricas que coinciden con el catálogo.
- [x] **B3.3 — Rutas externas.** `SecurityConfig` reserva
  `/api/v1/provider/courts/**`, `/api/v1/provider/massages/**` y
  `/api/v1/provider/maintenance/**` para `PROVIDER`; las rutas existentes de
  lavandería mantienen su política compatible.
- [x] **B3.4 — Jerarquía de roles.** Se verificó que `SUPERVISOR` conserva
  `ROLE_OPERATOR` y que `PROVIDER` no recibe autoridades administrativas.
- [x] **B3.5 — Auditoría externa.** Los eventos registran usuario, rol, tipo e
  identificador del proveedor junto con la acción. V49 agrega las columnas e
  índice necesarios sin modificar los eventos históricos.
- [x] **B4.1 — Lavandería consolidada.** Las rutas externas existentes obtienen
  el actor mediante `ProviderAccessGuard` y rechazan proveedores de otro tipo o
  recursos desactivados. Detalles y mutaciones propias consultan por
  `id + laundryProviderId`, también bajo bloqueo pesimista, sin cambiar URLs,
  DTO, estados ni compatibilidad v1/v2.
- [x] **B4.2 — Canchas de solo lectura.** La ruta externa permite listar la
  agenda y consultar el detalle propio mediante proyecciones reducidas filtradas
  por `CourtPartnerCoach`. El contrato excluye contacto, importes, pagos,
  materiales y auditoría interna; aplica orden estable, rango máximo de 31 días,
  bloqueo de actores inactivos y `404` para reservas ajenas. No se habilitaron
  mutaciones mientras sus transiciones funcionales sigan pendientes.
- [x] **B4.3 — Masajes de solo lectura.** La ruta externa permite listar la
  agenda y consultar el detalle propio mediante proyecciones reducidas filtradas
  por `MassageTherapist`. Expone cliente, referencia, tratamiento y estado, pero
  excluye importes, pagos, datos de empresa y auditoría interna. Aplica orden
  estable, rango máximo de 31 días, bloqueo si el terapeuta o su empresa están
  inactivos y `404` para reservas ajenas; las mutaciones siguen deshabilitadas.

Validación ejecutada para los puntos cerrados:

- `./mvnw.cmd -DskipTests compile`: correcta.
- `./mvnw.cmd -Dtest=FlywayReservationMigrationTest test`: correcta; 48
  migraciones aplicadas y 23 pruebas superadas, incluida la aserción específica
  de las cinco columnas de asociación de proveedor.
- `./mvnw.cmd -Dtest=AuthControllerTest,UserManagementControllerTest test`:
  correcta; regresión de autenticación y usuarios sin fallos.
- `./mvnw.cmd test`: correcta; 212 pruebas, 0 fallos y 0 errores. Incluye los
  cuatro tipos, recursos inválidos/inactivos y rechazo de claims de ownership
  manipulados.
- `./mvnw.cmd -DskipTests package`: correcta; artefacto Spring Boot generado.
- `./mvnw.cmd -DskipTests compile`: correcta después de B3.
- `./mvnw.cmd -Dtest=ProviderAccessGuardTest,AuthenticatedUserPrincipalTest,AuditTrailServiceTest,AuthControllerTest,FlywayReservationMigrationTest test`:
  correcta; 41 pruebas, 0 fallos y 0 errores. Incluye las 49 migraciones,
  ownership, bloqueo por recurso inactivo, rutas externas, jerarquía y contexto
  de auditoría.
- `./mvnw.cmd test`: correcta después de B3; 221 pruebas, 0 fallos y 0
  errores. La auditoría conserva el contrato público estricto de lavandería v1.
- `./mvnw.cmd -DskipTests package`: correcta después de B3; artefacto Spring
  Boot generado.
- `./mvnw.cmd clean -Dtest=LaundryDispatchControllerTest,LaundryReturnControllerTest,ProviderAccessGuardTest,LaundryV1CompatibilitySnapshotTest test`:
  correcta para B4.1; 20 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd test`: correcta después de B4.1; 222 pruebas, 0 fallos y 0
  errores. Incluye rechazo de un proveedor `COURTS` en rutas de lavandería.
- `./mvnw.cmd -DskipTests package`: correcta después de B4.1; artefacto Spring
  Boot generado.
- `./mvnw.cmd -Dtest=ProviderCourtBookingControllerTest,AuthControllerTest test`:
  correcta para B4.2; 15 pruebas, 0 fallos y 0 errores. Incluye aislamiento entre
  profesores, detalle propio y ajeno, contrato reducido, rango temporal,
  desactivación inmediata y rechazo por rol o tipo incorrecto.
- `./mvnw.cmd test`: correcta después de B4.2; 226 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd -DskipTests package`: correcta después de B4.2; artefacto Spring
  Boot generado.
- `./mvnw.cmd -Dtest=ProviderMassageBookingControllerTest,ProviderCourtBookingControllerTest,AuthControllerTest test`:
  correcta para B4.3; 19 pruebas, 0 fallos y 0 errores. Incluye aislamiento entre
  terapeutas, detalle propio y ajeno, contrato reducido, rango temporal y
  desactivación inmediata de la empresa.
- [x] **B4.4 — Mantenimiento operativo acotado.** La ruta externa permite listar
  y consultar órdenes propias mediante proyecciones reducidas, iniciar una orden,
  adjuntar evidencia y completarla con notas de resolución. Todas las lecturas y
  mutaciones se acotan por `orderId + MaintenanceProvider`; aplica rango máximo
  de 31 días, orden estable, bloqueo de proveedores inactivos o no externos y
  `404` para órdenes ajenas. Se excluyeron creación, edición general,
  reasignación, reprogramación, cancelación, pagos, auditoría y borrado de
  anexos.
- [x] **B5 — Pruebas y validación backend.** La regresión de seguridad cubre las
  variantes administrativas `GET`, `POST`, `PUT`, `PATCH` y `DELETE`, además de
  invalidar JWT reales al reasignar el actor, cambiar el rol o deshabilitar el
  usuario. El smoke HTTP crea o reutiliza un proveedor de mantenimiento,
  comprueba su consulta propia y confirma `403` sobre la ruta administrativa.
- `./mvnw.cmd test`: correcta después de B4.3; 230 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd -DskipTests package`: correcta después de B4.3; artefacto Spring
  Boot generado.
- `./mvnw.cmd -Dtest=ProviderMaintenanceOrderControllerTest test`: correcta
  para B4.4; 4 pruebas, 0 fallos y 0 errores. Incluye aislamiento entre
  proveedores, contrato reducido, rango temporal, desactivación inmediata y el
  flujo iniciar, adjuntar evidencia y completar.
- `./mvnw.cmd -Dtest=ProviderMaintenanceOrderControllerTest,ProviderMassageBookingControllerTest,ProviderCourtBookingControllerTest,AuthControllerTest test`:
  correcta; 23 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd test`: correcta después de B4.4; 234 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd -DskipTests package`: correcta después de B4.4; artefacto Spring
  Boot generado.
- `./mvnw.cmd -Dtest=AuthControllerTest,UserManagementControllerTest test`:
  correcta para B5; 25 pruebas, 0 fallos y 0 errores.
- `./scripts/backend_smoke_local.ps1 -Port 8092 -Profile smoke -SkipBuild`:
  correcto; autenticación y reserva de operador verificadas, login de proveedor
  `MAINTENANCE`, consulta propia correcta y acceso administrativo rechazado con
  `403`.
- [x] **F1 — Identidad y navegación de proveedor.** El cliente Flutter interpreta
  `providerAccess` con compatibilidad temporal de lavandería, valida asociaciones
  contradictorias y limita rutas, menú y redirección inicial al módulo propio y
  la configuración personal.
- [x] **F2 — Autoservicio COURTS, MASSAGE y MAINTENANCE.** Se implementaron
  modelos y clientes HTTP tipados exclusivos para las rutas externas. Canchas y
  masajes muestran exclusivamente la agenda reducida propia; mantenimiento lista
  órdenes propias y permite iniciar, completar y adjuntar evidencia JPG, PNG o
  PDF. Ningún flujo reutiliza pantallas o servicios administrativos.
- [x] **F3 — Estados y pruebas de autoservicio.** Las tres pantallas representan
  carga, contenido, vacío y error; los clientes conservan `401`/`403` y el cliente
  autorizado mantiene el cierre de sesión ante `401`. Se agregaron pruebas
  focalizadas de serialización, HTTP y widgets para cada tipo.
- [x] **B6 — Cardinalidad de cuentas externas.** La migración
  `V50__enforce_single_provider_access_account.sql` establece una relación 1:1
  entre cada actor y su cuenta `PROVIDER` mediante índices únicos para los
  cuatro tipos. `UserManagementService` rechaza con `409` altas o
  reasignaciones duplicadas, manteniendo la base de datos como protección ante
  concurrencia.
- [x] **F4 — Administración de proveedores para SUPERVISOR.** Configuración
  incorpora un panel exclusivo con búsqueda y categorías para profesores,
  empresas y terapeutas de masajes, proveedores externos de mantenimiento y
  lavanderías. Permite alta, edición, activación/desactivación del actor y un
  flujo separado para crear, activar, desactivar, restablecer o reasignar la
  cuenta `PROVIDER`. Los estados `Sem acesso`, `Usuário ativo` y `Usuário
  desativado` se derivan del catálogo y de `/api/v1/users`; si el alta de la
  cuenta falla, el actor permanece creado y el acceso puede reintentarse sin
  duplicarlo.
- [x] **F5 — Aceptación cross-repo de los cuatro tipos.** El smoke HTTP crea o
  reutiliza una cuenta activa para `COURTS`, `MASSAGE`, `MAINTENANCE` y
  `LAUNDRY`, autentica cada identidad, consulta su autoservicio y exige `403`
  al intentar un módulo incompatible. La regresión Flutter verifica que cada
  sesión ve únicamente su módulo y la configuración personal.
- `./mvnw.cmd -Dtest=LaundryStockControllerTest#shouldTraceMovementsAndRetireItemIdempotently test`:
  correcta; se estabilizó el horario del caso para que su fecha local no cruce
  el límite UTC al ejecutarse después de las 21:00 en `America/Sao_Paulo`.
- `./mvnw.cmd test`: correcta después de B5; 236 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd -DskipTests package`: correcta después de B5; artefacto Spring Boot
  generado.
- `flutter test --no-pub` en `quedras-front`: correcta después de F1-F3; 183
  pruebas, 0 fallos.
- `flutter analyze --no-pub` en `quedras-front`: correcto, sin incidencias.
- `git diff --check` y comprobación de secuencias UTF-8 corruptas en los archivos
  Flutter modificados: correctos.
- `./mvnw.cmd -Dtest=UserManagementControllerTest test`: correcta para la
  cardinalidad 1:1; 14 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd test`: correcta después de B6 y F4; 237 pruebas, 0 fallos y 0
  errores.
- `./mvnw.cmd -DskipTests package`: correcta después de B6 y F4; artefacto
  Spring Boot generado.
- `flutter test` en `quedras-front`: correcta después de F4; 188 pruebas, 0
  fallos.
- Pruebas focalizadas de Configuración > Provedores: 8 pruebas, 0 fallos; cubren
  modelo, serialización, asociación tipada, estados de pantalla y errores HTTP
  `400`, `403`, `409` e inesperados.
- `flutter analyze --no-pub` en `quedras-front`: correcto después de F4, sin
  incidencias.
- `./scripts/backend_smoke_local.ps1 -Port 8093 -Profile smoke -SkipBuild`:
  correcto para F5; los cuatro tipos autenticaron, consultaron su ruta propia y
  recibieron `403` ante un módulo incompatible.
- `./mvnw.cmd test`: correcta después de F5; 237 pruebas, 0 fallos y 0 errores.
- `./mvnw.cmd -DskipTests package`: correcta después de F5; artefacto Spring
  Boot generado.
- `flutter test --no-pub` en `quedras-front`: correcta después de F5; 191
  pruebas, 0 fallos.
- `flutter analyze --no-pub` en `quedras-front`: correcto después de F5, sin
  incidencias.

Cambios locales previos en lavandería detectados y preservados; no forman parte
de este plan.

## 1. Objetivo

Permitir que usuarios externos ingresen al sistema con un único rol de seguridad
`PROVIDER`, vean solamente el módulo que les corresponde y operen únicamente sobre
sus propios datos.

Los tipos iniciales de proveedor serán:

- `COURTS`: profesor de tenis vinculado a `court_partner_coaches`.
- `MASSAGE`: masajista vinculado a `massage_therapists`.
- `MAINTENANCE`: proveedor externo vinculado a `maintenance_providers`.
- `LAUNDRY`: lavandería vinculada a `laundry_providers`.

La implementación se hará primero en el backend. El frontend se adaptará cuando
el contrato del backend esté implementado y probado.

## 2. Estado actual comprobado

El backend ya posee tres roles en `AppUserRole`: `OPERATOR`, `SUPERVISOR` y
`PROVIDER`. Sin embargo, `PROVIDER` está implementado únicamente para lavandería:

- `AppUser` contiene una relación directa `laundryProvider`.
- Los DTO de autenticación y usuarios exponen `laundryProviderId`.
- El JWT contiene `role`, `securityVersion` y, para este rol,
  `laundryProviderId`.
- `JwtAuthenticationFilter` contrasta esos datos contra el estado persistido.
- `SecurityConfig` permite al proveedor solamente un conjunto de operaciones de
  `/api/v2/laundry/**`.
- Los servicios de lavandería filtran por el proveedor autenticado, por lo que ya
  existe un precedente de autorización por ownership.
- La migración `V46__associate_users_with_laundry_providers.sql` exige que todo
  usuario `PROVIDER` tenga `laundry_provider_id`.

Actualmente las rutas de canchas, masajes y mantenimiento requieren autoridad
`OPERATOR`; otorgar esa autoridad a un proveedor externo expondría información y
operaciones administrativas de todo el módulo.

## 3. Decisiones de diseño

### 3.1 Un rol y un tipo de proveedor

Se conservará `AppUserRole.PROVIDER` y se agregará un enum independiente, por
ejemplo `ProviderAccessType`:

```text
COURTS | MASSAGE | MAINTENANCE | LAUNDRY
```

El rol expresa que el usuario es externo. El tipo expresa el módulo al que puede
entrar. No se crearán roles como `LAUNDRY_PROVIDER` o `MASSAGE_PROVIDER`, porque
eso mezcla rol, módulo y reglas de negocio y dificulta agregar nuevos tipos.

### 3.2 Un usuario proveedor pertenece inicialmente a un solo tipo

Para la primera versión, cada usuario `PROVIDER` tendrá exactamente un tipo y un
recurso de negocio asociado. `OPERATOR` y `SUPERVISOR` no tendrán tipo ni vínculo
de proveedor.

Esta restricción coincide con el caso solicitado y mantiene simples el login, la
navegación y la autorización. Si más adelante un mismo usuario debe representar
varios servicios, se evolucionará a asignaciones múltiples sin crear nuevos
roles; no se incorporará esa complejidad en la primera entrega.

### 3.3 Identidad de negocio por módulo

El vínculo debe identificar al actor que realmente trabaja en cada reserva u
orden:

| Tipo | Entidad asociada | Alcance esperado |
| --- | --- | --- |
| `COURTS` | `CourtPartnerCoach` | Reservas asignadas a ese profesor |
| `MASSAGE` | `MassageTherapist` | Reservas asignadas a ese masajista |
| `MAINTENANCE` | `MaintenanceProvider` | Órdenes asignadas a ese proveedor externo |
| `LAUNDRY` | `LaundryProvider` | Documentos, devoluciones y stock de esa lavandería |

En masajes se vinculará al terapeuta, no solamente a la empresa
`MassageProvider`, porque el caso de uso solicitado es el acceso del masajista.

### 3.4 Autorización en dos niveles

Cada operación de proveedor deberá superar dos controles:

1. `role == PROVIDER` y `providerType` compatible con el módulo.
2. El recurso solicitado pertenece al identificador vinculado al usuario.

Ocultar pantallas en el frontend no será un control de seguridad. El backend
devolverá `403 Forbidden` cuando el tipo no tenga acceso y no entregará recursos
ajenos. Para búsquedas y detalles se aplicará el ownership dentro de la consulta
o del servicio, no después de cargar datos sin filtrar.

### 3.5 Endpoints externos separados de los administrativos

Se crearán rutas de autoservicio explícitas por módulo, siguiendo el precedente
de lavandería, en lugar de abrir al proveedor las rutas administrativas actuales:

```text
/api/v1/provider/courts/**
/api/v1/provider/massages/**
/api/v1/provider/maintenance/**
/api/v2/laundry/provider/**       (se conserva inicialmente)
```

Estas rutas devolverán DTO reducidos y no expondrán pagos, reportes globales,
configuración, catálogos administrativos, auditoría interna ni datos de otros
proveedores salvo que un requisito posterior lo autorice expresamente.

## 4. Contrato de autenticación propuesto

### 4.1 Respuesta de login y usuario actual

`POST /api/v1/auth/login` y `GET /api/v1/users/me` incorporarán un objeto
opcional `providerAccess`:

```json
{
  "username": "profesor.joao",
  "role": "PROVIDER",
  "providerAccess": {
    "type": "COURTS",
    "resourceId": 17,
    "displayName": "Joao Silva"
  }
}
```

Para `OPERATOR` y `SUPERVISOR`, `providerAccess` será `null` o estará ausente de
forma consistente según se defina al implementar el contrato.

Durante la transición se conservará `laundryProviderId` en las respuestas y en
el JWT para proveedores de lavandería. Se marcará como campo legado y se retirará
solo después de adaptar y desplegar todos los consumidores.

### 4.2 Gestión de usuarios

Los contratos de creación y actualización de usuarios pasarán de un campo
específico `laundryProviderId` a:

```json
{
  "username": "masajista.maria",
  "password": "********",
  "role": "PROVIDER",
  "providerType": "MASSAGE",
  "providerResourceId": 23,
  "enabled": true
}
```

Reglas del servidor:

- `PROVIDER` exige `providerType` y `providerResourceId` válidos y activos.
- `OPERATOR` y `SUPERVISOR` rechazan ambos campos.
- `MASSAGE` solo acepta un `MassageTherapist` activo.
- `COURTS` solo acepta un `CourtPartnerCoach` activo.
- `MAINTENANCE` solo acepta un `MaintenanceProvider` activo y de tipo externo.
- `LAUNDRY` solo acepta un `LaundryProvider` activo.
- Cambiar tipo, recurso, estado, rol o contraseña incrementa
  `securityVersion`, invalidando tokens anteriores.

### 4.3 Claims del JWT

El JWT continuará incluyendo `role` y `securityVersion` y agregará:

```text
providerType
providerResourceId
```

El filtro JWT los contrastará contra la base de datos en cada petición, como ya
hace con el proveedor de lavandería. Los claims sirven para transportar la
identidad autenticada; la autorización final y el ownership seguirán dependiendo
del estado persistido.

## 5. Matriz inicial de permisos

La matriz exacta de acciones funcionales debe confirmarse antes de implementar
cada módulo. Como base segura se propone:

| Módulo | Lectura propia | Acciones propias | Operaciones excluidas |
| --- | --- | --- | --- |
| Canchas | Agenda y reservas asignadas | Confirmar/actualizar estado operativo definido | Tarifas, pagos, otros profesores, reportes globales |
| Masajes | Agenda y reservas asignadas | Confirmar/actualizar estado operativo definido | Pagos, catálogo, otros terapeutas, reportes globales |
| Mantenimiento | Órdenes asignadas y adjuntos permitidos | Iniciar, adjuntar evidencia y completar según flujo | Crear/asignar órdenes, pagos, proveedores, reportes globales |
| Lavandería | Documentos, devoluciones y stock propios | Confirmar/observar documentos y gestionar devoluciones permitidas | Ajustes de stock, conciliación y datos de otras lavanderías |

No se implementarán transiciones de estado nuevas hasta acordar cuáles puede
ejecutar cada externo; inicialmente se reutilizarán solamente transiciones ya
existentes que sean compatibles con el flujo y se protegerán en el servicio.

## 6. Plan de implementación backend

### Fase B1. Modelo y migración

1. Crear `ProviderAccessType`.
2. Generalizar en `AppUser` la asociación actual de lavandería para representar
   tipo y recurso asociado, manteniendo integridad referencial hacia las cuatro
   tablas de negocio.
3. Crear una migración Flyway aditiva posterior a la última versión existente:
   agregar los nuevos campos/relaciones, migrar usuarios con
   `laundry_provider_id` a tipo `LAUNDRY` y agregar restricciones `CHECK` e
   índices.
4. Mantener temporalmente `laundry_provider_id` si es necesario para un despliegue
   compatible; eliminarlo únicamente en una migración posterior cuando el código
   y los clientes ya no lo utilicen.
5. Rechazar asociaciones con entidades inactivas al crear o editar usuarios. La
   desactivación posterior de la entidad de negocio no borrará el usuario, pero
   impedirá su acceso operativo hasta resolver o reasignar el vínculo.

La forma física preferida para esta primera versión es mantener claves foráneas
explícitas y una restricción que permita exactamente una según `provider_type`.
Se evita una pareja polimórfica `tipo + id` sin claves foráneas porque permitiría
referencias huérfanas.

### Fase B2. Principal, JWT y contratos de usuario

1. Introducir un valor de dominio `ProviderAccess` que centralice tipo,
   identificador y validaciones, evitando condicionales repetidos.
2. Adaptar `AuthenticatedUserPrincipal`, `JwtService` y
   `JwtAuthenticationFilter`.
3. Adaptar `CreateAppUserDto`, `UpdateAppUserDto`, `AppUserDto`,
   `CurrentUserDto` y `AuthTokenDto` con compatibilidad temporal para lavandería.
4. Adaptar `UserManagementService`, auditoría y carga de usuario.
5. Documentar los enums y ejemplos JSON en la documentación REST.

### Fase B3. Política de autorización reutilizable

1. Crear un componente de autorización, por ejemplo `ProviderAccessGuard`, con
   operaciones para exigir tipo y ownership.
2. Definir consultas de repositorio acotadas por el actor autenticado; por
   ejemplo, reserva por `bookingId + partnerCoachId`, no solamente por
   `bookingId`.
3. Configurar `SecurityConfig` para permitir cada ruta externa solo a
   `PROVIDER`; el guard validará el subtipo y el recurso.
4. Conservar la herencia actual `SUPERVISOR -> OPERATOR` sin otorgar
   `ROLE_OPERATOR` a proveedores.
5. Registrar en auditoría usuario, tipo, recurso vinculado y acción externa.

### Fase B4. APIs externas por módulo

Implementar un módulo por vez para reducir el riesgo:

1. Consolidar lavandería sobre el modelo genérico sin cambiar su comportamiento.
2. Canchas: agenda y reservas filtradas por `CourtPartnerCoach`.
3. Masajes: agenda y reservas filtradas por `MassageTherapist`.
4. Mantenimiento: órdenes filtradas por `MaintenanceProvider` y transiciones
   expresamente permitidas.

Para cada módulo se definirán antes de codificar: campos visibles, estados
editables, rango temporal, paginación, orden estable y tratamiento de recursos
cancelados o inactivos.

### Fase B5. Pruebas y validación backend

Agregar pruebas de integración para:

- Creación y actualización válida de cada tipo de proveedor.
- Rechazo de combinación rol/tipo/recurso inválida o inactiva.
- Migración de un proveedor de lavandería existente sin pérdida de acceso.
- Claims correctos y rechazo de tokens con tipo, recurso o `securityVersion`
  obsoletos.
- `401` sin autenticación y `403` con rol o tipo incorrecto.
- Acceso permitido a recursos propios y denegado a recursos de otro proveedor
  del mismo tipo.
- Imposibilidad de acceder a endpoints administrativos mediante variantes de
  método HTTP o URL.
- Invalidación inmediata al deshabilitar, reasignar o cambiar el rol del usuario.
- Regresión completa de `OPERATOR` y `SUPERVISOR`.

Validación mínima de cierre del backend:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

También se actualizará el smoke test HTTP para cubrir al menos un login y una
operación autorizada/denegada de proveedor.

## 7. Plan de implementación frontend (después del backend)

El frontend vive en otro repositorio, por lo que esta fase se ejecutará allí una
vez estabilizado el contrato.

1. Actualizar modelos de login/usuario con `providerAccess`, conservando lectura
   temporal de `laundryProviderId`.
2. Implementar guards de navegación por rol y `providerAccess.type`.
3. Construir un menú específico: el proveedor verá únicamente su módulo, perfil
   y cambio de contraseña.
4. Crear pantallas de autoservicio contra las rutas `/provider/**`; no reutilizar
   pantallas administrativas si muestran datos o acciones adicionales.
5. Gestionar estados de carga, vacío, error, `401` y `403`.
6. Ocultar acciones no permitidas como medida de experiencia de usuario, sin
   asumir que esto reemplaza la autorización del backend.
7. Agregar pruebas de guards, menú, serialización y pantalla para cada tipo.
8. Validar el flujo extremo a extremo con un usuario de cada tipo y uno de
   control que intente acceder a datos ajenos.

### 7.1 Configuración de proveedores para el supervisor

El frontend incorporará una sección `Configuración > Proveedores`, visible para
el rol `SUPERVISOR`. Desde esta sección el supervisor podrá dar de alta y
administrar los actores externos de todos los tipos soportados:

| Opción de configuración | Entidad administrada | Datos específicos iniciales |
| --- | --- | --- |
| Canchas | Profesor asociado (`CourtPartnerCoach`) | Nombre y estado activo |
| Masajes | Empresa y masajista (`MassageProvider` / `MassageTherapist`) | Empresa, especialidad, contacto, terapeuta y estado |
| Mantenimiento | `MaintenanceProvider` | Tipo externo, especialidad, servicio, alcance, contacto y estado |
| Lavandería | `LaundryProvider` | Nombre, contacto y estado |

La pantalla tendrá un selector o pestañas por tipo, listado con búsqueda y
estado, formulario de alta, edición y activación/desactivación. Los campos
obligatorios y enums se obtendrán del contrato del backend y se representarán
con tipado estricto, sin duplicar reglas de negocio como fuente de verdad.

### 7.2 Alta del acceso al sistema

Crear el actor de negocio y otorgarle acceso son operaciones relacionadas pero
distintas. El flujo del supervisor será:

1. Crear o seleccionar el profesor, masajista, proveedor de mantenimiento o
   lavandería.
2. Elegir `Habilitar acceso al sistema`.
3. Informar usuario, contraseña inicial y estado.
4. El frontend enviará `role: PROVIDER`, el `providerType` correspondiente y el
   identificador del actor recién creado.
5. Mostrar el resultado del alta y si el actor tiene o no una cuenta asociada.

No se enviará la contraseña por correo ni se volverá a mostrar después de una
respuesta satisfactoria. El supervisor podrá restablecerla mediante el flujo de
usuarios existente. La entrega segura de las credenciales iniciales debe
definirse operativamente antes de habilitar usuarios reales.

El alta del actor podrá completarse sin crear inmediatamente una cuenta. Si el
segundo paso falla, la pantalla conservará el actor y permitirá reintentar la
creación del acceso, evitando duplicar el proveedor. No se asumirá atomicidad
entre dos endpoints independientes.

### 7.3 Permisos y contratos necesarios para la configuración

- La navegación y las rutas de configuración exigirán `SUPERVISOR`.
- El backend seguirá siendo la autoridad: crear una cuenta `PROVIDER`, asociarla,
  reasignarla, deshabilitarla o restablecer su contraseña requerirá
  `SUPERVISOR`.
- Los catálogos de actores usarán los endpoints administrativos existentes de
  canchas, masajes, mantenimiento y lavandería, adaptándolos solo cuando falte
  información necesaria. No se crearán endpoints duplicados por conveniencia de
  la pantalla.
- Si actualmente `OPERATOR` puede mantener alguno de esos catálogos, esa
  capacidad no se eliminará accidentalmente. La gestión de credenciales y
  asociaciones de acceso sí permanecerá exclusiva de `SUPERVISOR`.
- Los listados deberán indicar `sin acceso`, `usuario activo` o `usuario
  deshabilitado`, sin exponer hashes, tokens ni otros datos sensibles.
- Se impedirá asociar el mismo actor a cuentas incompatibles. La cardinalidad
  exacta (una o varias cuentas por actor) quedará definida y validada por el
  backend antes de construir el formulario.

### 7.4 Pruebas frontend de configuración

Se cubrirán al menos los siguientes escenarios:

- El supervisor visualiza la sección y puede crear proveedores de los cuatro
  tipos.
- Un `OPERATOR` o `PROVIDER` no puede navegar a la sección ni ejecutar las
  operaciones protegidas.
- El tipo seleccionado construye el formulario y contrato correctos.
- El alta del actor seguida del alta de usuario mantiene el identificador y tipo
  correctos.
- Un error al crear la cuenta no duplica el actor y permite reintentar.
- Se representan correctamente validaciones `400`, duplicados `409`, falta de
  autorización `403` y errores inesperados.
- La desactivación y reasignación actualizan la sesión mediante
  `securityVersion` y no dejan accesos anteriores vigentes.

## 8. Estrategia de despliegue

1. Desplegar migración y backend compatibles con el contrato anterior.
2. Crear usuarios de prueba deshabilitados o en ambiente de prueba para cada
   tipo.
3. Adaptar y desplegar el frontend.
4. Habilitar proveedores por módulo de forma gradual, empezando por lavandería,
   cuyo flujo ya existe.
5. Observar errores `401/403`, fallos de ownership y eventos de auditoría.
6. Retirar los campos y rutas legadas de lavandería solamente en una versión
   posterior y anunciada.

## 9. Riesgos y controles

- **Exposición horizontal de datos:** todo detalle, lista, adjunto y mutación debe
  filtrar ownership en backend; se cubrirá con pruebas cruzadas entre dos
  proveedores del mismo tipo.
- **Confusión entre empresa y persona:** masajes se vincula al terapeuta y canchas
  al profesor. Si el negocio necesita acceso empresarial agregado, será otro
  alcance y deberá modelarse explícitamente.
- **Proveedor con varios servicios:** no se cubre en la primera versión. El modelo
  de rol no lo impide, pero requerirá pasar de un vínculo único a una colección de
  asignaciones.
- **Cliente existente de lavandería:** se mantiene compatibilidad temporal del
  contrato y se despliega por etapas.
- **Entidades desactivadas:** no se elimina identidad histórica; se bloquean
  operaciones y se conserva auditoría.
- **Rutas administrativas demasiado amplias:** no se reutilizan directamente para
  externos; se publican DTO y endpoints limitados.

## 10. Decisiones funcionales pendientes antes de B4

Estas definiciones no bloquean B1-B3, pero sí la API operativa de cada módulo:

1. Qué acciones exactas puede ejecutar el profesor sobre una reserva de cancha.
2. Qué acciones exactas puede ejecutar el masajista y si ve datos de contacto del
   huésped.
3. Qué transiciones, campos y adjuntos puede modificar mantenimiento.
4. Si una empresa de masajes necesita un usuario que vea a todos sus terapeutas,
   además de usuarios individuales por masajista.
5. Si un mismo usuario externo podrá pertenecer a más de un tipo en el futuro
   cercano.

## 11. Criterios de aceptación globales

- Existe un único rol `PROVIDER` y cuatro tipos iniciales de acceso.
- Un proveedor autenticado recibe únicamente su tipo e identidad de negocio.
- No puede consultar ni modificar datos de otro proveedor, incluso conociendo el
  identificador del recurso.
- No puede acceder a configuración, usuarios, pagos o reportes globales no
  autorizados.
- `OPERATOR` y `SUPERVISOR` conservan su comportamiento actual.
- El proveedor de lavandería existente continúa funcionando durante la
  transición.
- El supervisor puede crear y administrar profesores, masajistas, proveedores de
  mantenimiento y lavanderías desde configuración, y otorgarles o revocarles
  acceso al sistema.
- `OPERATOR` y `PROVIDER` no pueden administrar credenciales ni asociaciones de
  acceso de terceros.
- Migraciones, tests, build y smoke test finalizan correctamente.
- El frontend muestra solo el módulo correspondiente y soporta correctamente
  `401/403`, sin ser la única barrera de seguridad.
