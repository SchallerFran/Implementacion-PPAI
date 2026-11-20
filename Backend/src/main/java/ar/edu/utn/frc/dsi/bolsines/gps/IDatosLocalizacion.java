package ar.edu.utn.frc.dsi.bolsines.gps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Patrón Adapter - Target: interfaz propia del sistema que el InterfazGPSTracker (cliente) espera.
 * Cada modelo de GPS Tracker se resuelve en un adaptador que traduce la llamada y la respuesta.
 * <p>
 * Formato común de salida (String[][]): una fila por ítem con
 * [0] número de bolsín, [1] latitud, [2] longitud, [3] fecha y hora de última actualización (ISO yyyy-MM-ddTHH:mm:ss).
 */
public interface IDatosLocalizacion {

    String[][] recibirDatos(String apiKey, int nroBolsin, String codCMOrigen, String codCMDestino);

    void solicitarDatosUbicacion();

    String[][] adaptarDatosLocalizacion();

    /** Normaliza una fila al formato común. */
    static String[] fila(Object nroBolsin, Object latitud, Object longitud, Object fechaHora) {
        return new String[]{
                String.valueOf(nroBolsin).strip(),
                String.valueOf(Double.parseDouble(String.valueOf(latitud).strip())),
                String.valueOf(Double.parseDouble(String.valueOf(longitud).strip())),
                LocalDateTime.parse(String.valueOf(fechaHora).strip()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        };
    }
}
