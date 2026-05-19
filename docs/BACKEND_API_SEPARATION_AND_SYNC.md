# BACKEND API SEPARADO Y SINCRONIZACION

## Estado actual
- `quadras` funciona como backend API puro.
- Ya no aloja frontend HTML, JS ni CSS embebido.
- La sincronizacion con `quedras-front` se expone como API a traves de `SSE`.

## Endpoint de sincronizacion
- Ruta: `GET /api/v1/sync/events`
- Seguridad: requiere autenticacion JWT.
- Proposito: informar cambios persistidos para que el frontend recargue solo la vista afectada.

## Dominios publicados
- `reservations`
- `massages`
- `courts`
- `tours`
- `maintenance`

## Contrato del evento
- `domain`
- `action`
- `entityId`
- `dateFrom`
- `dateTo`

## Regla de backend
- El backend no debe asumir detalles de UI.
- Solo publica cambios de dominio despues de una transaccion exitosa.
- La decision de refrescar o no una pantalla pertenece exclusivamente al frontend.
