package ar.edu.utn.frc.dsi.bolsines.gps;

import java.util.Arrays;

/** Adapter para GeoPulse MTR-900: fetchCargoPositions(apiKey, nroBolsin) -> [[101, -31.42, -64.18, "2026-03-07T12:30:00"]]. */
public class AdapterGPSGeoPulse implements IDatosLocalizacion {
    private final ServicioGPSGeoPulse servicioGPSGeoPulse;
    private String apiKey;
    private int nroBolsin;
    private String[][] respuesta;

    public AdapterGPSGeoPulse(String apiKey, ServicioGPSGeoPulse servicioGPSGeoPulse) {
        this.apiKey = apiKey;
        this.servicioGPSGeoPulse = servicioGPSGeoPulse;
    }

    @Override
    public String[][] recibirDatos(String apiKey, int nroBolsin, String codCMOrigen, String codCMDestino) {
        this.apiKey = apiKey;
        this.nroBolsin = nroBolsin; // este modelo solo usa el número de bolsín
        solicitarDatosUbicacion();
        return adaptarDatosLocalizacion();
    }

    @Override
    public void solicitarDatosUbicacion() {
        respuesta = servicioGPSGeoPulse.fetchCargoPositions(apiKey, nroBolsin);
    }

    @Override
    public String[][] adaptarDatosLocalizacion() {
        try {
            return Arrays.stream(respuesta)
                    .map(r -> IDatosLocalizacion.fila(r[0], r[1], r[2], r[3]))
                    .toArray(String[][]::new);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta inválida del GPS GeoPulse MTR-900: " + Arrays.deepToString(respuesta), e);
        }
    }
}
