package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class Provincia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nombre;
    private String descripcion;

    protected Provincia() {}

    public Provincia(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
