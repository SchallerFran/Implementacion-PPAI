package ar.edu.utn.frc.dsi.bolsines.gps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/** Adapter para XTR-4500L: getBolsinLocation(apiKey, nroBolsin, codCMOrigen) -> JSON {"items":[{...}]}. */
public class AdapterGPSXTR implements IDatosLocalizacion {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ServicioGPSXTR servicioGPSXTR;
    private String apiKey;
    private String codCMOrigen;
    private int nroBolsin;
    private String respuesta;

    public AdapterGPSXTR(String apiKey, ServicioGPSXTR servicioGPSXTR) {
        this.apiKey = apiKey;
        this.servicioGPSXTR = servicioGPSXTR;
    }

    @Override
    public String[][] recibirDatos(String apiKey, int nroBolsin, String codCMOrigen, String codCMDestino) {
        this.apiKey = apiKey;
        this.nroBolsin = nroBolsin;
        this.codCMOrigen = codCMOrigen; // este modelo identifica por CM origen; el destino no lo usa
        solicitarDatosUbicacion();
        return adaptarDatosLocalizacion();
    }

    @Override
    public void solicitarDatosUbicacion() {
        respuesta = servicioGPSXTR.getBolsinLocation(apiKey, nroBolsin, codCMOrigen);
    }

    @Override
    public String[][] adaptarDatosLocalizacion() {
        try {
            List<String[]> filas = new ArrayList<>();
            for (JsonNode item : JSON.readTree(respuesta).path("items")) {
                filas.add(IDatosLocalizacion.fila(item.get("numeroBolsin").asText(), item.get("latitud").asText(),
                        item.get("longitud").asText(), item.get("fechaHoraUltimaActualizacion").asText()));
            }
            return filas.toArray(String[][]::new);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta inválida del GPS XTR-4500L: " + respuesta, e);
        }
    }
}
