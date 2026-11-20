package ar.edu.utn.frc.dsi.bolsines.gps;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Adaptee: API del GPS Tracker modelo GeoPulse MTR-900. Devuelve una matriz de datos. */
@Component
public class ServicioGPSGeoPulse {
    private final RestClient cliente;

    public ServicioGPSGeoPulse(RestClient gpsRestClient) {
        this.cliente = gpsRestClient;
    }

    public String[][] fetchCargoPositions(String apiKey, int nroBolsin) {
        return cliente.get()
                .uri(u -> u.path("/api/geopulse-mtr-900/fetchCargoPositions")
                        .queryParam("numeroBolsin", nroBolsin)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String[][].class);
    }
}
