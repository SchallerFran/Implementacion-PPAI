package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Documentacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Integer numero;
    @Column(nullable = false)
    private String asunto;
    @Column(nullable = false)
    private LocalDate fechaPase;
    @ManyToOne(optional = false)
    private TipoDocumento tipoDocumento;
    @ManyToOne(optional = false)
    private ComisionMedica cmOrigen;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "documentacion_id")
    @OrderBy("id")
    private List<Archivo> archivoAdjunto = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "documentacion_id")
    @OrderBy("id")
    private List<CambioEstadoDocumentacion> cambioEstado = new ArrayList<>();

    protected Documentacion() {}

    /** 7. Registrar Documentación / new(): queda en estado Registrada. */
    public Documentacion(int numero, TipoDocumento tipoDocumento, String asunto, LocalDate fechaPase,
                         ComisionMedica cmOrigen, List<Archivo> archivos, Estado registrada, Empleado responsable) {
        this.numero = numero;
        this.tipoDocumento = tipoDocumento;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.cmOrigen = cmOrigen;
        this.archivoAdjunto.addAll(archivos);
        crearCE(registrada, responsable);
    }

    public void crearCE(Estado estado, Empleado responsable) {
        cambioEstado.add(new CambioEstadoDocumentacion(estado, responsable));
    }

    /** Cierra el cambio de estado actual y crea uno nuevo, registrando el empleado responsable. */
    public void setEstado(Estado estado, Empleado responsable) {
        for (CambioEstadoDocumentacion ce : cambioEstado) {
            if (ce.sosActual()) ce.setFechaHoraFin(LocalDateTime.now());
        }
        crearCE(estado, responsable);
    }

    /** Al incluirse en un remito: Registrada -> EnRemito. */
    public void remitar(Estado enRemito, Empleado responsable) {
        Estado.validar(getEstadoActual(), "la documentación " + numero, "incluir en un remito", Estado.REGISTRADA);
        setEstado(enRemito, responsable);
    }

    /** Recepción conforme: -> RecibidaYAceptada. */
    public void aceptar(Estado recibidaYAceptada, Empleado responsable) {
        setEstado(recibidaYAceptada, responsable);
    }

    public Estado getEstadoActual() {
        for (CambioEstadoDocumentacion ce : cambioEstado) {
            if (ce.sosActual()) return ce.getEstado();
        }
        throw new IllegalStateException("La documentación " + numero + " no tiene estado actual");
    }

    public String getAsunto() { return asunto; }

    public String getDatosDocumentacion() {
        return "N° " + numero + " - " + mostrarTipoDocumentacion() + " - " + asunto;
    }

    public String mostrarTipoDocumentacion() { return tipoDocumento.getNombre(); }

    public Integer getNumero() { return numero; }
    public LocalDate getFechaPase() { return fechaPase; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public ComisionMedica getCmOrigen() { return cmOrigen; }
    public List<Archivo> getArchivoAdjunto() { return List.copyOf(archivoAdjunto); }
    public List<CambioEstadoDocumentacion> getCambioEstado() { return List.copyOf(cambioEstado); }
}
