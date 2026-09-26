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

## Base de datos

La API usa una instancia existente de MySQL o MariaDB. Flyway actualiza el
esquema al arrancar. `configure_local_mysql.ps1` es una ayuda opcional para
preparar MySQL; no instala el motor.

## Frontend

El frontend se compila y publica desde `quedras-front` y debe configurarse con
`COSTANORTE_API_BASE_URL=http://<host-api>:8080/api/v1`. Nunca debe recibir las
credenciales de la base de datos.
