package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ar.edu.utn.frc.dsi.bolsines.modelo.Estado.*;

/**
 * Máquina de estados de Remito:
 * Creado -(asignar)-> EnBolsinSaliente -(retirar)-> EnBolsinEnviado -(recibirYAceptar | recibirYAceptarParcial)-> RecibidoYAceptado | RecibidoYAceptadoParcial
 * Creado -(cancelar)-> Cancelado ; EnBolsinSaliente -(desasignar)-> Creado ; Creado -(modificar)-> Creado
 */
@Entity
public class Remito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false, unique = true)
    private Integer numero;
    @ManyToOne(optional = false)
    private ComisionMedica origen;
    @ManyToOne(optional = false)
    private ComisionMedica destino;
    @ManyToOne(optional = false)
    private Estado estado;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "remito_id")
    @OrderBy("id")
    private List<DetalleRemito> detalleRemito = new ArrayList<>();

    protected Remito() {}

    /** 15. Generar Remito / new(): Remito en Creado, documentación Registrada -> EnRemito. */
    public Remito(int numero, ComisionMedica origen, ComisionMedica destino, List<Documentacion> documentaciones,
                  String areaCMCDestino, Estado creado, Estado docEnRemito, Empleado responsable) {
        if (origen.equals(destino)) throw new IllegalArgumentException("La CM destino debe ser distinta a la CM origen");
        if (documentaciones.isEmpty()) throw new IllegalArgumentException("El remito debe incluir al menos una documentación");
        this.numero = numero;
        this.fecha = LocalDate.now();
        this.origen = origen;
        this.destino = destino;
        setEstado(creado);
        tomarDocumentacion(documentaciones, areaCMCDestino, docEnRemito, responsable);
    }

    public void tomarDocumentacion(List<Documentacion> documentaciones, String areaCMCDestino, Estado docEnRemito, Empleado responsable) {
        for (Documentacion d : documentaciones) {
            if (!d.getCmOrigen().equals(origen)) {
                throw new IllegalArgumentException("La documentación " + d.getNumero() + " no fue registrada en la CM origen del remito");
            }
            d.remitar(docEnRemito, responsable);
            detalleRemito.add(new DetalleRemito(d, areaCMCDestino));
        }
    }

    /** 16. Modificar Remito / modificar(): solo en Creado. Quita y agrega documentación. */
    public void modificar(List<Documentacion> nuevas, String areaCMCDestino, Estado docRegistrada, Estado docEnRemito, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "modificar", CREADO);
        modificarDocumentacionIncluida(nuevas, areaCMCDestino, docRegistrada, docEnRemito, responsable);
    }

    public void modificarDocumentacionIncluida(List<Documentacion> nuevas, String areaCMCDestino, Estado docRegistrada, Estado docEnRemito, Empleado responsable) {
        if (nuevas.isEmpty()) throw new IllegalArgumentException("El remito debe incluir al menos una documentación; para anularlo use cancelar");
        List<Integer> numerosNuevos = nuevas.stream().map(Documentacion::getNumero).toList();
        List<Integer> numerosActuales = buscarDocumentacion().stream().map(Documentacion::getNumero).toList();
        detalleRemito.removeIf(d -> {
            boolean quitar = !numerosNuevos.contains(d.getDocumentacion().getNumero());
            if (quitar) d.actualizarEstadoDoc(docRegistrada, responsable);
            return quitar;
        });
        tomarDocumentacion(nuevas.stream().filter(d -> !numerosActuales.contains(d.getNumero())).toList(),
                areaCMCDestino, docEnRemito, responsable);
    }

    /** 17. Cancelar Remito / cancelar(): Creado -> Cancelado, documentación -> Registrada. */
    public void cancelar(Estado cancelado, Estado docRegistrada, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "cancelar", CREADO);
        actualizarEstadoDoc(docRegistrada, responsable);
        setEstado(cancelado);
    }

    /** 19/20. Generar o Modificar Bolsín / asignar(): Creado -> EnBolsinSaliente. */
    public void asignar(Estado enBolsinSaliente, Estado docEnBolsinSaliente, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "asignar a un bolsín", CREADO);
        setEstado(enBolsinSaliente);
        actualizarEstadoDoc(docEnBolsinSaliente, responsable);
    }

    /** 20/21. Modificar o Eliminar Bolsín / desasignar(): EnBolsinSaliente -> Creado, documentación -> EnRemito. */
    public void desasignar(Estado creado, Estado docEnRemito, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "quitar del bolsín", EN_BOLSIN_SALIENTE);
        setEstado(creado);
        actualizarEstadoDoc(docEnRemito, responsable);
    }

    /** 27. Registrar retiro de bolsines / retirar(): EnBolsinSaliente -> EnBolsinEnviado. */
    public void retirar(Estado enBolsinEnviado, Estado docEnBolsinEnviado, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "retirar", EN_BOLSIN_SALIENTE);
        setEstado(enBolsinEnviado);
        actualizarEstadoDoc(docEnBolsinEnviado, responsable);
    }

    /** 28. Registrar recepción [contenido igual al registrado = SI] / recibirYAceptar(). */
    public void recibirYAceptar(Estado recibidoYAceptado, Estado docRecibidaYAceptada, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "recibir", EN_BOLSIN_ENVIADO);
        setEstado(recibidoYAceptado);
        for (DetalleRemito d : detalleRemito) d.aceptarDocumentacion(docRecibidaYAceptada, responsable);
    }

    /** 28. Registrar recepción [contenido igual al registrado = NO] / recibirYAceptarParcial(). */
    public void recibirYAceptarParcial(Estado recibidoYAceptadoParcial, Estado docRecibidaYAceptada, Estado docNoRecibida,
                                       Collection<Integer> documentacionesNoRecibidas, Empleado responsable) {
        Estado.validar(estado, "el remito " + numero, "recibir", EN_BOLSIN_ENVIADO);
        setEstado(recibidoYAceptadoParcial);
        for (DetalleRemito d : detalleRemito) {
            if (documentacionesNoRecibidas.contains(d.getDocumentacion().getNumero())) d.actualizarEstadoDoc(docNoRecibida, responsable);
            else d.aceptarDocumentacion(docRecibidaYAceptada, responsable);
        }
    }

    public void actualizarEstadoDoc(Estado estadoDoc, Empleado responsable) {
        for (DetalleRemito d : detalleRemito) d.actualizarEstadoDoc(estadoDoc, responsable);
    }

    public List<Documentacion> buscarDocumentacion() {
        return detalleRemito.stream().map(DetalleRemito::getDocumentacion).toList();
    }

    public boolean contieneAlgunaDocumentacion(Collection<Integer> numeros) {
        return buscarDocumentacion().stream().anyMatch(d -> numeros.contains(d.getNumero()));
    }

    public boolean estasGenerado() { return estado.es(CREADO); }

    public boolean tenesEstaCMDestino(ComisionMedica cm) { return destino.equals(cm); }

    public boolean tenesEstaCMOrigen(ComisionMedica cm) { return origen.equals(cm); }

    public String getDatosRemito() {
        return "Remito N° " + numero + " - " + fecha + " - " + origen.getNombre() + " -> " + destino.getNombre() + " - " + estado.getNombre();
    }

    public List<String> mostrarInformacionRemito() {
        return detalleRemito.stream().map(DetalleRemito::mostrarDocumentacion).toList();
    }

    public void setEstado(Estado estado) {
        if (!estado.esAmbitoRemito()) throw new IllegalArgumentException("El estado " + estado.getNombre() + " no es de ámbito Remito");
        this.estado = estado;
    }

    public Integer getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public ComisionMedica getOrigen() { return origen; }
    public ComisionMedica getDestino() { return destino; }
    public Estado getEstado() { return estado; }
    public List<DetalleRemito> getDetalleRemito() { return List.copyOf(detalleRemito); }
}
