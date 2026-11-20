package ar.edu.utn.frc.dsi.bolsines.seguimiento;

import ar.edu.utn.frc.dsi.bolsines.gps.InterfazGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.modelo.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.modelo.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Sesion;
import ar.edu.utn.frc.dsi.bolsines.modelo.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repositorio.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.seguimiento.SeguimientoDtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Control del CU 36 Consultar seguimiento de bolsines.
 * Prototype: una instancia por ejecución del caso de uso (guarda el estado de la conversación entre pasos).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GestorConsultarSeguimientoBolsines {
    private static final Logger log = LoggerFactory.getLogger(GestorConsultarSeguimientoBolsines.class);

    // atributos del diagrama de clases de diseño
    private String codigoCMdestino;
    private String codigoCMorigen;
    private final Map<Integer, String[]> datosLocalizacionBolsin = new LinkedHashMap<>();
    private LocalDateTime fechaActual;
    private final List<Bolsin> listaBolsinesEnviados = new ArrayList<>();
    private String nombreDispGPS;
    private int numeroBolsin;
    private String numeroPrecinto;

    private Empleado encargadoLogueado;
    private ComisionMedica cmLogueado;
    private Bolsin bolsinSeleccionado;
    private final List<MarcadorBolsin> marcadores = new ArrayList<>();
    private final List<BolsinSinUbicacion> bolsinesSinUbicacion = new ArrayList<>();

    private final EmpleadoRepository empleados;
    private final BolsinRepository bolsines;
    private final InterfazGPSTracker interfazGPSTracker;
    private final InterfazServidorGoogleMaps interfazServidorGoogleMaps;
    private final Map<String, String> apiKeysGPS;

    public GestorConsultarSeguimientoBolsines(EmpleadoRepository empleados, BolsinRepository bolsines,
                                              InterfazGPSTracker interfazGPSTracker,
                                              InterfazServidorGoogleMaps interfazServidorGoogleMaps,
                                              @Value("#{${gps.api-keys}}") Map<String, String> apiKeysGPS) {
        this.empleados = empleados;
        this.bolsines = bolsines;
        this.interfazGPSTracker = interfazGPSTracker;
        this.interfazServidorGoogleMaps = interfazServidorGoogleMaps;
        this.apiKeysGPS = apiKeysGPS;
    }

    /** Pasos 1 a 6: busca los bolsines enviados desde la CM del EB, pide su ubicación al GPS y los marca en el mapa. */
    @Transactional(readOnly = true)
    public ConsultaUbicacion consultarUbicacion(Sesion actual, String nombreDispGPS) {
        if (!apiKeysGPS.containsKey(nombreDispGPS)) {
            throw new IllegalArgumentException("Modelo de GPS Tracker desconocido: " + nombreDispGPS + ". Disponibles: " + apiKeysGPS.keySet());
        }
        this.nombreDispGPS = nombreDispGPS;
        this.fechaActual = LocalDateTime.now();
        getEncargadoDeBolsinLogueado(actual);
        buscarBolsinEnviadoDesdeCM();
        pedirDatosGPSTracker();
        marcarBolsinEnMapa();
        return habilitarOpcionSeleccionarBolsinEnMapa();
    }

    /** Paso 2: busca la CM del usuario logueado. */
    public void getEncargadoDeBolsinLogueado(Sesion actual) {
        Usuario usuario = actual.getUsuario();
        // ponytail: recorre todos los empleados como en el diagrama de secuencia; usar findByUsuario si crecen
        for (Empleado e : empleados.findAll()) {
            if (e.esTuUsuario(usuario)) {
                encargadoLogueado = e;
                break;
            }
        }
        if (encargadoLogueado == null || !encargadoLogueado.sosEncargadoDeBolsines()) {
            throw new SecurityException("El usuario " + usuario.getNombre() + " no es Encargado de Bolsines");
        }
        cmLogueado = encargadoLogueado.getCM();
        codigoCMorigen = cmLogueado.getCodigo();
    }

    /** Paso 3: bolsines en estado Enviado cuya CM origen es la del usuario logueado. A1 si no hay ninguno. */
    public void buscarBolsinEnviadoDesdeCM() {
        listaBolsinesEnviados.clear();
        for (Bolsin b : bolsines.findAll(Sort.by("numeroBolsin"))) {
            if (b.esTuComisionMedicaOrigen(cmLogueado) && b.sosEnviado()) listaBolsinesEnviados.add(b);
        }
        if (listaBolsinesEnviados.isEmpty()) {
            throw new NoSuchElementException("No se encuentran bolsines en estado Enviado desde la " + cmLogueado.getNombre());
        }
    }

    /** Pasos 4 y 5: pide y recibe del GPS Tracker la localización de cada bolsín. A3 si no puede informarla. */
    public void pedirDatosGPSTracker() {
        datosLocalizacionBolsin.clear();
        bolsinesSinUbicacion.clear();
        String apiKey = apiKeysGPS.get(nombreDispGPS);
        for (Bolsin b : listaBolsinesEnviados) {
            int nro = b.getNumeroBolsin();
            try {
                String[][] filas = interfazGPSTracker.solicitarDatosLocalizacion(nombreDispGPS, apiKey, nro,
                        b.getOrigen().getCodigo(), b.getCMDestino().getCodigo());
                // si informa varias posiciones del bolsín, la más reciente (fecha ISO => orden lexicográfico)
                Optional<String[]> ultima = Arrays.stream(filas)
                        .filter(f -> Integer.parseInt(f[0]) == nro)
                        .max(Comparator.comparing((String[] f) -> f[3]));
                if (ultima.isPresent()) datosLocalizacionBolsin.put(nro, ultima.get());
                else bolsinesSinUbicacion.add(new BolsinSinUbicacion(nro, b.getNumeroPrecinto(), "El GPS Tracker no informó la ubicación"));
            } catch (RestClientException | IllegalStateException e) {
                log.warn("GPS {} sin ubicación para bolsín {}: {}", nombreDispGPS, nro, e.getMessage());
                bolsinesSinUbicacion.add(new BolsinSinUbicacion(nro, b.getNumeroPrecinto(), motivo(e)));
            }
        }
    }

    /** Paso 6: marca en el mapa los bolsines con ubicación. */
    public void marcarBolsinEnMapa() {
        marcadores.clear();
        for (Bolsin b : listaBolsinesEnviados) {
            String[] datos = datosLocalizacionBolsin.get(b.getNumeroBolsin());
            if (datos != null) marcadores.add(interfazServidorGoogleMaps.marcarBolsinEnMapa(b, datos));
        }
    }

    public ConsultaUbicacion habilitarOpcionSeleccionarBolsinEnMapa() {
        return new ConsultaUbicacion(cmLogueado.getCodigo(), cmLogueado.getNombre(), nombreDispGPS, fechaActual,
                List.copyOf(marcadores), List.copyOf(bolsinesSinUbicacion));
    }

    /** Paso 6 (filtro): por número de precinto y/o CM destino. A2 si no hay coincidencias. */
    public List<MarcadorBolsin> filtrarBolsines(String numeroPrecinto, String codigoCMDestino) {
        List<MarcadorBolsin> filtrados = marcadores.stream()
                .filter(m -> numeroPrecinto == null || numeroPrecinto.equalsIgnoreCase(m.numeroPrecinto()))
                .filter(m -> codigoCMDestino == null || codigoCMDestino.equalsIgnoreCase(m.codigoCMDestino()))
                .toList();
        if (filtrados.isEmpty()) {
            throw new NoSuchElementException(numeroPrecinto != null
                    ? "No se encuentra bolsín con el número de precinto " + numeroPrecinto
                    : "No se encuentran bolsines para la CM destino " + codigoCMDestino);
        }
        return filtrados;
    }

    /** Pasos 7 y 8: toma el bolsín seleccionado en el mapa y arma la consulta de envío de mail al GCM destino. */
    public SeleccionBolsin tomarSeleccionBolsinEnMapa(int numeroBolsin) {
        MarcadorBolsin marcador = marcadores.stream()
                .filter(m -> m.numeroBolsin() == numeroBolsin)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("El bolsín " + numeroBolsin + " no está marcado en el mapa"));
        bolsinSeleccionado = listaBolsinesEnviados.stream()
                .filter(b -> b.getNumeroBolsin() == numeroBolsin)
                .findFirst()
                .orElseThrow();
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = bolsinSeleccionado.getNumeroPrecinto();
        this.codigoCMdestino = bolsinSeleccionado.getCMDestino().getCodigo();
        return new SeleccionBolsin(marcador, "¿Desea enviar un correo electrónico al Gerente de la "
                + bolsinSeleccionado.getCMDestino().getNombre() + " para informar la ubicación del bolsín " + numeroBolsin + "?");
    }

    /** Pasos 9 a 11. A5 si el EB no confirma el envío del mail. */
    public ResultadoSeguimiento tomarConfirmacionDeMailCMGerenteDestino(boolean confirma) {
        if (bolsinSeleccionado == null) throw new IllegalStateException("Primero debe seleccionar un bolsín del mapa");
        if (!confirma) {
            finCU();
            return new ResultadoSeguimiento(false, "No se envió el correo electrónico. Fin del caso de uso.", null);
        }
        String email = buscarCorreoElectronico();
        NotificacionUbicacion notificacion = llamarCU31NotificarUbicacionBolsin(email);
        finCU();
        return new ResultadoSeguimiento(true, "Se notificó la ubicación del bolsín " + numeroBolsin + " al Gerente de la "
                + bolsinSeleccionado.getCMDestino().getNombre() + " (" + email + ").", notificacion);
    }

    /** Paso 10: email del Gerente de la CM destino del bolsín seleccionado. */
    public String buscarCorreoElectronico() {
        ComisionMedica destino = bolsinSeleccionado.getCMDestino();
        for (Empleado e : empleados.findAll()) {
            if (e.esTuCM(destino) && e.sosGCM()) return e.getEmail();
        }
        throw new NoSuchElementException("No hay Gerente registrado para la " + destino.getNombre());
    }

    /**
     * Paso 11: include del CU 31 Notificar ubicación de bolsín (número, fecha/hora de última actualización y coordenadas).
     * ponytail: se registra en el log; el envío real va por Microsoft Graph (RNF 5) cuando haya credenciales.
     */
    public NotificacionUbicacion llamarCU31NotificarUbicacionBolsin(String email) {
        String[] datos = datosLocalizacionBolsin.get(numeroBolsin);
        NotificacionUbicacion n = new NotificacionUbicacion(email, numeroBolsin, datos[3],
                Double.parseDouble(datos[1]), Double.parseDouble(datos[2]));
        log.info("CU 31 Notificar ubicación de bolsín -> {}", n);
        return n;
    }

    public void finCU() {
        log.info("Fin CU 36 - EB {} - CM {} - bolsín {}", encargadoLogueado.getNombreCompleto(), codigoCMorigen, numeroBolsin);
    }

    private static String motivo(Exception e) {
        if (e instanceof RestClientResponseException r) {
            return "El GPS Tracker no pudo informar la ubicación (HTTP " + r.getStatusCode().value() + ")";
        }
        return "El GPS Tracker no pudo informar la ubicación: " + e.getMessage();
    }
}
