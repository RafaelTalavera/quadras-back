# Plan De Roles Y Puestos

## Objetivo

Definir un modelo de seguridad y navegacion para que cada usuario tenga un puesto dentro de la organizacion, y que ese puesto determine:

- que modulos puede ver en el frontend
- que acciones puede ejecutar en el backend
- que tareas pendientes se le muestran al ingresar

## Estado Actual Detectado

Backend:

- El sistema ya tiene autenticacion con JWT.
- El usuario persistido tiene un unico `role`.
- El JWT ya emite el claim `role`.
- El filtro JWT valida que el `role` del token coincida con el persistido.
- La autorizacion de endpoints hoy esta hardcodeada a `OPERATOR`.
- La base de datos solo acepta `OPERATOR`.

Frontend:

- La sesion ya guarda `role`.
- El `role` aun no gobierna la navegacion ni el acceso visual.
- El shell renderiza todas las secciones.
- La configuracion todavia muestra "Operador" de forma fija.

## Conclusion Tecnica

La base actual sirve como punto de partida. No hay un conflicto estructural grave, pero hoy el modelo esta acoplado a un solo rol (`OPERATOR`) y mezcla dos conceptos:

- puesto organizacional
- permiso efectivo de acceso

Si mas adelante un mismo puesto necesita permisos mas finos, o si dos puestos comparten parcialmente accesos, conviene separar ambos conceptos desde ahora aunque al principio haya una relacion 1 a 1.

## Recomendacion De Modelo

### 1. Mantener un solo puesto por usuario

Para esta etapa, la regla puede ser:

- un usuario tiene exactamente un puesto

Eso es consistente con lo que definiste y simplifica autenticacion, JWT, filtros de UI y futuras tareas pendientes.

### 2. Separar semanticamente puesto de permisos

Recomendacion:

- `position` o `puesto`: identifica el cargo dentro de la organizacion
- `permissions`: lista derivada del puesto, usada para autorizacion y UI

Aunque por ahora solo `SISTEMAS` tenga acceso total, esta separacion evita rehacer el modelo cuando entren los demas puestos.

### 3. Catalogo inicial de puestos

Definir valores estables de maquina, sin acentos ni espacios. Ejemplo:

- `RECEPTIONIST`
- `RESERVATIONS`
- `SUPERVISOR`
- `MAINTENANCE`
- `HOUSEKEEPING`
- `RESTAURANT`
- `SYSTEMS`

Los labels visibles al usuario pueden seguir en espanol o portugues:

- Recepcionista
- Reservas
- Supervisor
- Mantenimiento
- Governanca
- Restaurante
- Sistemas

Nota: para `Governanca` conviene decidir una convencion estable desde ya. A nivel tecnico recomiendo un codigo unico y ASCII, por ejemplo `HOUSEKEEPING` o `GOVERNANCE`, y no cambiarlo despues.

## Recomendacion Para JWT

No conviene que el frontend dependa solo de decodificar el token por su cuenta. El token puede informar el puesto, pero el backend debe seguir siendo la autoridad real.

### Contrato recomendado

Mantener compatibilidad transitoria con `role`, pero empezar a emitir tambien:

- `position`
- `permissions`
- `modules`

Ejemplo:

```json
{
  "sub": "usuario.sistemas",
  "role": "SYSTEMS",
  "position": "SYSTEMS",
  "permissions": ["*"],
  "modules": ["ALL"],
  "iat": 1711610000,
  "exp": 1711638800
}
```

### Regla importante

El frontend puede usar `position` o `permissions` para ocultar o mostrar cosas, pero el backend debe validar siempre el acceso real al endpoint.

Si no, cualquier usuario con una UI manipulada podria intentar llamar endpoints no permitidos.

## Plan De Implementacion

### Fase 1. Normalizar el dominio en backend

Objetivo: reemplazar el rol unico actual por puestos reales.

Cambios:

- Cambiar `AppUserRole` por un enum de puestos reales.
- Idealmente renombrar el concepto a `AppUserPosition` para que el dominio quede claro.
- Si no quieren tocar mucho codigo ahora, pueden mantener temporalmente el nombre `role` y migrar a puestos reales, pero semantica y documentalmente eso queda mas debil.

Recomendacion practica:

- corto plazo: mantener `role` en codigo y DTOs para no romper demasiado
- mediano plazo: migrar a `position` en modelo, DTOs y claims

### Fase 2. Migracion de base de datos

Objetivo: dejar persistencia preparada.

Cambios:

- nueva migracion que actualice el `CHECK` de `app_users.role`
- migrar usuarios actuales `OPERATOR` a `SYSTEMS`
- actualizar cualquier seed o demo user

Decision recomendada:

- si `OPERATOR` ya no tiene sentido de negocio, migrarlo directamente a `SYSTEMS`
- no mantener `OPERATOR` como legado salvo que exista un motivo operativo real

### Fase 3. Respuesta de login y `/users/me`

Objetivo: que el frontend reciba el puesto de forma confiable y estable.

Cambios:

- devolver `position`
- mantener `role` de forma transitoria si el frontend actual lo necesita
- opcionalmente devolver `permissions` y `modules`

