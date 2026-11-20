# Bolsines – Backend (PPAI 2026 · 3K5 · G5)

CU 36 **Consultar seguimiento de bolsines** con patrón **Adapter** para los 3 modelos de GPS Tracker,
y las máquinas de estado de **Remito** y **Bolsín**.

Java 21 · Spring Boot 3.5 · Maven · PostgreSQL · JPA/Hibernate · API REST JSON.

## Requisitos
- JDK 21 o superior y Maven 3.9+
- PostgreSQL (probado con la versión 18)
- API mock del GPS Tracker de la cátedra: https://github.com/LiberaeMentis/dsi-api-bolsines

## Ejecutar

1. Tener PostgreSQL iniciado. **No hace falta crear la base**: al arrancar, el backend crea `bolsines` si no existe
   (`CreadorBaseDatos`), Hibernate crea las tablas y `DatosIniciales` carga los datos de prueba.
2. Configurar las variables de entorno: copiar `.env.example` como `.env` y completar al menos `DB_PASSWORD`.
   ```
   cp .env.example .env
   ```
   `.env` está en `.gitignore`: **nunca se sube al repositorio**. Spring lo lee al iniciar (`spring.config.import`
   en `application.properties`). Una variable definida en el sistema operativo tiene prioridad sobre el `.env`.
3. Levantar la API mock del GPS Tracker en `http://localhost:8080` (`.\mvnw.cmd spring-boot:run` en ese repositorio).
4. Levantar el backend (puerto **8081**) desde la carpeta `Backend`:
   ```
   mvn spring-boot:run
   ```
5. Tests unitarios: `mvn test`. Pruebas de endpoints: importar `postman/Bolsines.postman_collection.json`
   y ejecutar la colección completa en orden (o `postman collection run postman/Bolsines.postman_collection.json`).

### Variables de entorno (`.env`)
| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8081` | Puerto del backend |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `bolsines` | Conexión a PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / **obligatoria** | Credenciales de PostgreSQL |
| `GPS_BASE_URL` | `http://localhost:8080` | URL de la API del GPS Tracker |
| `GPS_DISPOSITIVO_POR_DEFECTO` | `XTR-4500L` | Modelo de GPS si el request no indica otro |
| `GPS_API_KEY_XTR` / `GPS_API_KEY_NAVTRACK` / `GPS_API_KEY_GEOPULSE` | **obligatorias** | API keys de cada modelo de GPS |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Orígenes del frontend permitidos |

La documentación técnica completa (arquitectura, vista de diseño, flujos y referencia de la API) está en
[`docs/Documentacion_Tecnica_Backend.pdf`](docs/Documentacion_Tecnica_Backend.pdf).

Con la base vacía se cargan datos de prueba (`DatosIniciales`): 11 comisiones médicas con los mismos códigos que el
mock GPS, bolsines **101–110** en estado Enviado (iguales al mock) y el **111** (Córdoba → Mendoza, no existe en el GPS).
Para cada CM hay dos usuarios con contraseña `bolsines`:
`eb.<cm>` (Encargado de Bolsines) y `gcm.<cm>` (Gerente), con `<cm>` = cordoba, rosario, mendoza, central, tucuman,
salta, jujuy, neuquen, mardelplata, laplata, villamaria. Para volver a cargar los datos de prueba, borrar y recrear la base.

## Estructura

| Paquete | Contenido |
|---|---|
| `modelo` | Entidades del modelo de dominio. La lógica de negocio y las transiciones de estado están acá (`Bolsin.precintar()`, `Remito.asignar()`, …) |
| `seguimiento` | `GestorConsultarSeguimientoBolsines` (control del CU 36) e `InterfazServidorGoogleMaps` |
| `gps` | **Adapter**: `IDatosLocalizacion` (target), `AdapterGPSXTR/NavTrack/GeoPulse`, `ServicioGPS*` (adaptees), `InterfazGPSTracker` (cliente) |
| `repositorio` | Spring Data JPA |
| `api` | Controllers REST, DTOs, sesión y manejo de errores |

Fuera de `src/`: `docs/` (documentación técnica en PDF), `postman/` (colección de pruebas de endpoints),
`.env.example` (plantilla de variables de entorno).

## API

Todas las rutas (salvo login y catálogos) requieren el header **`X-Sesion-Id`** con el `sesionId` del login.
Los errores devuelven `application/problem+json` (`{ "title", "status", "detail" }`):
400 dato inválido · 401 sin sesión · 403 sin permiso · 404 no encontrado · 409 transición de estado no permitida.

### Sesión y catálogos
| Método | Ruta | Body / Respuesta |
|---|---|---|
| POST | `/api/sesiones` | `{nombre, contrasena}` → `{sesionId, usuario, empleado, email, rol, comisionMedica}` |
| GET | `/api/sesiones/actual` | igual que el login |
| DELETE | `/api/sesiones` | cierra la sesión (204) |
| GET | `/api/comisiones-medicas` | `[{codigo, nombre, direccion, email, telefono, localidad}]` |
| GET | `/api/tipos-documento` | `[{nombre, descripcion}]` |

