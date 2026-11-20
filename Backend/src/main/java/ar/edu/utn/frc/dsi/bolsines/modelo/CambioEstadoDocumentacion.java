package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class CambioEstadoDocumentacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    @ManyToOne(optional = false)
    private Estado estado;
    @ManyToOne(optional = false)
    private Empleado responsableCE;

    protected CambioEstadoDocumentacion() {}

    public CambioEstadoDocumentacion(Estado estado, Empleado responsableCE) {
        this.estado = estado;
        this.responsableCE = responsableCE;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    public boolean sosActual() { return fechaHoraFin == null; }

    public Estado getEstado() { return estado; }
    public Empleado getResponsableCE() { return responsableCE; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
}
