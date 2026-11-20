# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

React + Vite (Node.js) en `Frontend/`, consumiendo el backend Spring Boot de `Backend/` (http://localhost:8081). Mapa con Leaflet + OpenStreetMap (`react-leaflet` v4), decisión del equipo en lugar de Google Maps: sin API key; el backend conserva `InterfazServidorGoogleMaps` y solo entrega coordenadas. La atribución de OpenStreetMap debe quedar visible.

## Users

Empleados de las Comisiones Médicas (CM) de un Organismo Controlador:
- **Encargado de Bolsines (EB)**: arma, precinta, despacha, recibe y sigue bolsines con documentación entre CM. Usuario principal del CU 36 (Consultar seguimiento de bolsines).
- **Gerente de CM (GCM)**: recibe por mail la ubicación de un bolsín; en el sistema no puede ejecutar el CU 36.
- **Encargado de Documentación**: registra documentación y genera remitos (en los datos de prueba lo hace el EB).

Inferido de la ERS v1.2 y la Descripción del Caso (Docs/).

## Product Purpose

Gestionar el envío y la recepción de bolsines de documentación entre comisiones médicas, con trazabilidad completa del estado de cada bolsín, remito y documentación, y seguimiento GPS de los bolsines en tránsito. Es el Proyecto Práctico de Aplicación Integrador (PPAI 2026, 3K5, Grupo 5) de DSI, UTN-FRC; el éxito se mide en la defensa: el CU 36 respeta los nombres del diseño (PantallaSeguimientoBolsines) y todos los flujos alternativos A1–A5.

## Operating Context

Oficina administrativa de una CM, en horario laboral, escritorio con monitor. Al final de la jornada el EB separa los bolsines cerrados; el correo los retira a primera hora. El seguimiento se hace sobre un mapa de Argentina.

## Capabilities and Constraints

- CU 36 con orden obligatorio consultar → (filtrar) → seleccionar → confirmar; cancelar (A4) disponible siempre. Estado del CU guardado por sesión en el backend.
- Patrón Adapter para 3 modelos de GPS Tracker: XTR-4500L, NavTrack QX-7A, GeoPulse MTR-900.
- Máquinas de estado de Documentación, Remito y Bolsín: el backend es la fuente de verdad; el front no duplica reglas, muestra el `detail` de los errores (problem+json).
- Sesión por header `X-Sesion-Id`; 401 vuelve al login.
- El mail del CU 31 no se envía realmente (sin credenciales de Microsoft Graph); el backend lo registra.
- Contrato completo: `Backend/docs/INTEGRACION_FRONTEND.txt` y `Backend/README.md`.

## Brand Commitments

- Paleta tomada de las ilustraciones de los documentos de la cátedra (azul cielo, azul institucional, amarillo del correo, verde del mapa).
- Tipografía del documento `PPAI2026_3K5A_G5_E2_Diseño.pdf`: Montserrat (títulos) y Arial (texto).
- Encabezado académico: UTN-FRC, Diseño de Sistemas de Información.

## Evidence on Hand

Datos de prueba del backend (11 CM, bolsines 101–111, usuarios `eb.<cm>` / `gcm.<cm>`, contraseña `bolsines`). No hay métricas, clientes ni testimonios: no inventar.

## Product Principles

1. El backend decide; la pantalla muestra lo que devuelve y explica los errores con su `detail`.
2. Nombres del modelado visibles en el código de la pantalla del CU 36.
3. Cada estado de un bolsín, remito o documentación es legible de un vistazo.
4. Ningún flujo alternativo se presenta como error técnico.