### CU 36 Consultar seguimiento de bolsines (PantallaSeguimientoBolsines)
Requiere rol Encargado de Bolsines. El estado del caso de uso se guarda por sesión entre los pasos.

| Paso | Método | Ruta | Body / Respuesta |
|---|---|---|---|
| 1–6 `opcionConsultarUbicacion` | POST | `/api/seguimiento-bolsines?dispositivoGPS=XTR-4500L` | → `{codigoCM, nombreCM, dispositivoGPS, fechaActual, bolsines:[Marcador], bolsinesSinUbicacion:[{numeroBolsin, numeroPrecinto, motivo}]}` |
| 6 filtro | GET | `/api/seguimiento-bolsines/bolsines?numeroPrecinto=&codigoCMDestino=` | → `[Marcador]` (404 = A2) |
| 7–8 `tomarSeleccionBolsinEnMapa` | POST | `/api/seguimiento-bolsines/seleccion` | `{numeroBolsin}` → `{bolsin: Marcador, pregunta}` |
| 9–11 `tomarConfirmacionMailCMGerenteDestino` | POST | `/api/seguimiento-bolsines/confirmacion` | `{enviarMail: true\|false}` → `{mailEnviado, mensaje, notificacion:{destinatario, numeroBolsin, fechaHoraUltimaActualizacion, latitud, longitud}}` |
| A4 cancelar | DELETE | `/api/seguimiento-bolsines` | 204 |

`Marcador = {numeroBolsin, numeroPrecinto, codigoCMDestino, nombreCMDestino, latitud, longitud, fechaHoraUltimaActualizacion}`.
`dispositivoGPS` es opcional (default `XTR-4500L`). Valores: `XTR-4500L`, `NavTrack QX-7A`, `GeoPulse MTR-900`.
Flujos alternativos: A1 = 404 en el POST inicial · A2 = 404 en el filtro · A3 = el bolsín aparece en `bolsinesSinUbicacion` · A4 = DELETE · A5 = `enviarMail:false`.

### Documentación, remitos y bolsines (máquinas de estado)
| CU | Método | Ruta | Body | Transición |
|---|---|---|---|---|
| 7 | POST | `/api/documentaciones` | `{tipoDocumento, asunto, fechaPase, archivos:[{nombreArchivo, tituloDocumento}]}` | → Registrada |
| 10 | GET | `/api/documentaciones[?estado=]`, `/api/documentaciones/{numero}` | | |
| 15 | POST | `/api/remitos` | `{codigoCMDestino, documentaciones:[numero], areaCMCDestino?}` | Remito Creado · Doc EnRemito |
| 16 | PUT | `/api/remitos/{numero}` | `{documentaciones:[numero]}` | solo Creado · doc quitada → Registrada |
| 17 | POST | `/api/remitos/{numero}/cancelar` | | Creado → Cancelado · Doc → Registrada |
| 18 | GET | `/api/remitos[?estado=&codigoCMDestino=]`, `/api/remitos/{numero}` | | |
| 19 | POST | `/api/bolsines` | `{codigoCMDestino, remitos:[numero], peso}` | Bolsín Creado · Remitos/Doc EnBolsinSaliente |
| 20 | PUT | `/api/bolsines/{numero}` | `{remitos:[numero]}` | solo Creado · remito quitado → Creado, Doc → EnRemito |
| 39 | POST | `/api/bolsines/{numero}/precintar` | `{numeroPrecinto}` | Creado → Cerrado |
| 40 | POST | `/api/bolsines/{numero}/abrir` | | Cerrado → Creado (borra precinto) |
| 21 | DELETE | `/api/bolsines/{numero}` | | Creado/Cerrado → DeBaja · Remitos → Creado · Doc → EnRemito |
| 27 | POST | `/api/bolsines/{numero}/retiro` | | Cerrado → Enviado · Remitos/Doc → EnBolsinEnviado |
| 28 | POST | `/api/bolsines/{numero}/recepcion` | `{documentacionesNoRecibidas:[numero]}` | Enviado → Recibido · Remito RecibidoYAceptado o RecibidoYAceptadoParcial · Doc RecibidaYAceptada o NoRecibida (lo registra el EB de la CM destino) |
| 22 | GET | `/api/bolsines[?estado=&numeroPrecinto=]`, `/api/bolsines/{numero}` | | incluye remitos, documentación e historial de estados |

## Decisiones respecto del diseño
- `InterfazGPSTracker.solicitarDatosLocalizacion()` recibe además `nombreDispGPS` (atributo del gestor) para saber qué adaptador crear, y devuelve `String[][]`.
- El mapa lo dibuja el frontend con Leaflet + OpenStreetMap (en lugar de la librería de Google Maps del RNF 7, para no
  depender de una API key con facturación). El backend conserva el boundary `InterfazServidorGoogleMaps` del diseño:
  `marcarBolsinEnMapa()` arma los marcadores con las coordenadas, independientes del proveedor del mapa.
- CU 31 Notificar ubicación: se registra en el log y se devuelve en la respuesta. El envío real por Microsoft Graph (RNF 5) requiere credenciales.
- Las API keys del GPS se configuran en `.env` (`GPS_API_KEY_*`).
