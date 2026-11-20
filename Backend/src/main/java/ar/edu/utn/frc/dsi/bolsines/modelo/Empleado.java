package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class Empleado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    @ManyToOne(optional = false)
    private Rol rol;
    @ManyToOne(optional = false)
    private ComisionMedica asignadoA;
    @OneToOne(optional = false)
    private Usuario usuario;

    protected Empleado() {}

    public Empleado(String nombre, String apellido, String email, Rol rol, ComisionMedica asignadoA, Usuario usuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.asignadoA = asignadoA;
        this.usuario = usuario;
    }

    public boolean esTuCM(ComisionMedica cm) { return asignadoA.equals(cm); }

    public boolean esTuUsuario(Usuario u) { return usuario.getId().equals(u.getId()); }

    public ComisionMedica getCM() { return asignadoA; }

    public String getEmail() { return email; }

    public String mostrarCM() { return asignadoA.getNombre(); }

    public boolean sosGCM() { return rol.esGCM(); }

    public boolean sosEncargadoDeBolsines() { return rol.esEncargadoDeBolsines(); }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public Rol getRol() { return rol; }
    public Usuario getUsuario() { return usuario; }

    public String getNombreCompleto() { return nombre + " " + apellido; }
}
