---
version: 1
slug: "frontend"
primary_target: "Frontend"
related_targets: []
---

Scope: Frontend completo (login, CU 36 PantallaSeguimientoBolsines, documentación, remitos, bolsines). Mode: operate.
Audience: Encargado de Bolsines de una CM, escritorio, horario laboral. Job: seguir bolsines en tránsito y operar sus estados.

## Direction contract

THESIS: Cada bolsín es un tramo de ruta entre dos comisiones médicas. La pantalla rechaza el panel genérico de tarjetas con métricas: los datos se leen como hoja de ruta, origen → hitos de estado → destino.

OWN-WORLD: Azul institucional #1F4E79 en la barra superior, azul cielo #DCEBF7 tintando el fondo, amarillo correo #F2C230 solo para lo que está en tránsito (Enviado, marcador del mapa), verde mapa #5E9E4B para recibido/aceptado. Códigos de CM como escudos de ruta (rectángulo azul, borde blanco interno, Montserrat Bold). Montserrat para títulos y escudos, Arial para texto y tablas, numerales tabulares.

STORY: El EB entra, ve su CM, abre Seguimiento, ve en el mapa dónde está cada bolsín enviado, filtra, selecciona y decide avisar al gerente destino. En las demás pantallas opera el ciclo de vida.

FIRST VIEWPORT: Barra superior azul con la marca Bolsines, CM del usuario como escudo y la navegación. En Seguimiento, el mapa ocupa ~70% a la derecha; a la izquierda, panel de 360px con filtros (numeroPrecintoTxt, CM destino, modelo GPS), la lista de tramos y el aviso A3. Cancelar arriba a la derecha.

FORM: Hoja de ruta vial, candidato 3 de mi lista. seed:9b66ace3f74037e2. Raised (de split-flap): un cambio de estado redibuja la fila en el lugar, sin romper la grilla. Raised (de la ticket wallet): nada desaparece; DeBaja o Cancelado queda tachado y visible.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
