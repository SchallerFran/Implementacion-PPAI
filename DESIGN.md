---
name: Bolsines
description: Seguimiento y gestión de bolsines entre comisiones médicas, leído como hoja de ruta vial.
colors:
  azul-900: "#12324f"
  azul-700: "#1f4e79"
  azul-600: "#2e74b5"
  azul-400: "#5b9bd5"
  azul-200: "#b7d3ee"
  cielo-100: "#dcebf7"
  cielo-50: "#eef5fb"
  amarillo: "#f2c230"
  amarillo-100: "#fdf3cf"
  verde: "#5e9e4b"
  verde-100: "#e2f0db"
  verde-ink: "#2c5e1f"
  naranja-100: "#fbe7d3"
  naranja-50: "#fff7ee"
  naranja-ink: "#8a4a0e"
  rojo: "#b3403a"
  rojo-100: "#f6dedc"
  rojo-ink: "#8e2c27"
  tinta: "#152536"
  tinta-2: "#46596c"
  linea: "#c8d9ea"
  linea-suave: "#e2ecf5"
  blanco: "#ffffff"
typography:
  display:
    fontFamily: "Montserrat, Arial, sans-serif"
    fontSize: "30px"
    fontWeight: 700
    letterSpacing: "-0.01em"
  headline:
    fontFamily: "Montserrat, Arial, sans-serif"
    fontSize: "24px"
    fontWeight: 700
    letterSpacing: "-0.01em"
  title:
    fontFamily: "Montserrat, Arial, sans-serif"
    fontSize: "18px"
    fontWeight: 700
  subtitle:
    fontFamily: "Montserrat, Arial, sans-serif"
    fontSize: "14px"
    fontWeight: 600
  body:
    fontFamily: "Arial, 'Helvetica Neue', Helvetica, sans-serif"
    fontSize: "14.5px"
    fontWeight: 400
    lineHeight: 1.45
  data:
    fontFamily: "Arial, 'Helvetica Neue', Helvetica, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    fontFeature: "tnum"
  label:
    fontFamily: "Montserrat, Arial, sans-serif"
    fontSize: "12.5px"
    fontWeight: 600
    lineHeight: 1.2
rounded:
  xs: "4px"
  sm: "6px"
  md: "8px"
  lg: "10px"
  full: "50%"
spacing:
  xs: "8px"
  sm: "12px"
  md: "16px"
  lg: "20px"
  xl: "28px"
components:
  button-primario:
    backgroundColor: "{colors.azul-700}"
    textColor: "{colors.blanco}"
    typography: "{typography.subtitle}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "38px"
  button-primario-hover:
    backgroundColor: "{colors.azul-900}"
  button-secundario:
    backgroundColor: "{colors.blanco}"
    textColor: "{colors.azul-700}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "38px"
  button-secundario-hover:
    backgroundColor: "{colors.cielo-50}"
  button-peligro-lleno:
    backgroundColor: "{colors.rojo}"
    textColor: "{colors.blanco}"
    rounded: "{rounded.sm}"
    height: "38px"
  button-peligro-lleno-hover:
    backgroundColor: "{colors.rojo-ink}"
  input:
    backgroundColor: "{colors.blanco}"
    textColor: "{colors.tinta}"
    rounded: "{rounded.sm}"
    padding: "0 10px"
    height: "38px"
  estado-base:
    backgroundColor: "{colors.cielo-100}"
    textColor: "{colors.azul-700}"
    rounded: "{rounded.xs}"
    padding: "0 8px"
    height: "22px"
  estado-firme:
    backgroundColor: "{colors.azul-700}"
    textColor: "{colors.blanco}"
    rounded: "{rounded.xs}"
    height: "22px"
  estado-transito:
    backgroundColor: "{colors.amarillo}"
    textColor: "{colors.azul-900}"
    rounded: "{rounded.xs}"
    height: "22px"
  estado-ok:
    backgroundColor: "{colors.verde-100}"
    textColor: "{colors.verde-ink}"
    rounded: "{rounded.xs}"
    height: "22px"
  estado-alerta:
    backgroundColor: "{colors.naranja-100}"
    textColor: "{colors.naranja-ink}"
    rounded: "{rounded.xs}"
    height: "22px"
  estado-baja:
    backgroundColor: "{colors.rojo-100}"
    textColor: "{colors.rojo-ink}"
    rounded: "{rounded.xs}"
    height: "22px"
  barra:
    backgroundColor: "{colors.azul-700}"
    textColor: "{colors.blanco}"
    height: "60px"
  panel:
    backgroundColor: "{colors.blanco}"
    rounded: "{rounded.md}"
    padding: "18px 20px 22px"
  dialogo:
    backgroundColor: "{colors.blanco}"
    textColor: "{colors.tinta}"
    rounded: "{rounded.lg}"
    padding: "24px"
    width: "520px"
