package ar.edu.utn.frc.dsi.bolsines.gps;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Boundary hacia el GPS Tracker (cliente del Adapter). Crea el adaptador del modelo de dispositivo
 * y trabaja solo contra IDatosLocalizacion, sin conocer la API concreta de cada modelo.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InterfazGPSTracker {
    public static final String XTR_4500L = "XTR-4500L";
    public static final String NAVTRACK_QX_7A = "NavTrack QX-7A";
    public static final String GEOPULSE_MTR_900 = "GeoPulse MTR-900";

    private final ServicioGPSXTR servicioGPSXTR;
    private final ServicioGPSNavTrack servicioGPSNavTrack;
    private final ServicioGPSGeoPulse servicioGPSGeoPulse;

    private String apiKey;
    private String codCMOrigen;
    private String codCMDestino;
    private int nroBolsin;

    public InterfazGPSTracker(ServicioGPSXTR servicioGPSXTR, ServicioGPSNavTrack servicioGPSNavTrack, ServicioGPSGeoPulse servicioGPSGeoPulse) {
        this.servicioGPSXTR = servicioGPSXTR;
        this.servicioGPSNavTrack = servicioGPSNavTrack;
        this.servicioGPSGeoPulse = servicioGPSGeoPulse;
    }

    /**
     * Respecto del diagrama se agrega nombreDispGPS (atributo del gestor) para saber qué adaptador crear.
     * @return filas [nroBolsin, latitud, longitud, fechaHoraUltimaActualizacion]
     */
    public String[][] solicitarDatosLocalizacion(String nombreDispGPS, String apiKey, int nroBolsin, String codCMOrigen, String codCMDestino) {
        this.apiKey = apiKey;
        this.nroBolsin = nroBolsin;
        this.codCMOrigen = codCMOrigen;
        this.codCMDestino = codCMDestino;
        IDatosLocalizacion adaptador = switch (nombreDispGPS) {
            case XTR_4500L -> new AdapterGPSXTR(apiKey, servicioGPSXTR);
            case NAVTRACK_QX_7A -> new AdapterGPSNavTrack(apiKey, servicioGPSNavTrack);
            case GEOPULSE_MTR_900 -> new AdapterGPSGeoPulse(apiKey, servicioGPSGeoPulse);
            default -> throw new IllegalArgumentException("Modelo de GPS Tracker desconocido: " + nombreDispGPS);
        };
        return adaptador.recibirDatos(this.apiKey, this.nroBolsin, this.codCMOrigen, this.codCMDestino);
    }
}
