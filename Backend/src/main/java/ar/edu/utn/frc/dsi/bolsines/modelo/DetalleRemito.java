package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class DetalleRemito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Área de la sede central a la que va la documentación (solo si el destino es la CMC). */
    private String areaCMCDestino;
    @ManyToOne(optional = false)
    private Documentacion documentacion;

    protected DetalleRemito() {}

    public DetalleRemito(Documentacion documentacion, String areaCMCDestino) {
        this.documentacion = documentacion;
        this.areaCMCDestino = areaCMCDestino;
    }

    public void aceptarDocumentacion(Estado recibidaYAceptada, Empleado responsable) {
        documentacion.aceptar(recibidaYAceptada, responsable);
    }

    public void actualizarEstadoDoc(Estado estado, Empleado responsable) {
        documentacion.setEstado(estado, responsable);
    }

    public Documentacion getDocumentacion() { return documentacion; }

    public String mostrarDocumentacion() { return documentacion.getDatosDocumentacion(); }

    public String getAreaCMCDestino() { return areaCMCDestino; }
}
