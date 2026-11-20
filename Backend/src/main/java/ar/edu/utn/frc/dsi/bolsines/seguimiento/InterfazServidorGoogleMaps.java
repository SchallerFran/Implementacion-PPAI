package ar.edu.utn.frc.dsi.bolsines.seguimiento;

import ar.edu.utn.frc.dsi.bolsines.modelo.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.seguimiento.SeguimientoDtos.MarcadorBolsin;
import org.springframework.stereotype.Component;

/**
 * Boundary hacia el Servidor Google Maps.
 * El mapa lo dibuja el frontend (Leaflet + OpenStreetMap en lugar de Google Maps, sin API key); acá solo se arma
 * el marcador con las coordenadas, que sirven para cualquier proveedor de mapas.
 */
@Component
public class InterfazServidorGoogleMaps {

    /** @param datos fila del GPS: [nroBolsin, latitud, longitud, fechaHoraUltimaActualizacion] */
    public MarcadorBolsin marcarBolsinEnMapa(Bolsin bolsin, String[] datos) {
        return new MarcadorBolsin(bolsin.getNumeroBolsin(), bolsin.getNumeroPrecinto(),
                bolsin.getCMDestino().getCodigo(), bolsin.getCMDestino().getNombre(),
                Double.parseDouble(datos[1]), Double.parseDouble(datos[2]), datos[3]);
    }
}
