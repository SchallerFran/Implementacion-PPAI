package ar.edu.utn.frc.dsi.bolsines.gps;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Adaptee: API del GPS Tracker modelo XTR-4500L. Devuelve un JSON en formato String. */
@Component
public class ServicioGPSXTR {
    private final RestClient cliente;

    public ServicioGPSXTR(RestClient gpsRestClient) {
        this.cliente = gpsRestClient;
    }

    public String getBolsinLocation(String apiKey, int nroBolsin, String codCMOrigen) {
        return cliente.get()
                .uri(u -> u.path("/api/xtr-4500l/getBolsinLocation")
                        .queryParam("numeroBolsin", nroBolsin)
                        .queryParam("codigoComisionMedicaOrigen", codCMOrigen)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}
