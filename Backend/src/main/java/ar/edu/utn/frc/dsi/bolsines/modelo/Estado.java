package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ambito", "nombre"}))
public class Estado {
    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";
    public static final String AMBITO_DOCUMENTACION = "Documentacion";

    // Bolsín (máquina de estados de Bolsín)
    public static final String CREADO = "Creado";
    public static final String CERRADO = "Cerrado";
    public static final String ENVIADO = "Enviado";
    public static final String RECIBIDO = "Recibido";
    public static final String DE_BAJA = "DeBaja";
    // Remito (máquina de estados de Remito; "Creado" se comparte)
    public static final String CANCELADO = "Cancelado";
    public static final String EN_BOLSIN_SALIENTE = "EnBolsinSaliente";
    public static final String EN_BOLSIN_ENVIADO = "EnBolsinEnviado";
    public static final String RECIBIDO_Y_ACEPTADO_PARCIAL = "RecibidoYAceptadoParcial";
    public static final String RECIBIDO_Y_ACEPTADO = "RecibidoYAceptado";
    // Documentación (reglas de negocio de la ERS; EnBolsinSaliente/EnBolsinEnviado/DeBaja se comparten)
    public static final String REGISTRADA = "Registrada";
    public static final String EN_REMITO = "EnRemito";
    public static final String RECIBIDA_Y_ACEPTADA = "RecibidaYAceptada";
    public static final String NO_RECIBIDA = "NoRecibida";
    public static final String RECIBIDA_Y_RECHAZADA = "RecibidaYRechazada";
    public static final String PARA_REDIRIGIR = "ParaRedirigir";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String ambito;
    @Column(nullable = false)
    private String nombre;
    private String descripcion;

    protected Estado() {}

    public Estado(String ambito, String nombre, String descripcion) {
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esAmbitoBolsin() { return AMBITO_BOLSIN.equals(ambito); }
    public boolean esAmbitoRemito() { return AMBITO_REMITO.equals(ambito); }
    public boolean esAmbitoDocumentacion() { return AMBITO_DOCUMENTACION.equals(ambito); }

    public boolean esEnviado() { return esAmbitoBolsin() && ENVIADO.equals(nombre); }
    public boolean esRecibidoEnCMDestino() { return esAmbitoBolsin() && RECIBIDO.equals(nombre); }
    public boolean esRecibidoYAceptado() { return esAmbitoRemito() && RECIBIDO_Y_ACEPTADO.equals(nombre); }
    public boolean esRecibidaYAceptada() { return esAmbitoDocumentacion() && RECIBIDA_Y_ACEPTADA.equals(nombre); }

    /** Usado por las condiciones de guarda de las máquinas de estado. */
    public boolean es(String nombre) { return this.nombre.equals(nombre); }

    public String getAmbito() { return ambito; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public void setAmbito(String ambito) { this.ambito = ambito; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Lanza 409 si el estado actual no es ninguno de los permitidos por la máquina de estados. */
    static void validar(Estado actual, String entidad, String operacion, String... permitidos) {
        for (String p : permitidos) if (actual.es(p)) return;
        throw new IllegalStateException("No se puede " + operacion + " " + entidad + " en estado "
                + actual.getNombre() + " (requiere: " + String.join(" o ", permitidos) + ")");
    }
}
