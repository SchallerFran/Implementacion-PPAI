package ar.edu.utn.frc.dsi.bolsines;

import ar.edu.utn.frc.dsi.bolsines.gps.*;
import ar.edu.utn.frc.dsi.bolsines.modelo.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static ar.edu.utn.frc.dsi.bolsines.modelo.Estado.*;
import static org.junit.jupiter.api.Assertions.*;

/** Sin Spring ni base: adapters del GPS y máquinas de estado de Bolsín/Remito. */
class BolsinesTest {

    private static final String[] ESPERADO = {"101", "-31.4201", "-64.1888", "2026-03-07T12:30:00"};

    @Test
    void losTresAdaptersDevuelvenElMismoFormato() {
        var xtr = new ServicioGPSXTR(null) {
            @Override public String getBolsinLocation(String k, int n, String o) {
                return "{\"items\":[{\"numeroBolsin\":101,\"latitud\":-31.4201,\"longitud\":-64.1888,\"fechaHoraUltimaActualizacion\":\"2026-03-07T12:30:00\"}]}";
            }
        };
        var nav = new ServicioGPSNavTrack(null) {
            @Override public String retrieveTrackingData(String k, int n, String d) { return "101,-31.4201,-64.1888,2026-03-07T12:30"; }
        };
        var geo = new ServicioGPSGeoPulse(null) {
            @Override public String[][] fetchCargoPositions(String k, int n) {
                return new String[][]{{"101", "-31.4201", "-64.1888", "2026-03-07T12:30:00"}};
            }
        };
        List<IDatosLocalizacion> adapters = List.of(new AdapterGPSXTR("k", xtr), new AdapterGPSNavTrack("k", nav), new AdapterGPSGeoPulse("k", geo));
        for (IDatosLocalizacion a : adapters) {
            String[][] filas = a.recibirDatos("k", 101, "CM-CBA-01", "CM-ROS-02");
            assertEquals(1, filas.length, a.getClass().getSimpleName());
            assertArrayEquals(ESPERADO, filas[0], a.getClass().getSimpleName());
        }
    }

    @Test
    void respuestaInvalidaDelGpsEsIllegalState() {
        var nav = new ServicioGPSNavTrack(null) {
            @Override public String retrieveTrackingData(String k, int n, String d) { return "basura"; }
        };
        assertThrows(IllegalStateException.class, () -> new AdapterGPSNavTrack("k", nav).recibirDatos("k", 1, "a", "b"));
    }

    @Test
    void cicloDeVidaDelBolsinYSusRemitos() {
        Localidad loc = new Localidad("Córdoba", "", new Provincia("Córdoba", ""));
        ComisionMedica cba = new ComisionMedica("CM-CBA-01", "CM Córdoba", "", "", "", loc);
        ComisionMedica ros = new ComisionMedica("CM-ROS-02", "CM Rosario", "", "", "", loc);
        Empleado eb = new Empleado("EB", "Test", "eb@test", new Rol(Rol.ENCARGADO_BOLSINES, ""), cba, null);
        TipoDocumento exp = new TipoDocumento("Expediente", "");

        Documentacion d1 = new Documentacion(1, exp, "uno", LocalDate.now(), cba, List.of(), doc(REGISTRADA), eb);
        Documentacion d2 = new Documentacion(2, exp, "dos", LocalDate.now(), cba, List.of(), doc(REGISTRADA), eb);
        Remito r1 = new Remito(1, cba, ros, List.of(d1), null, rem(CREADO), doc(EN_REMITO), eb);
        Remito r2 = new Remito(2, cba, ros, List.of(d2), null, rem(CREADO), doc(EN_REMITO), eb);
        assertTrue(d1.getEstadoActual().es(EN_REMITO));

        Bolsin b = new Bolsin(100, cba, ros, 500, List.of(r1, r2), bol(CREADO), rem(EN_BOLSIN_SALIENTE), doc(EN_BOLSIN_SALIENTE), eb);
        assertTrue(r1.getEstado().es(EN_BOLSIN_SALIENTE));

        // modificar: se quita r2 -> vuelve a Creado y su documentación a EnRemito
        b.modificar(List.of(r1), rem(CREADO), doc(EN_REMITO), rem(EN_BOLSIN_SALIENTE), doc(EN_BOLSIN_SALIENTE), eb);
        assertTrue(r2.estasGenerado());
        assertTrue(d2.getEstadoActual().es(EN_REMITO));

        b.precintar("P-1", bol(CERRADO), eb);
        assertThrows(IllegalStateException.class, () -> b.modificar(List.of(r1), null, null, null, null, eb));
        b.abrir(bol(CREADO), eb);
        assertNull(b.getNumeroPrecinto());
        b.precintar("P-2", bol(CERRADO), eb);

        assertFalse(b.sosEnviado());
        b.enviar(bol(ENVIADO), rem(EN_BOLSIN_ENVIADO), doc(EN_BOLSIN_ENVIADO), eb);
        assertTrue(b.sosEnviado());
        assertThrows(IllegalStateException.class, () -> b.eliminar(bol(DE_BAJA), rem(CREADO), doc(EN_REMITO), eb));

        // recepción con la documentación 1 faltante -> remito parcial
        b.recibir(bol(RECIBIDO), rem(RECIBIDO_Y_ACEPTADO), rem(RECIBIDO_Y_ACEPTADO_PARCIAL),
                doc(RECIBIDA_Y_ACEPTADA), doc(NO_RECIBIDA), List.of(1), eb);
        assertTrue(b.getEstadoActual().esRecibidoEnCMDestino());
        assertTrue(r1.getEstado().es(RECIBIDO_Y_ACEPTADO_PARCIAL));
        assertTrue(d1.getEstadoActual().es(NO_RECIBIDA));
        assertEquals(6, b.getCambioEstado().size()); // Creado, Cerrado, Creado, Cerrado, Enviado, Recibido
    }

    private static Estado bol(String n) { return new Estado(AMBITO_BOLSIN, n, ""); }
    private static Estado rem(String n) { return new Estado(AMBITO_REMITO, n, ""); }
    private static Estado doc(String n) { return new Estado(AMBITO_DOCUMENTACION, n, ""); }
}
