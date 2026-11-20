# Bolsines – Frontend (PPAI 2026 · 3K5 · G5)

React 18 + Vite. Consume el backend de `../Backend` (http://localhost:8081). Mapa: Leaflet + OpenStreetMap.

## Ejecutar

1. Backend y mock del GPS Tracker levantados (ver `../Backend/README.md`).
2. `cp .env.example .env` (solo `VITE_API_URL`; `.env` no se sube).
3. `npm install` y `npm run dev` → http://localhost:5173 (origen permitido por el CORS del backend).

Usuarios de prueba: `eb.cordoba` / `bolsines` (demo del CU 36), `eb.tucuman` (flujo A1), `gcm.cordoba` (rol Gerente: no ve Seguimiento).

## Estructura

| Archivo | Contenido |
|---|---|
| `src/api.js` | Único cliente HTTP: header `X-Sesion-Id`, errores `problem+json` → `detail`, 401 → login |
| `src/main.jsx` | Sesión, login y navegación por rol |
| `src/PantallaSeguimientoBolsines.jsx` | CU 36, con los atributos y métodos del diseño (`habilitarVentana()`, `tomarSeleccionBolsinEnMapa()`, …) |
| `src/Bolsines.jsx` | CU 19, 20, 21, 22, 27, 28, 39, 40 |
| `src/Remitos.jsx` | CU 15, 16, 17, 18 |
| `src/Documentaciones.jsx` | CU 7, 10 |
| `src/ui.jsx` | Componentes compartidos: escudo de CM, tramo de estados, historial, avisos, diálogo |
