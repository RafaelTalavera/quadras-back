# Migración a WhatsApp manual — ejecución por etapas

Fecha: 2026-09-14. Repositorios: `quadras` y `quedras-front`.

## Decisión

El sistema no envía WhatsApp. El operador guarda la orden o el masaje, revisa
el texto preparado, elige aplicación Windows o WhatsApp Web y pulsa **Enviar**
dentro de WhatsApp. Abrir una conversación no demuestra envío, entrega ni
lectura. No se mostrará ninguno de esos estados.

## Etapa 0 — Estado previo y protección de datos

- Ambos árboles ya tenían cambios ajenos y no versionados; se preservaron.
- La migración V32 de Cloud API existe localmente. No hay certeza sobre su
  aplicación en bases externas. Por ello **no se modifica ni se borran sus
  tablas**; quedan históricas e inertes. Antes de retirarlas hay que comprobar
  la historia Flyway y el contenido de cada ambiente.
- Los campos `whatsapp_number` y `whatsapp_enabled` de prestadores se
  conservan. Ahora significan número y disponibilidad de contacto manual.

## Etapa 1 — Desactivar y retirar Cloud API (completada)

- Retirados cliente Graph API, worker, programación, webhook, credenciales,
  settings, presupuesto, cola y estados de respuesta automática.
- Los archivos Java Cloud API retirados eran locales sin seguimiento en Git;
  Git no puede restaurarlos automáticamente. El plan histórico se conserva.
- Retiradas las llamadas automáticas desde altas y ediciones de masajes y
  mantenimiento.
- No existe ninguna llamada del backend a Meta al guardar.
- La seguridad de los demás endpoints y la validación de proveedores se
  mantienen.

## Etapa 2 — Preparación manual en Flutter (completada)

- Componente compartido `core/whatsapp/manual_whatsapp_dialog.dart`.
- Previsualiza destinatario y texto; conserva los textos actuales de ambos
  módulos, sin introducir un segundo generador en el backend.
- Ofrece WhatsApp Web, aplicación Windows solo en Windows y copiar texto.
- Al pulsar cualquiera de los dos canales, copia primero el aviso al
  portapapeles y luego intenta abrir la conversación. Si WhatsApp no muestra
  el texto predefinido, el operador lo pega con Ctrl+V antes de enviarlo.
- No abre ningún canal sin clic del operador. Un fallo de apertura se muestra
  con alternativas, y jamás se etiqueta como mensaje enviado.
- El número se toma de `whatsappNumber`, no de `contact`.
- El guardado ofrece preparar el aviso solo si el proveedor está habilitado.
  La acción también queda disponible desde el registro.
- Mantenimiento separa contacto general y WhatsApp, y ya no exige WhatsApp
  para guardar una orden.
- Se retira el panel de costo/DRY_RUN de Configuraciones.

## Etapa 3 — Validación

- Backend: suite Maven completa aprobada el 2026-09-14.
- Frontend: suite completa aprobada (61 pruebas), incluida la prueba nueva de
  previsualización, número y codificación URI.
- `flutter analyze --no-fatal-infos` sin errores ni warnings; conserva 20
  avisos informativos de formularios Flutter obsoletos.
- Pendiente prueba manual en un Windows con WhatsApp instalado y sin instalar,
  navegador con y sin sesión y número de prueba. `url_launcher` solo confirma
  que el sistema operativo aceptó abrir el enlace, nunca el envío.
- En el Windows de desarrollo se verificó que el esquema `whatsapp:` está
  registrado. No se abrió una conversación ni se envió un mensaje de prueba.

## Etapa 4 — Trazabilidad opcional, no implementada

No se guarda una declaración de `enviado manualmente` porque dependería
exclusivamente de la palabra del operador. Si la operación necesita ese registro,
la siguiente iteración puede incorporar un botón explícito «Marcar como enviado»
en el historial, con usuario, fecha y canal, rotulado como confirmación manual y
sin pretender estados de Meta. No debe automatizarse mediante WhatsApp Web ni
mediante scraping de la aplicación de escritorio.

## Contrato y límites

- API de proveedores conserva `contact`, `whatsappNumber`,
  `whatsappEnabled`.
- No se requiere token, número empresarial Meta, webhook ni presupuesto API.
- `web.whatsapp.com/send?phone=...&text=...` abre la versión web.
- El parámetro `text` intenta prellenar el borrador, pero no se toma como
  garantía de que WhatsApp lo muestre. La copia automática es el respaldo;
  un fallo de apertura o de copia se informa por separado.
- `whatsapp://send?phone=...&text=...` solicita a Windows abrir la
  aplicación asociada. Si no existe, el operador puede usar Web o copiar.
- El botón no elude el paso final de envío dentro de WhatsApp.

## Prueba manual de aceptación

1. Cargar proveedor externo con contacto y WhatsApp distintos.
   Si el dato antiguo estaba solo en `contact`, copiarlo manualmente al nuevo
   campo WhatsApp tras verificar que efectivamente es el número correcto.
2. Guardar orden y comprobar que no abre WhatsApp automáticamente.
3. Revisar texto y número; abrir Windows y pulsar Enviar manualmente.
4. Repetir con Web, con sesión iniciada y sin ella.
   Confirmar que, si el campo aparece vacío, Ctrl+V pega exactamente la
   previsualización (acentos y saltos de línea incluidos).
5. Abrir el registro nuevamente y preparar el mismo texto.
6. Cargar proveedor sin WhatsApp y comprobar que la orden se guarda.
7. Repetir con masaje y caracteres acentuados/saltos de línea.
8. Comprobar que Configuraciones no muestra presupuesto Cloud API.
