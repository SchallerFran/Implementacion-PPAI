package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.api.Dtos.ConfirmacionRequest;
import ar.edu.utn.frc.dsi.bolsines.api.Dtos.SeleccionRequest;
import ar.edu.utn.frc.dsi.bolsines.seguimiento.GestorConsultarSeguimientoBolsines;
import ar.edu.utn.frc.dsi.bolsines.seguimiento.SeguimientoDtos.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Endpoints que invoca la PantallaSeguimientoBolsines (frontend) para el CU 36.
 * Cada sesión tiene su propia instancia del gestor mientras dura el caso de uso.
 */
@RestController
@RequestMapping("/api/seguimiento-bolsines")
public class SeguimientoBolsinesController {
    // ponytail: el CU en curso vive en memoria por sesión; se pierde si se reinicia el backend (se vuelve a consultar)
    private final Map<UUID, GestorConsultarSeguimientoBolsines> gestores = new ConcurrentHashMap<>();
    private final ObjectProvider<GestorConsultarSeguimientoBolsines> nuevoGestor;
    private final SesionActual sesionActual;
    private final String dispositivoPorDefecto;

    public SeguimientoBolsinesController(ObjectProvider<GestorConsultarSeguimientoBolsines> nuevoGestor, SesionActual sesionActual,
                                         @Value("${gps.dispositivo-por-defecto}") String dispositivoPorDefecto) {
        this.nuevoGestor = nuevoGestor;
        this.sesionActual = sesionActual;
        this.dispositivoPorDefecto = dispositivoPorDefecto;
    }

    /** opcionConsultarUbicacion(): pasos 1-6. dispositivoGPS: XTR-4500L | NavTrack QX-7A | GeoPulse MTR-900. */
    @PostMapping
    public ConsultaUbicacion consultarUbicacion(@RequestHeader(SesionActual.HEADER) UUID sesionId,
                                                @RequestParam(required = false) String dispositivoGPS) {
        GestorConsultarSeguimientoBolsines gestor = nuevoGestor.getObject();
        gestores.remove(sesionId);
        ConsultaUbicacion consulta = gestor.consultarUbicacion(sesionActual.obtener(sesionId),
                dispositivoGPS == null || dispositivoGPS.isBlank() ? dispositivoPorDefecto : dispositivoGPS);
        gestores.put(sesionId, gestor);
        return consulta;
    }

    /** Filtro del mapa por número de precinto y/o CM destino (A2). */
    @GetMapping("/bolsines")
    public List<MarcadorBolsin> filtrar(@RequestHeader(SesionActual.HEADER) UUID sesionId,
                                        @RequestParam(required = false) String numeroPrecinto,
                                        @RequestParam(required = false) String codigoCMDestino) {
        return gestor(sesionId).filtrarBolsines(numeroPrecinto, codigoCMDestino);
    }

    /** tomarSeleccionBolsinEnMapa(): pasos 7-8, devuelve la pregunta de confirmación de mail. */
    @PostMapping("/seleccion")
    public SeleccionBolsin tomarSeleccion(@RequestHeader(SesionActual.HEADER) UUID sesionId, @Valid @RequestBody SeleccionRequest req) {
        return gestor(sesionId).tomarSeleccionBolsinEnMapa(req.numeroBolsin());
    }

    /** tomarConfirmacionMailCMGerenteDestino(): pasos 9-11 (A5 si enviarMail=false). Finaliza el CU. */
    @PostMapping("/confirmacion")
    public ResultadoSeguimiento tomarConfirmacion(@RequestHeader(SesionActual.HEADER) UUID sesionId, @Valid @RequestBody ConfirmacionRequest req) {
        ResultadoSeguimiento resultado = gestor(sesionId).tomarConfirmacionDeMailCMGerenteDestino(req.enviarMail());
        gestores.remove(sesionId);
        return resultado;
    }

    /** A4: el EB cancela la ejecución del caso de uso. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@RequestHeader(SesionActual.HEADER) UUID sesionId) {
        sesionActual.obtener(sesionId);
        gestores.remove(sesionId);
    }

    private GestorConsultarSeguimientoBolsines gestor(UUID sesionId) {
        sesionActual.obtener(sesionId);
        GestorConsultarSeguimientoBolsines gestor = gestores.get(sesionId);
        if (gestor == null) throw new IllegalStateException("No hay una consulta de seguimiento en curso. Ejecute POST /api/seguimiento-bolsines");
        return gestor;
    }
}
