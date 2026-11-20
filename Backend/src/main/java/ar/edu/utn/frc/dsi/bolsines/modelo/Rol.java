package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class Rol {
    public static final String GERENTE_CM = "Gerente de Comisión Médica";
    public static final String ENCARGADO_BOLSINES = "Encargado de Bolsines";
    public static final String ENCARGADO_DOCUMENTACION = "Encargado de Documentación";
    public static final String PARAMETRIZADOR = "Parametrizador del sistema";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nombre;
    private String descripcion;

    protected Rol() {}

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esGCM() { return GERENTE_CM.equals(nombre); }

    public boolean esEncargadoDeBolsines() { return ENCARGADO_BOLSINES.equals(nombre); }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