Ejemplo recomendado:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 28800,
  "username": "usuario.sistemas",
  "role": "SYSTEMS",
  "position": "SYSTEMS",
  "permissions": ["*"],
  "modules": ["ALL"]
}
```

### Fase 4. Autorizacion backend

Objetivo: dejar de depender de un unico `hasRole(OPERATOR)`.

Cambios:

- reemplazar reglas fijas por acceso por puesto o permiso
- empezar por `SYSTEMS` con acceso total
- dejar preparados los matchers para sumar otros puestos despues

Recomendacion:

- usar permisos de alto nivel por modulo, por ejemplo `reservations.read`, `reservations.write`, `massages.read`, `settings.admin`
- derivar esos permisos desde el puesto

Esto permite que el dia de manana `Supervisor` comparta parte de acceso con `Reservas` sin inventar nuevos puestos tecnicos.

### Fase 5. Frontend guiado por sesion

Objetivo: que la UI renderice solo lo que corresponde.

Cambios:

- reemplazar el uso directo de `session.role` por un modelo tipo `UserAccessProfile`
- mapear `position` y `permissions` a visibilidad de modulos, tabs y acciones
- ocultar secciones no habilitadas
- bloquear botones o acciones no permitidas

Recomendacion:

- no dispersar `if (role == ...)` por toda la UI
- centralizarlo en un helper o servicio de acceso

Ejemplo:

- `AccessPolicy.canViewReservations(session)`
- `AccessPolicy.canManageMassages(session)`
- `AccessPolicy.canAccessSettings(session)`

### Fase 6. Tareas pendientes por puesto

Objetivo: usar el mismo modelo para la bandeja inicial del usuario.

Recomendacion:

- cada tarea pendiente debe tener al menos:
  - `assignedPosition`
  - `status`
  - `priority`
  - `dueAt`

Ejemplo de regla:

- un usuario `RESERVATIONS` ve tareas con `assignedPosition = RESERVATIONS`
- un usuario `SUPERVISOR` podria ver las propias y, mas adelante, tareas de supervision

## Recomendacion De Diseño

### Opcion recomendada

Usar este esquema:

- `AppUser.position`
- `PositionPermissionsResolver`
- JWT con `position` y `permissions`
- frontend con `AccessPolicy`

Ventajas:

- el puesto sigue representando la estructura organizacional real
- los permisos quedan listos para crecer sin rediseñar el login
- las tareas pendientes por puesto entran naturalmente en el modelo

## Riesgos O Conflictos A Considerar

### 1. Confundir visibilidad de frontend con seguridad real

Ocultar menus en Flutter mejora UX, pero no asegura nada. La seguridad real debe vivir en backend.

### 2. Mantener `role` como nombre tecnico demasiado tiempo

Si el negocio habla de puestos, tarde o temprano `role` empieza a generar ambiguedad. Conviene planear desde ahora una migracion ordenada a `position`.

### 3. Usuarios con mas de un puesto

Hoy no parece necesario. Si mas adelante una persona cubre dos areas, el modelo de un solo puesto por usuario queda corto. Por ahora no es un bloqueo, pero conviene saberlo.

### 4. Tareas asignadas solo por puesto

Si despues necesitan tareas por hotel, turno, sector o sede, el puesto solo no alcanza. La entidad de tarea deberia quedar abierta a agregar mas contexto.

## Recomendacion Para Esta Primera Implementacion

Dado que por ahora solo van a trabajar con `SISTEMAS`, sugiero este alcance minimo:

1. Reemplazar `OPERATOR` por el catalogo real de puestos.
2. Mapear todos los usuarios actuales a `SYSTEMS`.
3. Emitir `position = SYSTEMS` en login, JWT y `/users/me`.
4. Mantener `role` temporalmente para compatibilidad.
5. Agregar `permissions` o `modules` aunque inicialmente sea acceso total.
6. Hacer que el frontend consuma ese perfil y no dependa de strings hardcodeados.

## Impacto En El Codigo Actual

Puntos del backend que seguramente van a cambiar:

- enum de roles/puestos
- entidad `AppUser`
- filtro JWT
- servicio que emite token
- DTOs de login y usuario actual
- configuracion de seguridad
- migracion SQL de `app_users`
- demo user y tests

Puntos del frontend que seguramente van a cambiar:

- `AuthSession`
- render del shell y navegacion
- settings
- visibilidad de modulos y acciones
- capa centralizada de permisos

## Orden Recomendado De Trabajo

1. Definir nombres tecnicos finales de los puestos.
2. Ajustar backend y migraciones.
3. Ajustar contrato de login y `/users/me`.
4. Adaptar frontend a `position` y `permissions`.
5. Recien despues empezar a cerrar vistas o acciones por puesto.
6. Sobre esa base, implementar tareas pendientes por puesto.

## Decision Final Recomendada

La logica que planteaste es valida y no tiene un conflicto de base. La unica recomendacion fuerte es esta:

- modelar el puesto como identidad organizacional
- modelar permisos como capacidad efectiva

Si hacen eso desde ahora, `SISTEMAS` puede salir rapido con acceso total, y el sistema queda listo para crecer a los demas puestos sin rehacer autenticacion, JWT, frontend ni tareas pendientes.
