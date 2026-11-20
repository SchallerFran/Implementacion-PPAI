package ar.edu.utn.frc.dsi.bolsines.gps;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Adaptee: API del GPS Tracker modelo NavTrack QX-7A. Devuelve un único String con ítems separados por comas. */
@Component
public class ServicioGPSNavTrack {
    private final RestClient cliente;

    public ServicioGPSNavTrack(RestClient gpsRestClient) {
        this.cliente = gpsRestClient;
    }

    public String retrieveTrackingData(String apiKey, int nroBolsin, String codCMDestino) {
        return cliente.get()
                .uri(u -> u.path("/api/navtrack-qx-7a/retrieveTrackingData")
                        .queryParam("numeroBolsin", nroBolsin)
                        .queryParam("codigoComisionMedicaDestino", codCMDestino)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}
