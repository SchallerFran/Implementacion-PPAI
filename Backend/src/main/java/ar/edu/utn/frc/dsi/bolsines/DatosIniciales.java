package ar.edu.utn.frc.dsi.bolsines;

import ar.edu.utn.frc.dsi.bolsines.modelo.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ar.edu.utn.frc.dsi.bolsines.modelo.Estado.*;

/**
 * Carga datos de prueba si la base está vacía. Los bolsines 101 a 110 y sus CM coinciden con los
 * de la API mock del GPS Tracker; el 111 no existe en el GPS (sirve para probar el A3).
 * Usuarios: eb.&lt;cm&gt; (Encargado de Bolsines) y gcm.&lt;cm&gt; (Gerente) para cada CM. Contraseña: bolsines
 */
@Component
public class DatosIniciales implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    public static final String CONTRASENA = "bolsines";

    // codigo, nombre, localidad, provincia, alias de usuarios
    private static final String[][] COMISIONES = {
            {"CM-CBA-01", "Comisión Médica Córdoba", "Córdoba", "Córdoba", "cordoba"},
            {"CM-ROS-02", "Comisión Médica Rosario", "Rosario", "Santa Fe", "rosario"},
            {"CM-MDZ-03", "Comisión Médica Mendoza", "Mendoza", "Mendoza", "mendoza"},
            {"CM-BA-01", "Comisión Médica Central", "C.A.B.A.", "C.A.B.A.", "central"},
            {"CM-TUC-01", "Comisión Médica Tucumán", "San Miguel de Tucumán", "Tucumán", "tucuman"},
            {"CM-SLA-01", "Comisión Médica Salta", "Salta", "Salta", "salta"},
            {"CM-JUJ-01", "Comisión Médica Jujuy", "San Salvador de Jujuy", "Jujuy", "jujuy"},
            {"CM-NQN-01", "Comisión Médica Neuquén", "Neuquén", "Neuquén", "neuquen"},
            {"CM-MDQ-01", "Comisión Médica Mar del Plata", "Mar del Plata", "Buenos Aires", "mardelplata"},
            {"CM-LPL-01", "Comisión Médica La Plata", "La Plata", "Buenos Aires", "laplata"},
            {"CM-CBA-02", "Comisión Médica Villa María", "Villa María", "Córdoba", "villamaria"},
    };

    // numeroBolsin, origen, destino (mismos datos que la API mock del GPS Tracker)
    private static final String[][] BOLSINES_ENVIADOS = {
            {"101", "CM-CBA-01", "CM-ROS-02"}, {"102", "CM-ROS-02", "CM-MDZ-03"}, {"103", "CM-MDZ-03", "CM-BA-01"},
            {"104", "CM-BA-01", "CM-CBA-01"}, {"105", "CM-TUC-01", "CM-SLA-01"}, {"106", "CM-SLA-01", "CM-JUJ-01"},
            {"107", "CM-NQN-01", "CM-MDQ-01"}, {"108", "CM-MDQ-01", "CM-BA-01"}, {"109", "CM-LPL-01", "CM-CBA-01"},
            {"110", "CM-CBA-02", "CM-ROS-02"}, {"111", "CM-CBA-01", "CM-MDZ-03"},
            // más bolsines en tránsito desde Córdoba (también agregados al mock del GPS local)
            {"122", "CM-CBA-01", "CM-BA-01"}, {"123", "CM-CBA-01", "CM-MDZ-03"}, {"124", "CM-CBA-01", "CM-TUC-01"},
            {"125", "CM-CBA-01", "CM-SLA-01"}, {"126", "CM-CBA-01", "CM-NQN-01"},
    };

    private final EntityManager em;
    private final Map<String, Estado> estados = new HashMap<>();
    private final Map<String, ComisionMedica> cms = new HashMap<>();
    private final Map<String, Empleado> encargados = new HashMap<>();
    private final Map<String, TipoDocumento> tipos = new HashMap<>();
    private int nroDocumentacion = 1;
    private int nroRemito = 1;

    public DatosIniciales(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (em.createQuery("select count(e) from Estado e", Long.class).getSingleResult() > 0) return;
        log.info("Base vacía: cargando datos iniciales");
        cargarEstados();
        cargarTiposDocumento();
        cargarComisionesYEmpleados();
        for (String[] b : BOLSINES_ENVIADOS) {
            bolsinEnviado(Integer.parseInt(b[0]), b[1], b[2]);
        }
        // 105 ya fue recibido: Tucumán no tiene bolsines Enviado (A1 del CU 36)
        recibir(105);
        // documentación registrada en Córdoba lista para armar remitos desde el frontend
        documentacion("CM-CBA-01", "Expediente", "Expediente de incapacidad laboral - legajo 4521");
        documentacion("CM-CBA-01", "Estudio médico", "Resonancia magnética columna lumbar - legajo 4521");
        log.info("Datos iniciales cargados. Usuarios eb.<cm> / gcm.<cm>, contraseña '{}'", CONTRASENA);
    }

    private void cargarEstados() {
        String[][] datos = {
                {AMBITO_BOLSIN, CREADO, "Estado inicial. Permite modificaciones o el cierre del bolsín"},
                {AMBITO_BOLSIN, CERRADO, "Cerrado/precintado y listo para ser enviado. Puede reabrirse"},
                {AMBITO_BOLSIN, ENVIADO, "Enviado, en camino hacia la comisión médica de destino"},
                {AMBITO_BOLSIN, RECIBIDO, "Recibido en la comisión médica de destino"},
                {AMBITO_BOLSIN, DE_BAJA, "Dado de baja del sistema"},
                {AMBITO_REMITO, CREADO, "Estado inicial. Permite modificaciones y la asignación a bolsines"},
                {AMBITO_REMITO, CANCELADO, "Cancelado antes de ser enviado"},
                {AMBITO_REMITO, EN_BOLSIN_SALIENTE, "Asignado a un bolsín listo para ser enviado"},
                {AMBITO_REMITO, EN_BOLSIN_ENVIADO, "El bolsín fue enviado y está en camino"},
                {AMBITO_REMITO, RECIBIDO_Y_ACEPTADO_PARCIAL, "Recibido; el contenido no coincide completamente con lo registrado"},
                {AMBITO_REMITO, RECIBIDO_Y_ACEPTADO, "Recibido; el contenido coincide con lo registrado"},
                {AMBITO_DOCUMENTACION, REGISTRADA, "Registrada en el sistema"},
                {AMBITO_DOCUMENTACION, EN_REMITO, "Incluida en un remito"},
                {AMBITO_DOCUMENTACION, EN_BOLSIN_SALIENTE, "Su remito fue agregado a un bolsín"},
                {AMBITO_DOCUMENTACION, EN_BOLSIN_ENVIADO, "Su bolsín fue retirado por el correo"},
                {AMBITO_DOCUMENTACION, RECIBIDA_Y_ACEPTADA, "Recibida y aceptada en la CM destino"},
                {AMBITO_DOCUMENTACION, NO_RECIBIDA, "No llegó en el bolsín"},
                {AMBITO_DOCUMENTACION, RECIBIDA_Y_RECHAZADA, "No corresponde a la CM destino"},
                {AMBITO_DOCUMENTACION, PARA_REDIRIGIR, "Debe redirigirse a un área de la sede central"},
                {AMBITO_DOCUMENTACION, DE_BAJA, "Dada de baja"},
        };
        for (String[] d : datos) {
            Estado e = new Estado(d[0], d[1], d[2]);
            em.persist(e);
            estados.put(d[0] + d[1], e);
        }
    }

    private void cargarTiposDocumento() {
        for (String nombre : List.of("Expediente", "Dictamen", "Estudio médico", "Carta documento")) {
            TipoDocumento t = new TipoDocumento(nombre, nombre);
            em.persist(t);
            tipos.put(nombre, t);
        }
    }

    private void cargarComisionesYEmpleados() {
        Rol eb = new Rol(Rol.ENCARGADO_BOLSINES, "Gestión de bolsines, retiro, recepción, cierre, re-apertura y seguimiento");
        Rol gcm = new Rol(Rol.GERENTE_CM, "Reportes y estadísticas del movimiento de bolsines");
        em.persist(eb);
        em.persist(gcm);
        em.persist(new Rol(Rol.ENCARGADO_DOCUMENTACION, "Gestión de documentación y remitos"));
        em.persist(new Rol(Rol.PARAMETRIZADOR, "Parametrización de usuarios, tipos de documento y comisiones"));

        Map<String, Provincia> provincias = new HashMap<>();
        for (String[] c : COMISIONES) {
            Provincia p = provincias.computeIfAbsent(c[3], nombre -> {
                Provincia nueva = new Provincia(nombre, nombre);
                em.persist(nueva);
                return nueva;
            });
            Localidad l = new Localidad(c[2], c[2], p);
            em.persist(l);
            ComisionMedica cm = new ComisionMedica(c[0], c[1], "Av. Principal 100, " + c[2], "cm." + c[4] + "@bolsines.test", "0800-555-0000", l);
            em.persist(cm);
            cms.put(c[0], cm);
            encargados.put(c[0], empleado("eb." + c[4], "Encargado de Bolsines", c[2], eb, cm));
            empleado("gcm." + c[4], "Gerente", c[2], gcm, cm);
        }
    }

    private Empleado empleado(String usuario, String nombre, String apellido, Rol rol, ComisionMedica cm) {
        Usuario u = new Usuario(usuario, CONTRASENA);
        em.persist(u);
        Empleado e = new Empleado(nombre, apellido, usuario + "@bolsines.test", rol, cm, u);
        em.persist(e);
        return e;
    }

    private Documentacion documentacion(String codigoCM, String tipo, String asunto) {
        Empleado responsable = encargados.get(codigoCM);
        Documentacion d = new Documentacion(nroDocumentacion++, tipos.get(tipo), asunto, LocalDate.now(), cms.get(codigoCM),
                List.of(new Archivo("doc-" + nroDocumentacion + ".pdf", asunto)), doc(REGISTRADA), responsable);
        em.persist(d);
        return d;
    }

    /** Recorre la máquina de estados: generar remito -> generar bolsín -> precintar -> registrar retiro. */
    private void bolsinEnviado(int numero, String origen, String destino) {
        Empleado eb = encargados.get(origen);
        List<Documentacion> docs = List.of(
                documentacion(origen, "Expediente", "Expediente de incapacidad laboral N° " + (1000 + numero)),
                documentacion(origen, "Dictamen", "Dictamen médico N° " + (2000 + numero)));
        Remito r = new Remito(nroRemito++, cms.get(origen), cms.get(destino), docs, null, rem(CREADO), doc(EN_REMITO), eb);
        em.persist(r);
        Bolsin b = new Bolsin(numero, cms.get(origen), cms.get(destino), 850, List.of(r),
                bol(CREADO), rem(EN_BOLSIN_SALIENTE), doc(EN_BOLSIN_SALIENTE), eb);
        em.persist(b);
        b.precintar("P-" + (500000 + numero), bol(CERRADO), eb);
        b.enviar(bol(ENVIADO), rem(EN_BOLSIN_ENVIADO), doc(EN_BOLSIN_ENVIADO), eb);
    }

    private void recibir(int numero) {
        Bolsin b = em.createQuery("select b from Bolsin b where b.numeroBolsin = :n", Bolsin.class)
                .setParameter("n", numero).getSingleResult();
        b.recibir(bol(RECIBIDO), rem(RECIBIDO_Y_ACEPTADO), rem(RECIBIDO_Y_ACEPTADO_PARCIAL),
                doc(RECIBIDA_Y_ACEPTADA), doc(NO_RECIBIDA), List.of(), encargados.get(b.getCMDestino().getCodigo()));
    }

    private Estado bol(String nombre) { return estados.get(AMBITO_BOLSIN + nombre); }
    private Estado rem(String nombre) { return estados.get(AMBITO_REMITO + nombre); }
    private Estado doc(String nombre) { return estados.get(AMBITO_DOCUMENTACION + nombre); }
}