---

# Design System: Bolsines

## Overview

**Creative North Star: "La Hoja de Ruta"**

Cada bolsín es un tramo de ruta entre dos comisiones médicas, y toda la interfaz se lee así: un escudo de ruta de origen, una vía con hitos de estado y un escudo de destino. El sistema no usa tarjetas de métricas; los datos aparecen como recorrido, en tablas densas y en un panel lateral de trabajo. Es una herramienta de oficina para jornada completa: azul institucional en la barra, fondo cielo pálido, superficies blancas con filete azul claro y texto en tinta azulada.

El color de estado está racionado. El amarillo correo solo significa "en tránsito", el verde mapa solo significa "recibido", el naranja solo "parcial o con alerta" y el rojo solo "baja o cancelado". Todo lo demás es la familia azul. El mapa de Seguimiento (Leaflet con teselas de OpenStreetMap) se apaga con un filtro para que la paleta propia mande, y la atribución de OSM queda siempre visible.

Nada desaparece: un bolsín o remito de baja queda en la tabla, tachado y con su tramo en gris punteado. Un cambio de estado redibuja la fila en su lugar con un destello amarillo breve, sin mover la grilla.

**Key Characteristics:**
- Escudos de ruta (pentágono azul con filete blanco interno, Montserrat Bold) como identidad de cada comisión médica.
- Tramo horizontal origen → hitos → destino como lectura principal del estado.
- Historial como hoja de ruta vertical.
- Montserrat para títulos, etiquetas y escudos; Arial para texto y tablas, con numerales tabulares.
- Planos con filete; la sombra solo para lo que flota (panel, diálogo, notas sobre el mapa).

## Colors

Una familia azul institucional que carga casi toda la interfaz, más cuatro colores de estado con significado único.

### Primary
- **Azul Institucional** (azul-700): barra superior, botón primario, escudos de ruta, hitos cumplidos del tramo, enlaces de fila, estado "firme" (Cerrado, En remito, En bolsín saliente).
- **Azul Noche** (azul-900): títulos, hover del botón primario, borde de los pines y del hito en tránsito, texto sobre amarillo, velo del diálogo al 45%.
- **Azul Medio** (azul-600): anillo de foco (2px) en toda la interfaz.
- **Azul Claro** (azul-400): borde en hover de campos, ítems de tramo y filas seleccionables.
- **Azul Bruma** (azul-200): borde del botón secundario y de la barra de scroll.

### Secondary
- **Amarillo Correo** (amarillo): exclusivo de lo que está en tránsito. Estado Enviado / En bolsín enviado, hito actual en tránsito, móvil animado sobre la vía, pin del mapa, selección de texto. Su variante pálida (amarillo-100) solo aparece como destello de fila actualizada.

### Tertiary
- **Verde Mapa** (verde, verde-100, verde-ink): recibido y aceptado. Hito de llegada, estado ok, aviso de éxito.
- **Naranja Parcial** (naranja-100, naranja-50, naranja-ink): recibido parcial, no recibida, para redirigir y el bloque "Sin ubicación del GPS".
- **Rojo Baja** (rojo, rojo-100, rojo-ink): De baja, Cancelado, Rechazada, errores y acciones destructivas.

### Neutral
- **Cielo** (cielo-100): fondo de estado base, fila activa, contadores, aviso informativo, halo del hito actual.
- **Cielo Fondo** (cielo-50): fondo de la página, hover de filas, bloque de origen en Seguimiento.
- **Tinta** (tinta): texto principal.
- **Tinta Secundaria** (tinta-2): texto secundario, etiquetas de campo, encabezados de tabla.
- **Línea** (linea): bordes de campos, tablas, panel, vía pendiente del tramo.
- **Línea Suave** (linea-suave): divisores entre filas y listas.
- **Blanco** (blanco): superficies de trabajo (tablas, panel, diálogo, panel de Seguimiento) y texto sobre azul.

### Named Rules
**La Regla del Amarillo Correo.** El amarillo significa "en tránsito" y nada más. Si algo es amarillo y no está viajando, está mal. El subrayado de la navegación activa es blanco, no amarillo.

