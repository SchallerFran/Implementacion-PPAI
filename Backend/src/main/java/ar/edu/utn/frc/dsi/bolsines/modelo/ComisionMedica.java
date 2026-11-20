package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class ComisionMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Código con el que la identifican los GPS Tracker (ej: CM-CBA-01). */
    @Column(nullable = false, unique = true)
    private String codigo;
    @Column(nullable = false)
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;
    @ManyToOne(optional = false)
    private Localidad localidad;

    protected ComisionMedica() {}

    public ComisionMedica(String codigo, String nombre, String direccion, String email, String telefono, Localidad localidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
        this.localidad = localidad;
    }

    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public String getDireccion() { return direccion; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public Localidad getLocalidad() { return localidad; }

    // el código es la clave natural: dos instancias con el mismo código son la misma CM
    @Override
    public boolean equals(Object o) {
        return o instanceof ComisionMedica cm && codigo.equals(cm.codigo);
    }

    @Override
    public int hashCode() { return codigo.hashCode(); }
}
