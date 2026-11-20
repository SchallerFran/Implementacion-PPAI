package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.api.Dtos.*;
import ar.edu.utn.frc.dsi.bolsines.modelo.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Remito;
import ar.edu.utn.frc.dsi.bolsines.repositorio.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.ComisionMedicaRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.RemitoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ar.edu.utn.frc.dsi.bolsines.modelo.Estado.*;

/**
 * Máquina de estados de Bolsín: CU 19 Generar, 20 Modificar, 21 Eliminar, 22 Consultar,
 * 27 Registrar retiro, 28 Registrar recepción, 39 Cerrar (precintar), 40 Reabrir.
 */
@RestController
@RequestMapping("/api/bolsines")
public class BolsinController {
    private final BolsinRepository bolsines;
    private final RemitoRepository remitos;
    private final ComisionMedicaRepository comisiones;
    private final EstadoRepository estados;
    private final SesionActual sesionActual;

    public BolsinController(BolsinRepository bolsines, RemitoRepository remitos, ComisionMedicaRepository comisiones,
                            EstadoRepository estados, SesionActual sesionActual) {
        this.bolsines = bolsines;
        this.remitos = remitos;
        this.comisiones = comisiones;
        this.estados = estados;
        this.sesionActual = sesionActual;
    }

    /** 19. Generar Bolsín: Creado; remitos y documentación -> EnBolsinSaliente. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public BolsinDto generar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @Valid @RequestBody GenerarBolsinRequest req) {
        Empleado eb = encargado(sesionId);
        Bolsin b = new Bolsin(bolsines.siguienteNumero(), eb.getCM(), comisiones.porCodigo(req.codigoCMDestino()), req.peso(),
                remitos(req.remitos()), estados.bolsin(CREADO),
                estados.remito(EN_BOLSIN_SALIENTE), estados.documentacion(EN_BOLSIN_SALIENTE), eb);
        return BolsinDto.de(bolsines.save(b));
    }

    /** 20. Modificar Bolsín: solo en Creado; quita remitos completos (vuelven a Creado) y/o agrega nuevos. */
    @PutMapping("/{numero}")
    @Transactional
    public BolsinDto modificar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero,
                               @Valid @RequestBody ModificarBolsinRequest req) {
        Empleado eb = encargado(sesionId);
        Bolsin b = bolsinDeOrigen(numero, eb);
        b.modificar(remitos(req.remitos()), estados.remito(CREADO), estados.documentacion(EN_REMITO),
                estados.remito(EN_BOLSIN_SALIENTE), estados.documentacion(EN_BOLSIN_SALIENTE), eb);
        return BolsinDto.de(b);
    }

    /** 39. Cerrar Bolsín: Creado -> Cerrado con número de precinto. */
    @PostMapping("/{numero}/precintar")
    @Transactional
    public BolsinDto precintar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero,
                               @Valid @RequestBody PrecintarRequest req) {
        Empleado eb = encargado(sesionId);
        Bolsin b = bolsinDeOrigen(numero, eb);
        if (bolsines.existsByNumeroPrecinto(req.numeroPrecinto().strip())) {
            throw new IllegalStateException("El precinto " + req.numeroPrecinto() + " ya está colocado en otro bolsín");
        }
        b.precintar(req.numeroPrecinto(), estados.bolsin(CERRADO), eb);
        return BolsinDto.de(b);
    }

    /** 40. Reabrir Bolsín: Cerrado -> Creado, se borra el precinto. */
    @PostMapping("/{numero}/abrir")
    @Transactional
    public BolsinDto abrir(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        Empleado eb = encargado(sesionId);
        Bolsin b = bolsinDeOrigen(numero, eb);
        b.abrir(estados.bolsin(CREADO), eb);
        return BolsinDto.de(b);
    }

    /** 21. Eliminar Bolsín: Creado|Cerrado -> DeBaja; remitos -> Creado; documentación -> EnRemito. */
    @DeleteMapping("/{numero}")
    @Transactional
    public BolsinDto eliminar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        Empleado eb = encargado(sesionId);
        Bolsin b = bolsinDeOrigen(numero, eb);
        b.eliminar(estados.bolsin(DE_BAJA), estados.remito(CREADO), estados.documentacion(EN_REMITO), eb);
        return BolsinDto.de(b);
    }

    /** 27. Registrar retiro de bolsines: Cerrado -> Enviado; remitos y documentación -> EnBolsinEnviado. */
    @PostMapping("/{numero}/retiro")
    @Transactional
    public BolsinDto registrarRetiro(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        Empleado eb = encargado(sesionId);
        Bolsin b = bolsinDeOrigen(numero, eb);
        b.enviar(estados.bolsin(ENVIADO), estados.remito(EN_BOLSIN_ENVIADO), estados.documentacion(EN_BOLSIN_ENVIADO), eb);
        return BolsinDto.de(b);
    }

    /** 28. Registrar recepción de bolsín (lo registra el EB de la CM destino): Enviado -> Recibido. */
    @PostMapping("/{numero}/recepcion")
    @Transactional
    public BolsinDto registrarRecepcion(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero,
                                        @Valid @RequestBody(required = false) RecepcionRequest req) {
        Empleado eb = encargado(sesionId);
        Bolsin b = bolsines.porNumero(numero);
        if (!eb.esTuCM(b.getCMDestino())) {
            throw new SecurityException("El bolsín " + numero + " no está dirigido a la " + eb.mostrarCM());
        }
        List<Integer> noRecibidas = req == null ? List.of() : req.noRecibidas();
        b.recibir(estados.bolsin(RECIBIDO), estados.remito(RECIBIDO_Y_ACEPTADO), estados.remito(RECIBIDO_Y_ACEPTADO_PARCIAL),
                estados.documentacion(RECIBIDA_Y_ACEPTADA), estados.documentacion(NO_RECIBIDA), noRecibidas, eb);
        return BolsinDto.de(b);
    }

    /** 22. Consultar Bolsín: bolsines enviados o recibidos por la CM del usuario. Filtros opcionales. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<BolsinDto> listar(@RequestHeader(SesionActual.HEADER) UUID sesionId,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String numeroPrecinto) {
        var cm = sesionActual.empleado(sesionId).getCM();
        return bolsines.findByOrigenOrDestinoOrderByNumeroBolsin(cm, cm).stream()
                .filter(b -> estado == null || b.getEstadoActual().es(estado))
                .filter(b -> numeroPrecinto == null || numeroPrecinto.equalsIgnoreCase(b.getNumeroPrecinto()))
                .map(BolsinDto::de)
                .toList();
    }

    @GetMapping("/{numero}")
    @Transactional(readOnly = true)
    public BolsinDto consultar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        sesionActual.obtener(sesionId);
        return BolsinDto.de(bolsines.porNumero(numero));
    }

    private Empleado encargado(UUID sesionId) {
        Empleado e = sesionActual.empleado(sesionId);
        if (!e.sosEncargadoDeBolsines()) throw new SecurityException("La operación requiere el rol Encargado de Bolsines");
        return e;
    }

    /** Solo el EB de la CM origen puede operar el bolsín mientras está en su CM. */
    private Bolsin bolsinDeOrigen(int numero, Empleado eb) {
        Bolsin b = bolsines.porNumero(numero);
        if (!b.esTuComisionMedicaOrigen(eb.getCM())) {
            throw new SecurityException("El bolsín " + numero + " no fue generado por la " + eb.mostrarCM());
        }
        return b;
    }

    private List<Remito> remitos(List<Integer> numeros) {
        return numeros.stream().distinct().map(remitos::porNumero).toList();
    }
}
