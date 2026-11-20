package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class Localidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    private String descripcion;
    @ManyToOne(optional = false)
    private Provincia provincia;

    protected Localidad() {}

    public Localidad(String nombre, String descripcion, Provincia provincia) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.provincia = provincia;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Provincia getProvincia() { return provincia; }
}
