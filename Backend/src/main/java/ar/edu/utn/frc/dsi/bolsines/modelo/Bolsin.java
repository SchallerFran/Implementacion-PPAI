package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ar.edu.utn.frc.dsi.bolsines.modelo.Estado.*;

/**
 * Máquina de estados de Bolsín:
 * Creado -(precintar)-> Cerrado -(enviar)-> Enviado -(recibir)-> Recibido
 * Cerrado -(abrir)-> Creado ; Creado|Cerrado -(eliminar)-> DeBaja ; Creado -(modificar)-> Creado
 */
@Entity
public class Bolsin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false, unique = true)
    private Integer numeroBolsin;
    private String numeroPrecinto;
    /** Peso en gramos. */
    @Column(nullable = false)
    private Integer peso;
    @ManyToOne(optional = false)
    private ComisionMedica origen;
    @ManyToOne(optional = false)
    private ComisionMedica destino;
    // ManyToMany: un bolsín dado de baja conserva sus remitos como historial y el remito puede ir en otro bolsín
    @ManyToMany
    @JoinTable(name = "bolsin_remito")
    @OrderBy("numero")
    private List<Remito> remito = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("id")
    private List<CambioEstadoBolsin> cambioEstado = new ArrayList<>();

    protected Bolsin() {}

    /** 19. Generar Bolsín / new(): Bolsín en Creado, remitos -> EnBolsinSaliente. */
    public Bolsin(int numeroBolsin, ComisionMedica origen, ComisionMedica destino, int peso, List<Remito> remitos,
                  Estado creado, Estado remitoEnBolsinSaliente, Estado docEnBolsinSaliente, Empleado responsable) {
        if (origen.equals(destino)) throw new IllegalArgumentException("La CM destino debe ser distinta a la CM origen");
        if (remitos.isEmpty()) throw new IllegalArgumentException("El bolsín debe contener al menos un remito");
        this.numeroBolsin = numeroBolsin;
        this.fecha = LocalDate.now();
        this.origen = origen;
        this.destino = destino;
        setDetalleBolsin(peso);
        crearCEBolsin(creado, responsable);
        for (Remito r : remitos) asociarRemito(r, remitoEnBolsinSaliente, docEnBolsinSaliente, responsable);
    }

    public void asociarRemito(Remito r, Estado remitoEnBolsinSaliente, Estado docEnBolsinSaliente, Empleado responsable) {
        if (!r.tenesEstaCMOrigen(origen) || !r.tenesEstaCMDestino(destino)) {
            throw new IllegalArgumentException("El remito " + r.getNumero() + " no va de " + origen.getNombre() + " a " + destino.getNombre());
        }
        r.asignar(remitoEnBolsinSaliente, docEnBolsinSaliente, responsable);
        remito.add(r);
    }

    public void crearCEBolsin(Estado estado, Empleado responsable) {
        if (!estado.esAmbitoBolsin()) throw new IllegalArgumentException("El estado " + estado.getNombre() + " no es de ámbito Bolsín");
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) ce.setFechaHoraFin(LocalDateTime.now());
        }
        cambioEstado.add(new CambioEstadoBolsin(estado, responsable));
    }

    /** 20. Modificar Bolsín / modificar(): solo en Creado. Quita remitos completos (vuelven a Creado) o agrega nuevos. */
    public void modificar(List<Remito> nuevos, Estado remitoCreado, Estado docEnRemito,
                          Estado remitoEnBolsinSaliente, Estado docEnBolsinSaliente, Empleado responsable) {
        validarEstado("modificar", CREADO);
        if (nuevos.isEmpty()) throw new IllegalArgumentException("El bolsín debe contener al menos un remito; para darlo de baja use eliminar");
        List<Integer> numerosNuevos = nuevos.stream().map(Remito::getNumero).toList();
        List<Integer> numerosActuales = remito.stream().map(Remito::getNumero).toList();
        remito.removeIf(r -> {
            boolean quitar = !numerosNuevos.contains(r.getNumero());
            if (quitar) r.desasignar(remitoCreado, docEnRemito, responsable);
            return quitar;
        });
        for (Remito r : nuevos) {
            if (!numerosActuales.contains(r.getNumero())) asociarRemito(r, remitoEnBolsinSaliente, docEnBolsinSaliente, responsable);
        }
    }

    /** 39. Cerrar Bolsín / precintar(): Creado -> Cerrado. */
    public void precintar(String numeroPrecinto, Estado cerrado, Empleado responsable) {
        validarEstado("precintar", CREADO);
        if (numeroPrecinto == null || numeroPrecinto.isBlank()) throw new IllegalArgumentException("Debe ingresar el número de precinto");
        this.numeroPrecinto = numeroPrecinto.strip();
        crearCEBolsin(cerrado, responsable);
    }

    /** 40. Reabrir Bolsín / abrir(): Cerrado -> Creado, se borra el precinto (se rompió). */
    public void abrir(Estado creado, Empleado responsable) {
        validarEstado("reabrir", CERRADO);
        this.numeroPrecinto = null;
        crearCEBolsin(creado, responsable);
    }

    /** 21. Eliminar Bolsín / eliminar(): Creado|Cerrado -> DeBaja, remitos -> Creado, documentación -> EnRemito. */
    public void eliminar(Estado deBaja, Estado remitoCreado, Estado docEnRemito, Empleado responsable) {
        validarEstado("eliminar", CREADO, CERRADO);
        for (Remito r : remito) r.desasignar(remitoCreado, docEnRemito, responsable);
        crearCEBolsin(deBaja, responsable);
    }

    /** 27. Registrar retiro de bolsines / enviar(): Cerrado -> Enviado, remitos y documentación -> EnBolsinEnviado. */
    public void enviar(Estado enviado, Estado remitoEnBolsinEnviado, Estado docEnBolsinEnviado, Empleado responsable) {
        validarEstado("enviar", CERRADO);
        for (Remito r : remito) r.retirar(remitoEnBolsinEnviado, docEnBolsinEnviado, responsable);
        crearCEBolsin(enviado, responsable);
    }

    /**
     * 28. Registrar recepción de bolsín / recibir(): Enviado -> Recibido.
     * Remitos con alguna documentación no recibida quedan RecibidoYAceptadoParcial (esa documentación: NoRecibida),
     * el resto RecibidoYAceptado (documentación: RecibidaYAceptada).
     */
    public void recibir(Estado recibido, Estado remitoRecibidoYAceptado, Estado remitoRecibidoYAceptadoParcial,
                        Estado docRecibidaYAceptada, Estado docNoRecibida,
                        Collection<Integer> documentacionesNoRecibidas, Empleado responsable) {
        validarEstado("recibir", ENVIADO);
        List<Integer> contenidas = remito.stream().flatMap(r -> r.buscarDocumentacion().stream()).map(Documentacion::getNumero).toList();
        for (Integer n : documentacionesNoRecibidas) {
            if (!contenidas.contains(n)) throw new IllegalArgumentException("La documentación " + n + " no pertenece al bolsín " + numeroBolsin);
        }
        for (Remito r : remito) {
            if (r.contieneAlgunaDocumentacion(documentacionesNoRecibidas)) {
                r.recibirYAceptarParcial(remitoRecibidoYAceptadoParcial, docRecibidaYAceptada, docNoRecibida, documentacionesNoRecibidas, responsable);
            } else {
                r.recibirYAceptar(remitoRecibidoYAceptado, docRecibidaYAceptada, responsable);
            }
        }
        crearCEBolsin(recibido, responsable);
    }

    public boolean sosEnviado() {
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) return ce.sosEnviado();
        }
        return false;
    }

    public Estado getEstadoActual() {
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) return ce.getEstado();
        }
        throw new IllegalStateException("El bolsín " + numeroBolsin + " no tiene estado actual");
    }

    private void validarEstado(String operacion, String... permitidos) {
        Estado.validar(getEstadoActual(), "el bolsín " + numeroBolsin, operacion, permitidos);
    }

    public boolean esTuComisionMedicaOrigen(ComisionMedica cm) { return origen.equals(cm); }

    /** Nombre del modelo de dominio; el diseño lo llama esTuComisionMedicaOrigen(). */
    public boolean esTuCMOrigen(ComisionMedica cm) { return esTuComisionMedicaOrigen(cm); }

    public ComisionMedica getCMDestino() { return destino; }

    /** Nombre del modelo de dominio; el diseño lo llama getCMDestino(). */
    public ComisionMedica obtenerCMDestino() { return getCMDestino(); }

    public String getNumeroPrecinto() { return numeroPrecinto; }

    /** Nombre del modelo de dominio; el diseño lo llama getNumeroPrecinto(). */
    public String getNroPrecinto() { return getNumeroPrecinto(); }

    public Integer getNumeroBolsin() { return numeroBolsin; }

    public void setDetalleBolsin(int peso) {
        if (peso <= 0) throw new IllegalArgumentException("El peso (en gramos) debe ser mayor a cero");
        this.peso = peso;
    }

    public List<Remito> obtenerInformacionRemito() { return List.copyOf(remito); }

    public String mostrarDatos() {
        return "Bolsín N° " + numeroBolsin + " - " + fecha + " - " + origen.getNombre() + " -> " + destino.getNombre()
                + " - " + peso + " g - precinto " + numeroPrecinto + " - " + getEstadoActual().getNombre();
    }

    public LocalDate getFecha() { return fecha; }
    public Integer getPeso() { return peso; }
    public ComisionMedica getOrigen() { return origen; }
    public List<CambioEstadoBolsin> getCambioEstado() { return List.copyOf(cambioEstado); }
}
