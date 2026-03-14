# VALIDACION SEGURIDAD HITO 12 - COSTANORTE

## Alcance validado
- Spring Security activo con autenticacion stateless por JWT.
- Rol inicial `OPERATOR` embebido en el JWT y validado en backend.
- Usuario demo persistido automaticamente en bootstrap.
- Endpoints protegidos de reservas (`/api/v1/reservations/**`) y endpoint autenticado de usuario actual (`/api/v1/users/me`).
- Endpoints publicos limitados a salud (`/api/v1/system/health`) y login (`/api/v1/auth/login`).

## Usuario demo para pruebas
- Username: `operador.demo`
- Password: `Costanorte2026!`
- Rol: `OPERATOR`

## Configuracion disponible
- `COSTANORTE_JWT_SECRET`
- `COSTANORTE_JWT_EXPIRATION_SECONDS`
- `COSTANORTE_DEMO_USER_ENABLED`
- `COSTANORTE_DEMO_USER_USERNAME`
- `COSTANORTE_DEMO_USER_PASSWORD`
- `COSTANORTE_DEMO_USER_ROLE`

## Contrato backend para frontend

### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "operador.demo",
  "password": "Costanorte2026!"
}
```

Respuesta esperada:
```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 28800,
  "username": "operador.demo",
  "role": "OPERATOR"
}
```

### Usuario autenticado
```http
GET /api/v1/users/me
Authorization: Bearer <jwt>
```

### Uso sobre endpoints protegidos
```http
GET /api/v1/reservations
Authorization: Bearer <jwt>
```

## Validaciones ejecutadas el 2026-03-14

### 1. Suite automatizada
```powershell
.\mvnw test
```
- Resultado: OK
- Cobertura validada: `28 tests`

### 2. Empaquetado
```powershell
.\mvnw -DskipTests package
```
- Resultado: OK
- Artefacto generado: `target/costanorte-0.0.1-SNAPSHOT.jar`

### 3. Smoke local con MySQL y JWT
```powershell
.\scripts\backend_smoke_local.ps1 -SkipBuild
```

Resultado observado:
```json
{
  "date": "2026-03-14",
  "healthStatus": "UP",
  "authUser": "operador.demo",
  "authRole": "OPERATOR",
  "reservationId": 5,
  "createdStatus": "SCHEDULED",
  "updatedStatus": "SCHEDULED",
  "cancelledStatus": "CANCELLED",
  "reservationsOnDate": 3
}
```

## Observaciones
- El usuario demo se crea/actualiza durante el bootstrap antes de aceptar trafico, evitando carrera entre arranque y primer login.
- La clave demo es para entorno local/pruebas; en despliegues productivos debe redefinirse por variables de entorno.
- Los campos `reservationId` y `reservationsOnDate` del smoke pueden variar segun el estado previo de la base local.