**La Regla de Un Color por Destino.** Verde = recibido, naranja = parcial/alerta, rojo = baja. Cada estado del dominio se mapea a uno de seis tonos (base, firme, tránsito, ok, alerta, baja) y nunca se inventa un séptimo.

## Typography

**Display Font:** Montserrat (500, 600, 700 vía @fontsource; con Arial de respaldo)
**Body Font:** Arial (con Helvetica Neue, Helvetica)

**Character:** Montserrat geométrica y firme para todo lo que nombra (títulos, etiquetas, botones, escudos, estados); Arial neutra y compacta para lo que se lee en cantidad (texto, celdas, datos).

### Hierarchy
- **Display** (700, 30px, -0.01em): solo la marca grande del login.
- **Headline** (700, 24px, -0.01em): título de página (h1). En Seguimiento baja a 20px; en el login sube a 26px.
- **Title** (700, 18–19px): cabecera del panel lateral y del diálogo.
- **Subtitle** (600, 14–16px): subtítulos de sección con contador, asunto del panel, nombre de tramo, botones.
- **Body** (400, 14.5px, 1.45): texto general; celdas de tabla a 14px.
- **Label** (600, 12.5px, 1.2): etiquetas de campo y encabezados de tabla, en tinta secundaria, en caja normal.
- **Texto chico** (400, 12.5px): metadatos, ayudas, fechas bajo un dato.

### Named Rules
**La Regla del Numeral Tabular.** Pesos, precintos, fechas y números de bolsín usan numerales tabulares; las columnas numéricas se alinean a la derecha.

**La Regla de la Caja Normal.** Ninguna etiqueta va en mayúsculas sostenidas ni con espaciado amplio; la jerarquía la dan peso y color.

## Layout

Barra superior fija de 60px. Las páginas de gestión (Bolsines, Remitos, Documentación) usan un contenedor de hasta 1440px con 28px de margen: cabecera con título y acción primaria a la derecha, fila de filtros (campos de 220px) y debajo la zona de trabajo. Al seleccionar una fila, la zona se parte en tabla flexible + panel de 420px pegajoso bajo la barra.

Seguimiento (CU 36) es una grilla de pantalla completa: panel blanco de 360px a la izquierda con filtros, lista de tramos y avisos; el mapa ocupa el resto.

Ritmo de espaciado sobre 8 / 12 / 16 / 20 / 28px; filas de tabla con 12px × 14px de relleno.

Adaptación: a 1180px el panel cae debajo de la tabla y se ocultan los datos del usuario; a 860px la marca queda solo en logo, la navegación se desplaza en horizontal, Seguimiento apila panel arriba y mapa de 60vh abajo, y el login pasa a una columna; a 560px la navegación reparte pestañas con ícono sobre texto y las listas de datos pasan a una columna.

## Elevation & Depth

Sistema plano con filete: tablas, campos, ítems de tramo y avisos se separan por borde de 1px en línea o por fondo tonal (cielo), sin sombra. La sombra queda reservada para lo que flota sobre el resto.

### Shadow Vocabulary
- **Flotante** (`0 1px 2px rgb(18 50 79 / 0.08), 0 6px 18px rgb(18 50 79 / 0.08)`): panel lateral, tarjeta de información y nota sobre el mapa.
- **Modal** (`0 20px 50px rgb(18 50 79 / 0.3)`): diálogo nativo, con velo azul noche al 45%.
- **Pin** (`-2px 3px 6px rgb(18 50 79 / 0.35)`): sombra difusa de los marcadores del mapa.
- **Halo** (`0 0 0 3px` en cielo-100, verde-100 o naranja-100): anillo del hito actual y del punto vigente del historial.

### Named Rules
**La Regla de lo que Flota.** Solo proyecta sombra lo que se superpone a otra cosa (panel pegajoso, diálogo, capas del mapa). Lo que vive en la grilla es plano.

## Shapes

Esquinas suavemente redondeadas y escalonadas por tamaño: 4px en estados y código, 6px en botones, campos, avisos y filas seleccionables, 8px en contenedores (tabla, panel, ítem de tramo), 10px en el diálogo. Los hitos y puntos del historial son círculos; el pin es una gota (círculo con una esquina recta, girado 45°). El escudo de ruta es la única silueta propia: pentágono de señal vial con filete interno. Las vías son líneas de 2px; punteadas cuando el recorrido está anulado o es decorativo (login).

