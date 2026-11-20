package ar.edu.utn.frc.dsi.bolsines.seguimiento;

import java.time.LocalDateTime;
import java.util.List;

/** Datos que el CU 36 devuelve a la PantallaSeguimientoBolsines (frontend). */
public final class SeguimientoDtos {
    private SeguimientoDtos() {}

    public record MarcadorBolsin(int numeroBolsin, String numeroPrecinto, String codigoCMDestino, String nombreCMDestino,
                                 double latitud, double longitud, String fechaHoraUltimaActualizacion) {}

    /** A3: el GPS Tracker no pudo informar la ubicación de este bolsín. */
    public record BolsinSinUbicacion(int numeroBolsin, String numeroPrecinto, String motivo) {}

    public record ConsultaUbicacion(String codigoCM, String nombreCM, String dispositivoGPS, LocalDateTime fechaActual,
                                    List<MarcadorBolsin> bolsines, List<BolsinSinUbicacion> bolsinesSinUbicacion) {}

    public record SeleccionBolsin(MarcadorBolsin bolsin, String pregunta) {}

    /** Datos que informa el CU 31 Notificar ubicación de bolsín. */
    public record NotificacionUbicacion(String destinatario, int numeroBolsin, String fechaHoraUltimaActualizacion,
                                        double latitud, double longitud) {}

    public record ResultadoSeguimiento(boolean mailEnviado, String mensaje, NotificacionUbicacion notificacion) {}
}
