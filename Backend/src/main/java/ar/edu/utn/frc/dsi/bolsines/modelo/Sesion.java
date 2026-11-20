package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
public class Sesion {
    /** El id viaja como token en el header X-Sesion-Id: UUID para que no sea adivinable. */
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDate fechaInicio;
    private LocalTime horaInicio;
    private LocalDate fechaFin;
    private LocalTime horaFin;
    @ManyToOne(optional = false)
    private Usuario usuario;

    protected Sesion() {}

    public Sesion(Usuario usuario) {
        this.usuario = usuario;
        this.fechaInicio = LocalDate.now();
        this.horaInicio = LocalTime.now();
    }

    public Usuario getUsuario() { return usuario; }

    public UUID getId() { return id; }

    // ponytail: sin expiración por tiempo, la sesión vive hasta cerrar(); agregar vencimiento si se despliega fuera del aula
    public boolean estaActiva() { return fechaFin == null; }

    public void cerrar() {
        this.fechaFin = LocalDate.now();
        this.horaFin = LocalTime.now();
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public LocalTime getHoraFin() { return horaFin; }
}
