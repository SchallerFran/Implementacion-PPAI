package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

@Entity
public class Archivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombreArchivo;
    private String tituloDocumento;

    protected Archivo() {}

    public Archivo(String nombreArchivo, String tituloDocumento) {
        this.nombreArchivo = nombreArchivo;
        this.tituloDocumento = tituloDocumento;
    }

    public String getNombreArchivo() { return nombreArchivo; }
    public String getTituloDocumento() { return tituloDocumento; }
}