## Components

### Buttons
Firmes y sobrios, siempre en Montserrat 600.
- **Shape:** esquinas suaves (6px), altura 38px (32px en la variante chica, 42px en el login).
- **Primario:** azul institucional con texto blanco; hover a azul noche.
- **Secundario:** blanco con texto azul institucional y borde azul bruma; hover a cielo fondo con borde azul claro.
- **Texto:** sin fondo, azul institucional; hover subraya con 3px de separación.
- **Peligro:** contorno (texto rojo-ink, hover rojo-100) o lleno (rojo, hover rojo-ink) para confirmar bajas y cancelaciones.
- **Ícono:** 32px, transparente, hover cielo; variante clara sobre la barra.
- **Hover / Focus:** transición de color de 0.15s; foco con anillo de 2px azul medio. Deshabilitado al 55% de opacidad.

### Chips (Estado)
- **Style:** pastilla de 22px de alto, esquinas de 4px, Montserrat 600 12px, uno de seis tonos (base, firme, tránsito, ok, alerta, baja).
- **Contador:** pastilla redondeada de 20px en cielo/azul, naranja cuando alerta.

### Cards / Containers
- **Corner Style:** 8px.
- **Background:** blanco sobre cielo fondo.
- **Shadow Strategy:** plano salvo el panel lateral (sombra flotante).
- **Border:** 1px línea.
- **Internal Padding:** 12px en ítems de tramo, 18px × 20px en el panel.

### Inputs / Fields
- **Style:** 38px de alto, borde 1px línea, fondo blanco, esquinas de 6px; select con chevron azul propio.
- **Focus:** borde y anillo de 2px azul medio sin separación; hover con borde azul claro.
- **Error / Disabled:** errores en aviso rojo; deshabilitado al 70% con cursor bloqueado.
- **Casillas:** listas de opciones con borde suave; la opción marcada toma borde azul claro y fondo cielo.

### Navigation
Barra azul institucional de 60px con marca (logo + "Bolsines"), pestañas en Montserrat 600 14px con ícono, blanco al 80%; hover blanco con velo del 7%; la activa en blanco pleno con subrayado blanco de 3px. A la derecha, el escudo de la CM del usuario y su nombre.

### Escudo de Ruta
Código de comisión médica (CM-CBA-01) dibujado como señal vial: pentágono azul institucional, filete blanco interno, sigla en Montserrat Bold y número debajo. 34px en tablas, 48px en el detalle, 72px en el login; en fondos azules se rellena de azul noche.

### Tramo
Escudo de origen, vía de hitos y escudo de destino. Hito cumplido: punto azul reducido; actual: anillo azul con halo cielo; en tránsito: relleno amarillo con borde azul noche y un móvil amarillo que avanza hacia el próximo hito; llegada: verde (naranja si fue parcial). Anulado: gris, al 55% y con vía punteada. La versión detallada nombra cada hito debajo.

### Historial
Hoja de ruta vertical: línea de 2px, puntos azules por cambio de estado y el vigente como anillo con halo, cada uno con su estado, fecha y responsable.

### Mapa
Teselas de OSM apagadas (`saturate(0.4) brightness(1.04) contrast(0.94)`), pines en gota amarilla con número del bolsín; el pin activo se agranda y pasa a blanco. Tarjeta de información flotante arriba a la derecha y atribución visible.

## Do's and Don'ts

### Do:
- **Do** leer cada bolsín y remito como tramo: escudo de origen, hitos, escudo de destino.
- **Do** reservar el amarillo correo (#f2c230) para lo que está en tránsito.
- **Do** mapear cada estado del dominio a uno de los seis tonos de estado existentes.
- **Do** dejar visibles y tachados los registros de baja o cancelados.
- **Do** usar numerales tabulares y alinear a la derecha las columnas numéricas.
- **Do** mantener visible la atribución de OpenStreetMap y las teselas apagadas.
- **Do** respetar `prefers-reduced-motion`: las animaciones se reducen a un instante.

### Don't:
- **Don't** usar amarillo para decoración, navegación activa o énfasis general.
- **Don't** armar tableros de tarjetas con métricas; la información se presenta como recorrido, tabla y panel.
- **Don't** hacer desaparecer un registro al cambiar de estado; se redibuja en su lugar.
- **Don't** poner sombra a elementos que viven en la grilla (tablas, campos, ítems de tramo).
- **Don't** poner etiquetas en mayúsculas sostenidas.
