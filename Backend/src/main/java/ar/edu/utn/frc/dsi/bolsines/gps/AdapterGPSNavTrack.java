package ar.edu.utn.frc.dsi.bolsines.gps;

/** Adapter para NavTrack QX-7A: retrieveTrackingData(apiKey, nroBolsin, codCMDestino) -> "101,-31.42,-64.18,2026-03-07T12:30". */
public class AdapterGPSNavTrack implements IDatosLocalizacion {
    private final ServicioGPSNavTrack servicioGPSNavTrack;
    private String apiKey;
    private String codCMDestino;
    private int nroBolsin;
    private String respuesta;

    public AdapterGPSNavTrack(String apiKey, ServicioGPSNavTrack servicioGPSNavTrack) {
        this.apiKey = apiKey;
        this.servicioGPSNavTrack = servicioGPSNavTrack;
    }

    @Override
    public String[][] recibirDatos(String apiKey, int nroBolsin, String codCMOrigen, String codCMDestino) {
        this.apiKey = apiKey;
        this.nroBolsin = nroBolsin;
        this.codCMDestino = codCMDestino; // este modelo identifica por CM destino; el origen no lo usa
        solicitarDatosUbicacion();
        return adaptarDatosLocalizacion();
    }

    @Override
    public void solicitarDatosUbicacion() {
        respuesta = servicioGPSNavTrack.retrieveTrackingData(apiKey, nroBolsin, codCMDestino);
    }

    @Override
    public String[][] adaptarDatosLocalizacion() {
        try {
            // un ítem por línea, 4 valores separados por coma
            return respuesta.strip().lines()
                    .filter(l -> !l.isBlank())
                    .map(l -> l.split(","))
                    .map(p -> IDatosLocalizacion.fila(p[0], p[1], p[2], p[3]))
                    .toArray(String[][]::new);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta inválida del GPS NavTrack QX-7A: " + respuesta, e);
        }
    }
}
